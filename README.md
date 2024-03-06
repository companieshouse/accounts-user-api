# accounts-user-api
## User Account Access API

### Overview
This API grants access to user account information and associated functionalities. It streamlines interactions with user resources within the system. For more detailed information and related Confluence pages, refer to the [accounts-user-api LLD - IDV - Confluence](https://your-company.atlassian.net/wiki/spaces/...)

### Prerequisites
To build the service and execute unit tests, ensure you have the following:
- Java 21
- Maven
- Git

### Getting Started with Docker
To set up and build the service using Docker, follow these steps:

1. Clone Docker CHS Development repository and follow instructions in the README.
2. Execute the following:
      - `./bin/chs-dev services enable accounts-user-api`
      - Run `./bin/chs-dev development enable accounts-user-api` for making changes.
3. Ensure you're using Java 21 (the same version as the ECR image).
4. Start Docker using `tilt up` in the docker-chs-development directory.
5. Use spacebar in the command line to open the tilt window and wait for `accounts-user-api` to become green.
6. Open your browser and navigate to http://api.chs.local/accounts-user-api/healthcheck.
7. If you're accessing the API directly, use this URL: http://api.chs.local/accounts-user-api.

Note: These instructions are tailored for a local Docker environment.

For further details, please refer to the documentation and associated resources.

Common API libraries
[Common API Library Usage README](docs/common-api-libraries-readme.md) 

## ERIC Request Headers
Header Code Variable    | Header Field Name   | Usage
:-----------------------|:--------------------|:-----------
 ERIC_REQUEST_ID_KEY    | X-Request-Id        | Context of the request for E2E logging
 ERIC_IDENTITY          | ERIC-identity       | This contains the API key

### Endpoints

The full path for each public endpoints:

[Endpoints Documentation](docs/endpoint-documentation.md) 