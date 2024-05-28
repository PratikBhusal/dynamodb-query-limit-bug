Toy project to showcase the wierd quirk when trying to query a DynamoDB local
table.

# Getting Started

## First Time

1. Clone the repo:

```sh
git clone git@github.com:PratikBhusal/dynamodb-query-limit-bug.git
```
2. Verify test fails by running:

```sh
gradle clean build
```

with the following log output:

```
> Task :library:test FAILED

DefaultProductDaoTest > query returns less than the max items when there are 25 in the partition() FAILED
    java.lang.AssertionError: query returned 12 items from the table
    50 should be <= 12
        at org.example.persistence.ddb.dao.impl.DefaultProductDaoTest.query returns less than the max items when there are 25 in the partition(DefaultProductDaoTest.kt:110)
```
