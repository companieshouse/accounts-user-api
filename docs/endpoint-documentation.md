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
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - API Key authentication is required for accessing the endpoints. Provide the API Key in the `Authorization` header.

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
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - API Key authentication is required for accessing the endpoints. Provide the API Key in the `Authorization` header.

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
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - API Key authentication is required for accessing the endpoints. Provide the API Key in the `Authorization` header.
    - OAuth2 authentication is required for accessing the endpoints.

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
    - `401`: Unauthorized. OAuth token not used.
     - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - API Key authentication is required for accessing the endpoints. Provide the API Key in the `Authorization` header.
    - OAuth2 authentication is required for accessing the endpoints.

### 5. Find User Based on a Partial Email
- **Endpoint:** `/internal/users/search`
- **Method:** GET
- **Description:** Searches for users based on a partial email address.
- **Parameters:**
    - `partial_email` (path parameter, required): The partial email to use to match to users emails.
    - `X-Request-Id` (header): A unique identifier for the request.
- **Responses:**
    - `200`: Success. Returns an array of user resources.
    - `400`: Bad request. The request body has errors.
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - OAuth2 authentication is required for accessing the endpoints.

### 6. Get All Roles
- **Endpoint:** `/internal/admin/roles`
- **Method:** GET
- **Description:** Returns a list of all the roles and the associated permissions.
- **Parameters:**
    - `X-Request-Id` (header): A unique identifier for the request.
- **Responses:**
    - `200`: Success. Returns an array of Roles resources.
    - `400`: Bad request. The request body has errors.
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - OAuth2 authentication is required for accessing the endpoints.

### 7. Add a New Role
- **Endpoint:** `/internal/admin/roles/add`
- **Method:** POST
- **Description:** Adds a new Role to the database.
- **Parameters:**
    - `X-Request-Id` (header): A unique identifier for the request.
- **Request Body:**
    - `Role` : The new Role to be added.
- **Responses:**
    - `201`: Success. Adds the new Role to the database.
    - `400`: Bad request. The request body has errors.
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - OAuth2 authentication is required for accessing the endpoints.

### 8. Edit an Existing Role
- **Endpoint:** `/internal/admin/{role_id}/edit`
- **Method:** POST
- **Description:** Edits an existing Role.
- **Parameters:**
    - `X-Request-Id` (header): A unique identifier for the request.
    - `role_id` (path parameter, required): The role to be edited.
- **Request Body:**
    - `PermissionList` : The permissions to added to the specified Role.
- **Responses:**
    - `200`: Success. Role is successfully updated.
    - `400`: Bad request. The request body has errors.
    - `500`: Internal Server Error.
- **Security:**
    - OAuth2 authentication is required for accessing the endpoints.

### 9. delete a Role
- **Endpoint:** `/internal/admin/{role_id}/delete`
- **Method:** DELETE
- **Description:** Deletes a Role from the database.
- **Parameters:**
    - `X-Request-Id` (header): A unique identifier for the request.
    - `role_id` (path parameter, required): The role to be deleted from the database.
- **Responses:**
    - `204`: Success. Role is successfully deleted.
    - `400`: Bad request. The request body has errors.
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - OAuth2 authentication is required for accessing the endpoints.

### 10. Get a user based on their UserId
- **Endpoint:** `/internal/users/{user_id}`
- **Method:** GET
- **Description:** Searches for the user with the matching user_id.
- **Parameters:**
    - `user_id` (path parameter, required): The users unique ID.
    - `X-Request-Id` (header): A unique identifier for the request.
- **Responses:**
    - `200`: Success. Returns the matched user resource.
    - `400`: Bad request. The request body has errors.
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - OAuth2 authentication is required for accessing the endpoints.

### 11. Get Current User Profile
- **Endpoint:** `/user/profile`
- **Method:** GET
- **Description:** Retrieves details of current user.
- **Parameters:**
    - `X-Request-Id` (header): A unique identifier for the request.
- **Responses:**
    - `200`: Success. Returns the user resource.
    - `401`: Unauthorized. OAuth token not used.
    - `403`: Forbidden. User does not have the required permission.
    - `500`: Internal Server Error.
- **Security:**
    - OAuth2 authentication is required for accessing the endpoints.

### 12. Healthcheck
- **Endpoint:** `accounts-user-api/healthcheck`
- **Method:** GET
- **Description:** Healthcheck.
- **Responses:**
    - `200`: Success. Returns the user resource.

## Data Models
- **User:** Represents user information including roles.
- **Role:** Represents user roles.

## Error Handling
- Errors are returned with appropriate HTTP status codes and error messages.

For detailed information on request and response schemas, please refer to the API documentation. [High Level Design](https://companieshouse.atlassian.net/wiki/spaces/IDV/pages/4471619599/High+Level+Design+V3#API-Spec)
