# User Management APIs

This page documents the API's available for the management of the users
-----------------------------------------------------------------------

<details>
  <summary><code>GET</code> <code><b>/users/search</b></code> <code>Gets the user with the specified email address</code></summary>

### Parameters

> | name              |  type     | data type      | description                         |
> |-------------------|-----------|----------------|-------------------------------------|
> | `user_email`      |  required | string         | The required email to search for    |

### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `User Record`                                                       |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `404`         | `application/json`                | `{"code":"404","message":"Not Found"}`                              |
> | `500`         |  None                             | None                                                                |

### Example cURL

#### Command 

 ```javascript
  curl -X GET -H "Content-Type: application/json" "http://api.chs.local:4001/users/search?user_email=demo@ch.gov.uk"
 ```
#### Response
```json
[
    {
        "forename": null,
        "surname": null,
        "email": "demo@ch.gov.uk",
        "user_id": "67ZeMsvAEgkBWs7tNKacdrPvOmQ",
        "display_name": null,
        "roles": [
            "supervisor",
            "bulk-refunds",
            "chs-orders-investigator"
        ],
        "hasLinkedOneLogin": true,
        "isPrivateBetaUser": true
    }
]
```
</details>

<details>
  <summary><code>GET</code> <code><b>/users/search_part_email</b></code> <code>Gets the users that have the supplied 'sub string' in their email address</code></summary>

### Parameters

> | name              |  type     | data type      | description                                  |
> |-------------------|-----------|----------------|----------------------------------------------|
> | `part_email`      |  required | string         | The sub string to search all user emails for |

### Responses

> | http code     | content-type                      | response                                   |
> |---------------|-----------------------------------|--------------------------------------------|
> | `200`         | `application/json`                | `List of User Record`                      |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`   |
> | `404`         | `application/json`                | `{"code":"404","message":"Not Found"}`     |
> | `500`         |  None                             | None                                       |

### Example cURL

#### Command 

 ```javascript
  curl -X GET -H "Content-Type: application/json" "http://api.chs.local:4001/users/search_part_email?part_email=demo"
 ```
#### Response
```json
[
    {
        "forename": null,
        "surname": null,
        "email": "demo@ch.gov.uk",
        "user_id": "Y2VkZWVlMzhlZWFjY2M4MzQ3MT",
        "display_name": null,
        "roles": [
            "supervisor",
            "bados-user",
            "bulk-refunds",
            "chs-orders-investigator"
        ],
        "hasLinkedOneLogin": false,
        "isPrivateBetaUser": false
    },
    {
        "forename": null,
        "surname": null,
        "email": "demo2@ch.gov.uk",
        "user_id": "Y2VkZWVlMzhlZWFjY2M4MzQ3MU",
        "display_name": null,
        "roles": [
            "restricted-word"
        ],
        "hasLinkedOneLogin": false,
        "isPrivateBetaUser": false
    }
]
```
</details>

<details>
  <summary><code>GET</code> <code><b>/users/{user_id}</b></code> <code>retrieves a user record based on the supplied user_id</code></summary>

### Parameters

> | name              |  type     | data type      | description                         |
> |-------------------|-----------|----------------|-------------------------------------|
> | `user_id`         |  required | string         | The unique key used for the user    |

### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `User record`                                                       |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `404`         | `application/json`                | `{"code":"404","message":"Not Found"}`                              |
> | `500`         |  None                             | None                                                                |

### Example cURL

#### Command 
> ```javascript
>  curl -X GET -H "Content-Type: application/json" "http://api.chs.local:4001/users/67ZeMsvAEgkBWs7tNKacdrPvOmQ"
> ```

#### Response
```json
[
    {
        "forename": null,
        "surname": null,
        "email": "demo@ch.gov.uk",
        "user_id": "67ZeMsvAEgkBWs7tNKacdrPvOmQ",
        "display_name": null,
        "roles": [
            "supervisor",
            "bulk-refunds",
            "chs-orders-investigator"
        ],
        "hasLinkedOneLogin": true,
        "isPrivateBetaUser": true
    }
]
```
</details>

<details>
  <summary><code>GET</code> <code><b>/users/{user_id}/roles</b></code> <code>Gets the roles assigned to the specified user</code></summary>

### Parameters

> | name              |  type     | data type      | description                         |
> |-------------------|-----------|----------------|-------------------------------------|
> | `user_id`         |  required | string         | The unique key used for the user    |

### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         | `application/json`                | `List of roles for a user`                                          |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `404`         | `application/json`                | `{"code":"404","message":"Not Found"}`                              |
> | `500`         |  None                             | None                                                                |

### Example cURL

#### Command 
> ```javascript
>  curl -X GET -H "Content-Type: application/json" "http://api.chs.local:4001/users/67ZeMsvAEgkBWs7tNKacdrPvOmQ/roles"
> ```

#### Response
```json
[
    "supervisor",
    "bulk-refunds",
    "chs-orders-investigator"
]
```
</details>

<details>
  <summary><code>PUT</code> <code><b>/users/{user_id}/roles</b></code> <code>Sets the roles to be assigned to the specified user</code></summary>

### Parameters

#### Header
> | name              |  type     | data type      | description                             |
> |-------------------|-----------|----------------|-----------------------------------------|
> | `user_id`         |  required | string         | The unique key used for the user        |


#### Body
> | Body type | description                |
> |---------- |----------------------------|
> | JSON      | list of roles for the user |


### Responses

> | http code     | content-type                      | response                                                            |
> |---------------|-----------------------------------|---------------------------------------------------------------------|
> | `200`         |  None                             | None                                                                    |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`                            |
> | `404`         | `application/json`                | `{"code":"404","message":"Not Found"}`                              |
> | `500`         |  None                             | None                                                                |

### Example cURL

#### Command 
> ```javascript
>  curl -X PUT -H "content-type: application/json" "http://api.chs.local:4001/users/67ZeMsvAEgkBWs7tNKacdrPvOmQ/roles" -L  -d '["supervisor","bulk-refunds"]'
> ```

</details>