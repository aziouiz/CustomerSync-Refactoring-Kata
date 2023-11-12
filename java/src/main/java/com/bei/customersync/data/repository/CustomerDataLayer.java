package com.bei.customersync.data.repository;

import com.bei.customersync.data.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerDataLayer extends JpaRepository<Customer, UUID> {

    Customer findByExternalId(String externalId);

    Customer findByMasterExternalId(String externalId);

    Customer findByCompanyNumber(String companyNumber);
}
