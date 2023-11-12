Consumer Match Kata 
====================

## Extra development related to the branch real-life-example
This branch builds on top of the solution made on the branch mvp and adds package separation, model separation and more importantly a more concrete working example which assumes that the external customer is received through a Kafka topic, and is used to update an existing internal customer on the DB (postgres).

### To deploy locally :

1/ Cd into the folder local/customersync which contains the local docker-compose
```shell
  cd local/customersync
```

2/ Start the needed containers
```shell
  docker-compose up -d
```

3/ Now, From the directory java, run the backend app :
```shell
  mvn spring-boot:run
```

4/ Now you can reproduce the same tests done by emily bach on here branch with_tests, from within the same folder as previously local/customersync execute :

```shell
    docker-compose exec kafka kafka-console-producer.sh --topic customer_updated --broker-list localhost:9092 --property parse.headers=true --property headers.key.separator=: --property headers.delimiter=.
```

To create a company you can do something like :
```shell
    __TypeId__:Customer.{"name": "company1", "externalId": "SYSTEM01-C01", "preferredStore": "store1", "external_id": "C01", "companyNumber": "C012023", "address": {"street": "rue de nullpart", "postalCode": "L-1212", "city": "Luxembourg"}, "shoppingLists": [{"products" : ["product1", "product2"]}, {"products" : ["product3"]}]}
```

Then you can try the same customer and remove the company number to trigger the conflict : 

```shell
  __TypeId__:Customer.{"name": "company1", "externalId": "SYSTEM01-C01", "preferredStore": "store1", "external_id": "C01", "address": {"street": "rue de nullpart", "postalCode": "L-1212", "city": "Luxembourg"}, "shoppingLists": [{"products" : ["product1", "product2"]}, {"products" : ["product3"]}]}
```

for personal customers you can do something like :
```shell
    __TypeId__:Customer.{"name": "john doe", "externalId": "SYSTEM01-C02", "preferredStore": "store2", "external_id": "C02", "bonusPointsBalance": "50", "address": {"street": "rue de john doe", "postalCode": "L-9999", "city": "Esch-sur-alzette"}, "shoppingLists": [{"products" : ["product4", "product5"]}, {"products" : ["product6"]}]}
```

then you can trigger a conflict by trying to sync it again with a company number :
```shell
  __TypeId__:Customer.{"name": "john doe", "externalId": "SYSTEM01-C02", "companyNumber": "C012023", "preferredStore": "store2", "external_id": "C02", "bonusPointsBalance": "50", "address": {"street": "rue de john doe", "postalCode": "L-9999", "city": "Esch-sur-alzette"}, "shoppingLists": [{"products" : ["product4", "product5"]}, {"products" : ["product6"]}]}
```


....etc.

-----
This README provides insights into the motivations and decisions behind the recent refactoring of this codebase.

## Scope of the solution:
From what I understand the goal of this exercise is to be able to split the business logic from the data access layer and add a small modification (bonusPointsBalance).
I did not see it fit for the purpose of the exercise to split packages, create DTO's....etc.

## Initial Issue:
Previously, the code suffered from a critical issue where the business logic was inconsistently distributed between two components: CustomerSync and CustomerDataAccess. 
This resulted in an ambiguous and inefficient process where the logic for identifying duplicate customers was unpredictably split between these two classes.

## Refactoring Goals:
Understanding the core of the problem led me to a streamlined approach, simplifying the process into two primary steps:

- Customer Search: Implementing varied methodologies to search for customers, including identifying potential duplicates, depending on the customer type.
- Update and Persist: Updating customer fields and the duplicates.

Structural Changes:
To achieve this streamlined process, I decided to eliminate the CustomerDataAccess component. 
This layer, rather than adding value, was contributing to confusion and complexity.
Now, CustomerSync assumes full responsibility for executing the aforementioned steps. 
It directly interacts with the CustomerDataLayer for data retrieval and updates. 
While CustomerDataAccess could have acted as a proxy, especially in scenarios where the persisted model diverges from the domain model, its removal simplifies the current solution without compromising functionality.
We can also note that by getting rid of CustomerDataAccess and putting the business logic in CustomerSync we no longer need to maintain the class CustomerMatches which was not really a model class but only a variable of sharing the state between CustomerDataAccess and CustomerSync.