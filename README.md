Consumer Match Kata 
====================

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