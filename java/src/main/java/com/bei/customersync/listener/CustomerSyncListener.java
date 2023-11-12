package com.bei.customersync.listener;

import com.bei.customersync.business.CustomerSyncService;
import com.bei.customersync.business.exceptions.ConflictException;
import com.bei.customersync.dto.ExternalCustomerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerSyncListener {

    Logger logger = LoggerFactory.getLogger(CustomerSyncListener.class);

    private final CustomerSyncService customerSyncService;

    public CustomerSyncListener(CustomerSyncService customerSyncService) {
        this.customerSyncService = customerSyncService;
    }

    @KafkaListener(topics = "customer_updated")
    public void syncConsumer(ExternalCustomerDto externalCustomer) {
        logger.info("Received customer with external id {}", externalCustomer.getExternalId());

        try {
            customerSyncService.syncWithDataLayer(externalCustomer);
        } catch (ConflictException e) {
            logger.error("Could not sync customer {} due to a conflict, will be ignored", externalCustomer.getExternalId(), e);
        }
    }
}
