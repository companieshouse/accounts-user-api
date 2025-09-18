# Accounts User API

### Overview

This API grants access to user account information and associated functionalities. It streamlines interactions with user resources within the system. 

For more detailed information and related Confluence pages, refer to the following:


- [LLD](https://companieshouse.atlassian.net/wiki/spaces/IDV/pages/4443963495/accounts-user-api+LLD)
- [HLD](https://companieshouse.atlassian.net/wiki/spaces/IDV/pages/4441571350/accounts-user-api+API+HLD)
- [API Specification](https://companieshouse.atlassian.net/wiki/spaces/IDV/pages/4441571350/accounts-user-api+API+HLD)

### Prerequisites

To build the service and execute unit tests, ensure you have the following:
- Java 21
- Maven
- Git

## Adding a new API Endpoint:
### Specification for API: Private Java and Controllers
In this section, we will enhance the specification file to encompass:

- API Endpoint
- Comprehensive Parameter Description (Header, Query, and Path)
- Exemplary Dataset

Upon finalization of the controller code, integration testing, such as with Postman, can commence.

### Create Service and Mongo Dao and Repository 

Steps:

- Utilize the model class as a foundation for creating the DAO.
- Within the service class, establish values for any read-only fields.
- Author unit and integration tests (MongoDB) 
- Integrate the controller to utilize the service.
- Following approval and compilation of this code, testers can commence testing for the functionality.

### Add Validation in the Service if required

Service should do any business or database checks. If a resource request is not found then a 404 error is returned

A business validation error will throw an exception that will be handled in the application ControllerAdvice class and a HTTP Bad request is returned with an Errors object from the CH standard Errors class.

## Getting Started with Docker
To set up and build the service using Docker, follow these steps:

1. Clone Docker CHS Development repository and follow instructions in the README.
2. Execute the following:
   - `./bin/chs-dev services enable accounts-user-api`
   - `./bin/chs-dev development enable accounts-user-api` 
3. Ensure you're using Java 21 
4. Start Docker using `chs-dev up` in the docker-chs-development directory.
5. Open your browser and navigate to http://api.chs.local/users/healthcheck.

Note: These instructions are tailored for a local Docker environment.

For further details, please refer to the documentation and associated resources.

## Endpoint Documentation

#### User API Documentation

- [Endpoints Documentation](docs/endpoint-documentation.md)

#### Request Headers 

- [Header variables](./docs/header-variables.md)

#### Common API libraries: 


- [API Model Specification](docs/common-api-libraries-open-api.md)

- [Common API Library Usage README](docs/common-api-libraries-readme.md)




