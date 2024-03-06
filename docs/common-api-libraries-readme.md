# Common API Libraries Usage in Project

## Libraries Utilized

- [rest-service-common-library](https://github.com/companieshouse/rest-service-common-library)
- [private-api-sdk-java](https://github.com/companieshouse/private-api-sdk-java)
- [private.api.ch.gov.uk-specifications](https://github.com/companieshouse/private.api.ch.gov.uk-specifications)

## Note Regarding Java 21

Java 21 versions of the common libraries are required.

If any of these libraries are updated, both versions (pre-Java 21 and Java 21) should be updated, as outlined in the library project README.

## Usage of rest-service-common-library

This library is employed to return standard CH `Errors` objects for validation failures.

Points to note about the `Errors` object:

- It is populated solely for business errors like validation failures in input data during a post or a missing record in a get (this is a private API).
- It remains empty for exceptions like null pointers and similar errors, which are likely programming bugs.

## Usage of private-api-sdk-java (and private.api.ch.gov.uk-specifications)

The `private-api-sdk-java` contains the API resources used in this project.

1. The model for the API should be authored in `private.api.ch.gov.uk-specifications` (and this project should **NOT** be directly referenced in `accounts-user-api`). This allows the resources to be generated for both Java and Node clients.
2. In `private-api-sdk-java`, the API resources required by `accounts-user-api` are generated. (The `private-api-sdk-java` utilizes the spec defined in `private.api.ch.gov.uk-specifications` to generate the necessary API resources).
3. The DTOs are brought into `accounts-user-api` as a dependency from `private-api-sdk-java` (ensure that these generated classes are stored in source control). Be sure to use the version generated when the PR for `private-api-sdk-java` is merged.

Refer to the detailed [open-api.md](https://github.com/companieshouse/accounts-user-api/docs/common-api-libraries-open-api.md) for the complete process.

