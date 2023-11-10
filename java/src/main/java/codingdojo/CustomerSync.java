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

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        if (externalCustomer.isCompany()) {
            return syncCompanyCustomer(externalCustomer);
        }
        return syncPersonCustomer(externalCustomer);
    }

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
                duplicates.add(createNewCustomer(externalCustomer));
            }
        }

        return doSync(externalCustomer, customerToSync, duplicates);
    }

    boolean syncPersonCustomer(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();

        Customer customerToSync = this.customerDataLayer.findByExternalId(externalId);
        validatePersonCustomerType(externalId, customerToSync);

        return doSync(externalCustomer, customerToSync, Collections.emptyList());
    }

    boolean doSync(ExternalCustomer externalCustomer, Customer customerToSync, Collection<Customer> duplicates) {
        boolean created = false;

        if (customerToSync == null) {
            created = true;
            customerToSync = createNewCustomer(externalCustomer);
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

    void save(Customer customer) {
        if (customer.getInternalId() == null) {
            customerDataLayer.createCustomerRecord(customer);
        } else {
            customerDataLayer.updateCustomerRecord(customer);
        }
    }

    Customer createNewCustomer(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        return customer;
    }

    void updateDuplicateFields(ExternalCustomer externalCustomer, Customer duplicate) {
        duplicate.setName(externalCustomer.getName());
    }

    void updateCustomerFields(ExternalCustomer externalCustomer, Customer customer) {
        customer.setName(externalCustomer.getName());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        customer.setAddress(externalCustomer.getPostalAddress());
        if (externalCustomer.isCompany()) {
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
            customer.setCustomerType(CustomerType.COMPANY);
        } else {
            customer.setCustomerType(CustomerType.PERSON);
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
