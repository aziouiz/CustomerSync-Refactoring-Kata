package com.bei.customersync.business;

import com.bei.customersync.dto.ExternalCustomerDto;

public interface CustomerSyncService {
    boolean syncWithDataLayer(ExternalCustomerDto externalCustomer);
}
