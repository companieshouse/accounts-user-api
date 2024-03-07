# User Account Access API

## Introduction
This API provides access to user account information and related functionalities. It is designed to facilitate interactions with user resources within the system.

## OpenAPI Specification
- Version: 3.0.3
- Security: API Key authentication

## Endpoints

### 1. Get User Details
- **Endpoint:** `/users/{user_id}`
- **Method:** GET
- **Description:** Retrieves details of a specific user by their ID.
- **Parameters:**
    - `user_id` (path parameter, required): The unique identifier of the user.
    - `X-Request-Id` (header): A unique identifier for the request.
- **Responses:**
    - `200`: Success. Returns the user resource.
    - `400`: Bad request. The request body has errors.
    - `500`: Internal Server Error.

### 2. Find User Based on Email
- **Endpoint:** `/users/search`
- **Method:** GET
- **Description:** Searches for users based on their email addresses.
- **Parameters:**
    - `user_email` (query parameter): The email address of the user.
    - `X-Request-Id` (header): A unique identifier for the request.
- **Responses:**
    - `200`: Success. Returns an array of user resources.
    - `400`: Bad request. The request body has errors.
    - `500`: Internal Server Error.

### 3. Get All User Roles
- **Endpoint:** `/users/{user_id}/roles`
- **Method:** GET
- **Description:** Retrieves all roles associated with a specific user.
- **Parameters:**
    - `user_id` (path parameter, required): The unique identifier of the user.
    - `X-Request-Id` (header): A unique identifier for the request.
- **Responses:**
    - `200`: Success. Returns an array of user roles.
    - `400`: Bad request. The request body has errors.
    - `500`: Internal Server Error.

### 4. Set All User Roles
- **Endpoint:** `/users/{user_id}/roles`
- **Method:** PUT
- **Description:** Sets all roles for a specific user.
- **Parameters:**
    - `user_id` (path parameter, required): The unique identifier of the user.
    - `X-Request-Id` (header): A unique identifier for the request.
- **Request Body:** Array of roles to be set for the user.
- **Responses:**
    - `201`: Success. User roles have been set.
    - `400`: Bad request. The request body has errors.
    - `500`: Internal Server Error.

## Security
- API Key authentication is required for accessing the endpoints. Provide the API Key in the `Authorization` header.

## Data Models
- **User:** Represents user information including roles.
- **Role:** Represents user roles.

## Error Handling
- Errors are returned with appropriate HTTP status codes and error messages.

For detailed information on request and response schemas, please refer to the API documentation. [High Level Design](https://companieshouse.atlassian.net/wiki/spaces/IDV/pages/4471619599/High+Level+Design+V3#API-Spec)



