package com.bei.customersync.data.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id")
    private UUID internalId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "master_external_id")
    private String masterExternalId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride( name = "street", column = @Column(name = "address_street")),
            @AttributeOverride( name = "city", column = @Column(name = "address_city")),
            @AttributeOverride( name = "postalCode", column = @Column(name = "address_postalcode"))
    })
    private Address address;

    @Column(name = "preferred_store")
    private String preferredStore;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "customer")
    private List<ShoppingList> shoppingLists = new ArrayList<>();

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    @Column(name = "company_number")
    private String companyNumber;

    @Column(name = "bonus_points_balance")
    private Integer bonusPointsBalance;

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setMasterExternalId(String masterExternalId) {
        this.masterExternalId = masterExternalId;
    }

    public String getMasterExternalId() {
        return masterExternalId;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public UUID getInternalId() {
        return internalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPreferredStore(String preferredStore) {
        this.preferredStore = preferredStore;
    }

    public String getPreferredStore() {
        return preferredStore;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public List<ShoppingList> getShoppingLists() {
        return shoppingLists;
    }

    public void setShoppingLists(List<ShoppingList> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }

    public String getName() {
        return name;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public void setInternalId(UUID internalId) {
        this.internalId = internalId;
    }

    public Integer getBonusPointsBalance() {
        return bonusPointsBalance;
    }

    public void setBonusPointsBalance(Integer bonusPointsBalance) {
        this.bonusPointsBalance = bonusPointsBalance;
    }
}
