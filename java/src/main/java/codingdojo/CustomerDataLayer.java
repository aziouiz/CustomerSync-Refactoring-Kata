package codingdojo;

public interface CustomerDataLayer {

    Customer updateCustomerRecord(Customer customer);

    Customer createCustomerRecord(Customer customer);

    Customer findByExternalId(String externalId);

    Customer findByMasterExternalId(String externalId);

    Customer findByCompanyNumber(String companyNumber);
}
