package com.bei.customersync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalCustomerDto {
    private AddressDto address;
    private String name;
    private String preferredStore;
    private List<ShoppingListDto> shoppingLists;
    private String externalId;
    private String companyNumber;
    private Integer bonusPointsBalance;

    public ExternalCustomerDto() {}

    public String getExternalId() {
        return externalId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public boolean isCompany() {
        return companyNumber != null;
    }

    public AddressDto getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferredStore() {
        return preferredStore;
    }

    public void setPreferredStore(String preferredStore) {
        this.preferredStore = preferredStore;
    }

    public List<ShoppingListDto> getShoppingLists() {
        return shoppingLists;
    }

    public void setShoppingLists(List<ShoppingListDto> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public Integer getBonusPointsBalance() {
        return bonusPointsBalance;
    }

    public void setBonusPointsBalance(Integer bonusPointsBalance) {
        this.bonusPointsBalance = bonusPointsBalance;
    }
}
