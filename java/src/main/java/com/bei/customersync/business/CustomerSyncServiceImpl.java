package com.bei.customersync.business;

import com.bei.customersync.business.exceptions.ConflictException;
import com.bei.customersync.data.entity.Customer;
import com.bei.customersync.data.entity.CustomerType;
import com.bei.customersync.data.repository.CustomerDataLayer;
import com.bei.customersync.dto.ExternalCustomerDto;
import com.bei.customersync.mapper.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Service
public class CustomerSyncServiceImpl implements CustomerSyncService {

    private final CustomerDataLayer customerDataLayer;
    private final CustomerMapper customerMapper;

    public CustomerSyncServiceImpl(CustomerDataLayer customerDataLayer, CustomerMapper customerMapper) {
        this.customerDataLayer = customerDataLayer;
        this.customerMapper = customerMapper;
    }

    /**
     * Syncs an external customer with an eventually existing equivalent. (company or person).
     * @param externalCustomer the external customer to sync
     * @return true if the customer was created, false if the customer already exists and was just updated.
     */
    @Override
    @Transactional
    public boolean syncWithDataLayer(ExternalCustomerDto externalCustomer) {
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
    boolean syncCompanyCustomer(ExternalCustomerDto externalCustomer) {
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
    boolean syncPersonCustomer(ExternalCustomerDto externalCustomer) {
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
    boolean doSync(ExternalCustomerDto externalCustomer, Customer customerToSync, Collection<Customer> duplicates) {
        boolean created = false;

        if (customerToSync == null) {
            created = true;
            customerToSync = getNewCustomer(externalCustomer);
        }

        //syncing the fields from external customer to customer.
        customerMapper.patchFields(externalCustomer, customerToSync);

        //saving all the duplicates
        duplicates
                .stream()
                .peek(duplicate -> updateDuplicateFields(externalCustomer, duplicate))
                .forEach(customerDataLayer::save);

        //saving the customer with a cascade persist on its relations (like shopping list)
        customerDataLayer.save(customerToSync);

        return created;
    }

    /**
     * Initialize a customer minimum fields based on its corresponding external customer.
     * @param externalCustomer the external customer on which the init should be based.
     * @return the initialized customer
     */
    Customer getNewCustomer(ExternalCustomerDto externalCustomer) {
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
    void updateDuplicateFields(ExternalCustomerDto externalCustomer, Customer duplicate) {
        duplicate.setName(externalCustomer.getName());
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
