# Header variables

HTTP Header variables are used in all requests **except** for the Heath Check.

The only header variable used for all other controller methods is the "X-Request-Id" HTTP Header field which is used for context logging.

## Headers required when accessing via eric

Header                  | Header Field Name   | Usage
:-----------------------|:--------------------|:-----------
Authorisation           | `Authorization`     | This contains the API key
X-Request-Id    | `X-Request-Id`      | Request Id