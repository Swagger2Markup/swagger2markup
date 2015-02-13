# Swagger Petstore
This is a sample server Petstore server.

[Learn about Swagger](http://swagger.wordnik.com) or join the IRC channel `#swagger` on irc.freenode.net.

For this sample, you can use the api key `special-key` to test the authorization filters

Version: 1.0.0

## Update an existing pet
```
PUT /pets
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|body|body|Pet object that needs to be added to the store|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|400|Invalid ID supplied|null||404|Pet not found|null||405|Validation exception|null|

### Consumes
* application/json
* application/xml

### Produces
* application/json
* application/xml

## Add a new pet to the store
```
POST /pets
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|body|body|Pet object that needs to be added to the store|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|405|Invalid input|null|

### Consumes
* application/json
* application/xml

### Produces
* application/json
* application/xml

## Finds Pets by status
### Description

Multiple status values can be provided with comma seperated strings

```
GET /pets/findByStatus
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|status|query|Status values that need to be considered for filter|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|200|successful operation|com.wordnik.swagger.models.properties.ArrayProperty@42cc43ea||400|Invalid status value|null|

### Produces
* application/json
* application/xml

## Finds Pets by tags
### Description

Muliple tags can be provided with comma seperated strings. Use tag1, tag2, tag3 for testing.

```
GET /pets/findByTags
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|tags|query|Tags to filter by|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|200|successful operation|com.wordnik.swagger.models.properties.ArrayProperty@3f17a74f||400|Invalid tag value|null|

### Produces
* application/json
* application/xml

## Find pet by ID
### Description

Returns a pet when ID < 10.  ID > 10 or nonintegers will simulate API error conditions

```
GET /pets/{petId}
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|petId|path|ID of pet that needs to be fetched|true|

### Responses
|Code|Description|Schema|
|----|----|----|
|200|successful operation|com.wordnik.swagger.models.properties.RefProperty@2347fe11||400|Invalid ID supplied|null||404|Pet not found|null|

### Produces
* application/json
* application/xml

## Deletes a pet
```
DELETE /pets/{petId}
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|api_key|header||true||petId|path|Pet id to delete|true|

### Responses
|Code|Description|Schema|
|----|----|----|
|400|Invalid pet value|null|

### Produces
* application/json
* application/xml

## Updates a pet in the store with form data
```
POST /pets/{petId}
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|petId|path|ID of pet that needs to be updated|true||name|formData|Updated name of the pet|true||status|formData|Updated status of the pet|true|

### Responses
|Code|Description|Schema|
|----|----|----|
|405|Invalid input|null|

### Consumes
* application/x-www-form-urlencoded

### Produces
* application/json
* application/xml

## Place an order for a pet
```
POST /stores/order
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|body|body|order placed for purchasing the pet|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|200|successful operation|com.wordnik.swagger.models.properties.RefProperty@3115cf36||400|Invalid Order|null|

### Produces
* application/json
* application/xml

## Find purchase order by ID
### Description

For valid response try integer IDs with value <= 5 or > 10. Other values will generated exceptions

```
GET /stores/order/{orderId}
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|orderId|path|ID of pet that needs to be fetched|true|

### Responses
|Code|Description|Schema|
|----|----|----|
|200|successful operation|com.wordnik.swagger.models.properties.RefProperty@11e609ed||400|Invalid ID supplied|null||404|Order not found|null|

### Produces
* application/json
* application/xml

## Delete purchase order by ID
### Description

For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers will generate API errors

```
DELETE /stores/order/{orderId}
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|orderId|path|ID of the order that needs to be deleted|true|

### Responses
|Code|Description|Schema|
|----|----|----|
|400|Invalid ID supplied|null||404|Order not found|null|

### Produces
* application/json
* application/xml

## Create user
### Description

This can only be done by the logged in user.

```
POST /users
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|body|body|Created user object|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|default|successful operation|null|

### Produces
* application/json
* application/xml

## Creates list of users with given input array
```
POST /users/createWithArray
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|body|body|List of user object|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|default|successful operation|null|

### Produces
* application/json
* application/xml

## Creates list of users with given input array
```
POST /users/createWithList
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|body|body|List of user object|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|default|successful operation|null|

### Produces
* application/json
* application/xml

## Logs user into the system
```
GET /users/login
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|username|query|The user name for login|false||password|query|The password for login in clear text|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|200|successful operation|com.wordnik.swagger.models.properties.StringProperty@1349f941||400|Invalid username/password supplied|null|

### Produces
* application/json
* application/xml

## Logs out current logged in user session
```
GET /users/logout
```

### Responses
|Code|Description|Schema|
|----|----|----|
|default|successful operation|null|

### Produces
* application/json
* application/xml

## Get user by user name
```
GET /users/{username}
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|username|path|The name that needs to be fetched. Use user1 for testing.|true|

### Responses
|Code|Description|Schema|
|----|----|----|
|200|successful operation|com.wordnik.swagger.models.properties.RefProperty@fe22a4f||400|Invalid username supplied|null||404|User not found|null|

### Produces
* application/json
* application/xml

## Updated user
### Description

This can only be done by the logged in user.

```
PUT /users/{username}
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|username|path|name that need to be deleted|true||body|body|Updated user object|false|

### Responses
|Code|Description|Schema|
|----|----|----|
|400|Invalid user supplied|null||404|User not found|null|

### Produces
* application/json
* application/xml

## Delete user
### Description

This can only be done by the logged in user.

```
DELETE /users/{username}
```

### Parameters
|Name|Located in|Description|Required|
|----|----|----|----|
|username|path|The name that needs to be deleted|true|

### Responses
|Code|Description|Schema|
|----|----|----|
|400|Invalid username supplied|null||404|User not found|null|

### Produces
* application/json
* application/xml

## Definitions
### User
|Name|Type|Required|
|----|----|----|
|id|integer|false||username|string|false||firstName|string|false||lastName|string|false||email|string|false||password|string|false||phone|string|false||userStatus|integer|false|

### Category
|Name|Type|Required|
|----|----|----|
|id|integer|false||name|string|false|

### Pet
|Name|Type|Required|
|----|----|----|
|id|integer|false||category|ref|false||name|string|true||photoUrls|array|true||tags|array|false||status|string|false|

### Tag
|Name|Type|Required|
|----|----|----|
|id|integer|false||name|string|false|

### Order
|Name|Type|Required|
|----|----|----|
|id|integer|false||petId|integer|false||quantity|integer|false||shipDate|string|false||status|string|false||complete|boolean|false|

