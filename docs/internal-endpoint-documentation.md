# Internal User / Role Management APIs

This page documents the API's available for the searching of users using a partial email and manage the roles assigned to them
------------------------------------------------------------------------------------------------------------------------------

<details>
  <summary><code>GET</code> <code><b>/internal/users/search</b></code> <code>Gets the users that have the supplied 'sub string' in their email address</code></summary>

### Parameters

> | name              |  type     | data type      | description                                  |
> |-------------------|-----------|----------------|----------------------------------------------|
> | `partial_email`   |  required | string         | The partial email to search all users for    |

### Responses

> | http code     | content-type                      | response                                   |
> |---------------|-----------------------------------|--------------------------------------------|
> | `200`         | `application/json`                | `List of User Records`                     |
> | `400`         | `application/json`                | `{"code":"400","message":"Bad Request"}`   |
> | `404`         | `application/json`                | `{"code":"404","message":"Not Found"}`     |
> | `500`         |  None                             | None                                       |

### Example cURL

#### Command 

 ```javascript
  curl -X GET -H "Content-Type: application/json" "http://api.chs.local:4001/internal/users/search?partial_email=demo"
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
