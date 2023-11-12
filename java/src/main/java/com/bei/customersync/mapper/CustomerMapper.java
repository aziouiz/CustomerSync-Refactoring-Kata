package com.bei.customersync.mapper;

import com.bei.customersync.data.entity.Customer;
import com.bei.customersync.data.entity.CustomerType;
import com.bei.customersync.dto.ExternalCustomerDto;
import org.mapstruct.*;

import java.util.Collections;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "companyNumber", ignore = true)
    @Mapping(target = "bonusPointsBalance", ignore = true)
    void patchFields(ExternalCustomerDto externalCustomer, @MappingTarget Customer customer);

    @BeforeMapping
    default void handleNotNullSubEntity(ExternalCustomerDto externalCustomer) {
        if (externalCustomer.getShoppingLists() == null) {
            externalCustomer.setShoppingLists(Collections.emptyList()); //hibernate popotte
        }
    }

    @AfterMapping
    default void afterMapping(ExternalCustomerDto externalCustomer, @MappingTarget Customer customer) {
        if (externalCustomer.isCompany()) {
            customer.setCustomerType(CustomerType.COMPANY);
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        } else {
            customer.setCustomerType(CustomerType.PERSON);
            customer.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
        }
        if (customer.getShoppingLists() != null) {
            customer.getShoppingLists().forEach(sl -> sl.setCustomer(customer)); //hibernate popotte!
        }
    }
}
