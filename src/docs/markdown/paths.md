# Swagger Petstore

This is a sample server Petstore server.

[Learn about Swagger](http://swagger.wordnik.com) or join the IRC channel `#swagger` on irc.freenode.net.

For this sample, you can use the api key `special-key` to test the authorization filters

Version: 1.0.0
Contact: apiteam@wordnik.com
License: Apache 2.0
License URL: http://www.apache.org/licenses/LICENSE-2.0.html
Terms of service: http://helloreverb.com/terms/

Host: petstore.swagger.wordnik.com
BasePath: /v2
Schemes: HTTP

## Paths
### Update an existing pet
```
PUT /pets
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|BodyParameter|body|Pet object that needs to be added to the store|false|Pet|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|400|Invalid ID supplied|No Content|
|404|Pet not found|No Content|
|405|Validation exception|No Content|


### Consumes

* application/json
* application/xml

### Produces

* application/json
* application/xml

### Tags

* pet

### Add a new pet to the store
```
POST /pets
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|BodyParameter|body|Pet object that needs to be added to the store|false|Pet|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|405|Invalid input|No Content|


### Consumes

* application/json
* application/xml

### Produces

* application/json
* application/xml

### Tags

* pet

### Finds Pets by status
```
GET /pets/findByStatus
```

### Description

Multiple status values can be provided with comma seperated strings

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|QueryParameter|status|Status values that need to be considered for filter|false|multi string array|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|successful operation|Pet array|
|400|Invalid status value|No Content|


### Produces

* application/json
* application/xml

### Tags

* pet

### Finds Pets by tags
```
GET /pets/findByTags
```

### Description

Muliple tags can be provided with comma seperated strings. Use tag1, tag2, tag3 for testing.

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|QueryParameter|tags|Tags to filter by|false|multi string array|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|successful operation|Pet array|
|400|Invalid tag value|No Content|


### Produces

* application/json
* application/xml

### Tags

* pet

### Find pet by ID
```
GET /pets/{petId}
```

### Description

Returns a pet when ID < 10.  ID > 10 or nonintegers will simulate API error conditions

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|PathParameter|petId|ID of pet that needs to be fetched|true|integer (int64)|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|successful operation|Pet|
|400|Invalid ID supplied|No Content|
|404|Pet not found|No Content|


### Produces

* application/json
* application/xml

### Tags

* pet

### Deletes a pet
```
DELETE /pets/{petId}
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|HeaderParameter|api_key||true|string|
|PathParameter|petId|Pet id to delete|true|integer (int64)|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|400|Invalid pet value|No Content|


### Produces

* application/json
* application/xml

### Tags

* pet

### Updates a pet in the store with form data
```
POST /pets/{petId}
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|PathParameter|petId|ID of pet that needs to be updated|true|string|
|FormDataParameter|name|Updated name of the pet|true|string|
|FormDataParameter|status|Updated status of the pet|true|string|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|405|Invalid input|No Content|


### Consumes

* application/x-www-form-urlencoded

### Produces

* application/json
* application/xml

### Tags

* pet

### Place an order for a pet
```
POST /stores/order
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|BodyParameter|body|order placed for purchasing the pet|false|Order|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|successful operation|Order|
|400|Invalid Order|No Content|


### Produces

* application/json
* application/xml

### Tags

* store

### Find purchase order by ID
```
GET /stores/order/{orderId}
```

### Description

For valid response try integer IDs with value <= 5 or > 10. Other values will generated exceptions

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|PathParameter|orderId|ID of pet that needs to be fetched|true|string|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|successful operation|Order|
|400|Invalid ID supplied|No Content|
|404|Order not found|No Content|


### Produces

* application/json
* application/xml

### Tags

* store

### Delete purchase order by ID
```
DELETE /stores/order/{orderId}
```

### Description

For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers will generate API errors

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|PathParameter|orderId|ID of the order that needs to be deleted|true|string|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|400|Invalid ID supplied|No Content|
|404|Order not found|No Content|


### Produces

* application/json
* application/xml

### Tags

* store

### Create user
```
POST /users
```

### Description

This can only be done by the logged in user.

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|BodyParameter|body|Created user object|false|User|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|default|successful operation|No Content|


### Produces

* application/json
* application/xml

### Tags

* user

### Creates list of users with given input array
```
POST /users/createWithArray
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|BodyParameter|body|List of user object|false|User array|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|default|successful operation|No Content|


### Produces

* application/json
* application/xml

### Tags

* user

### Creates list of users with given input array
```
POST /users/createWithList
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|BodyParameter|body|List of user object|false|User array|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|default|successful operation|No Content|


### Produces

* application/json
* application/xml

### Tags

* user

### Logs user into the system
```
GET /users/login
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|QueryParameter|username|The user name for login|false|string|
|QueryParameter|password|The password for login in clear text|false|string|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|successful operation|string|
|400|Invalid username/password supplied|No Content|


### Produces

* application/json
* application/xml

### Tags

* user

### Logs out current logged in user session
```
GET /users/logout
```

### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|default|successful operation|No Content|


### Produces

* application/json
* application/xml

### Tags

* user

### Get user by user name
```
GET /users/{username}
```

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|PathParameter|username|The name that needs to be fetched. Use user1 for testing.|true|string|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|200|successful operation|User|
|400|Invalid username supplied|No Content|
|404|User not found|No Content|


### Produces

* application/json
* application/xml

### Tags

* user

### Updated user
```
PUT /users/{username}
```

### Description

This can only be done by the logged in user.

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|PathParameter|username|name that need to be deleted|true|string|
|BodyParameter|body|Updated user object|false|User|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|400|Invalid user supplied|No Content|
|404|User not found|No Content|


### Produces

* application/json
* application/xml

### Tags

* user

### Delete user
```
DELETE /users/{username}
```

### Description

This can only be done by the logged in user.

### Parameters
|Type|Name|Description|Required|Schema|
|----|----|----|----|----|
|PathParameter|username|The name that needs to be deleted|true|string|


### Responses
|HTTP Code|Description|Schema|
|----|----|----|
|400|Invalid username supplied|No Content|
|404|User not found|No Content|


### Produces

* application/json
* application/xml

### Tags

* user

