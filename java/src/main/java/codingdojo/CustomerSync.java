package codingdojo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomerSync {

    private final CustomerDataLayer customerDataLayer;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    /**
     * Syncs an external customer with an eventually existing equivalent. (company or person).
     * @param externalCustomer the external customer to sync
     * @return true if the customer was created, false if the customer already exists and was just updated.
     */
    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        if (externalCustomer.isCompany()) {
            return syncCompanyCustomer(externalCustomer);
        }
        return syncPersonCustomer(externalCustomer);
    }

    /**
     * Syncs an external company customer with an eventually existing equivalent.
     * @param externalCustomer the external customer to sync
     * @return true if the customer was created, false if the customer already exists and was just updated.
     */
    boolean syncCompanyCustomer(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();
        final String companyNumber = externalCustomer.getCompanyNumber();

        Collection<Customer> duplicates = new ArrayList<>();

        Customer customerToSync = this.customerDataLayer.findByExternalId(externalId);
        if (customerToSync != null) {
            validateCompanyCustomerType(externalId, customerToSync);

            Customer matchByMasterId = this.customerDataLayer.findByMasterExternalId(externalId);
            if (matchByMasterId != null) {
                duplicates.add(matchByMasterId);
            }
            String customerCompanyNumber = customerToSync.getCompanyNumber();
            if (!companyNumber.equals(customerCompanyNumber)) {
                duplicates.add(customerToSync);
                customerToSync = null;
            }
        } else {
            customerToSync = this.customerDataLayer.findByCompanyNumber(companyNumber);
            if (customerToSync != null) {
                validateCompanyCustomerType(externalId, customerToSync);
                validateCompanyExternalId(externalId, companyNumber, customerToSync.getExternalId());
                customerToSync.setExternalId(externalId);
                customerToSync.setMasterExternalId(externalId);
                duplicates.add(getNewCustomer(externalCustomer));
            }
        }

        return doSync(externalCustomer, customerToSync, duplicates);
    }


    /**
     * Syncs an external person customer with an eventually existing equivalent.
     * @param externalCustomer the external customer to sync
     * @return true if the customer was created, false if the customer already exists and was just updated.
     */
    boolean syncPersonCustomer(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();

        Customer customerToSync = this.customerDataLayer.findByExternalId(externalId);
        validatePersonCustomerType(externalId, customerToSync);

        return doSync(externalCustomer, customerToSync, Collections.emptyList());
    }

    /**
     * Given an external customer, its equivalent internal customer and some duplicates, this method will do all the necessary updates to sync them all.
     * @param externalCustomer the external customer that was synced
     * @param customerToSync its corresponding internal customer
     * @param duplicates the duplicates that were discovered.
     * @return true if the customer was created, false if it was just updated.
     */
    boolean doSync(ExternalCustomer externalCustomer, Customer customerToSync, Collection<Customer> duplicates) {
        boolean created = false;

        if (customerToSync == null) {
            created = true;
            customerToSync = getNewCustomer(externalCustomer);
        }

        //syncing the fields from external customer to customer.
        updateCustomerFields(externalCustomer, customerToSync);

        //saving all the duplicates
        duplicates
                .stream()
                .peek(duplicate -> updateDuplicateFields(externalCustomer, duplicate))
                .forEach(this::save);

        //saving the customer with a cascade persist on its relations (like shopping list)
        save(customerToSync);

        return created;
    }

    /**
     * Create or update a customer.
     * @param customer the customer to create/update.
     */
    void save(Customer customer) {
        if (customer.getInternalId() == null) {
            customerDataLayer.createCustomerRecord(customer);
        } else {
            customerDataLayer.updateCustomerRecord(customer);
        }
    }

    /**
     * Initialize a customer minimum fields based on its corresponding external customer.
     * @param externalCustomer the external customer on which the init should be based.
     * @return the initialized customer
     */
    Customer getNewCustomer(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        return customer;
    }

    /**
     * The update that should be on the duplicates, referred at as 'If there are several matching Customers in our database, update them all (slightly differently)' on emily bach Readme.
     * @param externalCustomer the external customer on which duplicates were discovered.
     * @param duplicate the corresponding duplicates.
     */
    void updateDuplicateFields(ExternalCustomer externalCustomer, Customer duplicate) {
        duplicate.setName(externalCustomer.getName());
    }

    /**
     * Updates all the necessary fields on the Customer from the External Customer.
     * @param externalCustomer the external customer that is used to update our internal customer
     * @param customer the internal customer
     */
    void updateCustomerFields(ExternalCustomer externalCustomer, Customer customer) {
        customer.setName(externalCustomer.getName());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        customer.setAddress(externalCustomer.getPostalAddress());
        if (externalCustomer.isCompany()) {
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
            customer.setCustomerType(CustomerType.COMPANY);
        } else {
            customer.setCustomerType(CustomerType.PERSON);
            customer.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
        }
        List<ShoppingList> consumerShoppingLists = externalCustomer.getShoppingLists();
        for (ShoppingList consumerShoppingList : consumerShoppingLists) {
            customer.addShoppingList(consumerShoppingList);
        }
    }

    void validateCompanyCustomerType(String externalId, Customer customer) {
        if (customer != null && !CustomerType.COMPANY.equals(customer.getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        }
    }

    void validateCompanyExternalId(String externalId, String companyNumber, String customerExternalId) {
        if (customerExternalId != null && !externalId.equals(customerExternalId)) {
            throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId);
        }
    }

    void validatePersonCustomerType(String externalId, Customer customerToSync) {
        if (customerToSync != null && !CustomerType.PERSON.equals(customerToSync.getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a person");
        }
    }
}
