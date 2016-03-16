
<a name="paths"></a>
## Paths

<a name="addpet"></a>
### Add a new pet to the store
```
POST /pets
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Body|body|Pet object that needs to be added to the store|false|[Pet](#pet)||


#### Responses

##### HTTP Code 405

|HTTP Code|Description|Schema|
|---|---|---|
|405|Invalid input|No Content|


#### Consumes

* application/json
* application/xml


#### Produces

* application/json
* application/xml


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|oauth2|[petstore_auth](#petstore_auth)|write_pets,read_pets|


<a name="updatepet"></a>
### Update an existing pet
```
PUT /pets
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Body|body|Pet object that needs to be added to the store|false|[Pet](#pet)||


#### Responses

##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid ID supplied|No Content|


##### HTTP Code 404

|HTTP Code|Description|Schema|
|---|---|---|
|404|Pet not found|No Content|


##### HTTP Code 405

|HTTP Code|Description|Schema|
|---|---|---|
|405|Validation exception|No Content|


#### Consumes

* application/json
* application/xml


#### Produces

* application/json
* application/xml


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|oauth2|[petstore_auth](#petstore_auth)|write_pets,read_pets|


<a name="findpetsbystatus"></a>
### Finds Pets by status
```
GET /pets/findByStatus
```


#### Description
Multiple status values can be provided with comma seperated strings


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Query|status|Status values that need to be considered for filter|false|multi string array||


#### Responses

##### HTTP Code 200

|HTTP Code|Description|Schema|
|---|---|---|
|200|successful operation|[Pet](#pet) array|

**Headers**

|Name|Description|Schema|Default|
|---|---|---|---|
|X-Rate-Limit-Limit|The number of allowed requests in the current period|integer||
|X-Rate-Limit-Remaining|The number of remaining requests in the current period|integer||
|X-Rate-Limit-Reset|The number of seconds left in the current period|integer||


##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid status value|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|oauth2|[petstore_auth](#petstore_auth)|write_pets,read_pets|


<a name="findpetsbytags"></a>
### Finds Pets by tags
```
GET /pets/findByTags
```


#### Description
Muliple tags can be provided with comma seperated strings. Use tag1, tag2, tag3 for testing.


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Query|tags|Tags to filter by|false|multi string array||


#### Responses

##### HTTP Code 200

|HTTP Code|Description|Schema|
|---|---|---|
|200|successful operation|[Pet](#pet) array|

**Headers**

|Name|Description|Schema|Default|
|---|---|---|---|
|X-Rate-Limit-Limit|The number of allowed requests in the current period|integer||
|X-Rate-Limit-Remaining|The number of remaining requests in the current period|integer||
|X-Rate-Limit-Reset|The number of seconds left in the current period|integer||


##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid tag value|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|oauth2|[petstore_auth](#petstore_auth)|write_pets,read_pets|


<a name="updatepetwithform"></a>
### Updates a pet in the store with form data
```
POST /pets/{petId}
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Path|petId|ID of pet that needs to be updated|true|string||
|FormData|name|Updated name of the pet|true|string||
|FormData|status|Updated status of the pet|true|string||


#### Responses

##### HTTP Code 405

|HTTP Code|Description|Schema|
|---|---|---|
|405|Invalid input|No Content|


#### Consumes

* application/x-www-form-urlencoded


#### Produces

* application/json
* application/xml


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|oauth2|[petstore_auth](#petstore_auth)|write_pets,read_pets|


<a name="getpetbyid"></a>
### Find pet by ID
```
GET /pets/{petId}
```


#### Description
Returns a pet when ID < 10.  ID > 10 or nonintegers will simulate API error conditions


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Path|petId|ID of pet that needs to be fetched|true|integer(int64)||


#### Responses

##### HTTP Code 200

|HTTP Code|Description|Schema|
|---|---|---|
|200|successful operation|[Pet](#pet)|

**Headers**

|Name|Description|Schema|Default|
|---|---|---|---|
|X-Rate-Limit-Limit|The number of allowed requests in the current period|integer||
|X-Rate-Limit-Remaining|The number of remaining requests in the current period|integer||
|X-Rate-Limit-Reset|The number of seconds left in the current period|integer||


##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid ID supplied|No Content|


##### HTTP Code 404

|HTTP Code|Description|Schema|
|---|---|---|
|404|Pet not found|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|apiKey|[api_key](#api_key)||
|oauth2|[petstore_auth](#petstore_auth)|write_pets,read_pets|


<a name="deletepet"></a>
### Deletes a pet
```
DELETE /pets/{petId}
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Header|api_key||true|string||
|Path|petId|Pet id to delete|true|integer(int64)||


#### Responses

##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid pet value|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|oauth2|[petstore_auth](#petstore_auth)|write_pets,read_pets|


<a name="placeorder"></a>
### Place an order for a pet
```
POST /stores/order
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Body|body|order placed for purchasing the pet|false|[Order](#order)||


#### Responses

##### HTTP Code 200

|HTTP Code|Description|Schema|
|---|---|---|
|200|successful operation|[Order](#order)|

**Headers**

|Name|Description|Schema|Default|
|---|---|---|---|
|X-Rate-Limit-Limit|The number of allowed requests in the current period|integer||
|X-Rate-Limit-Remaining|The number of remaining requests in the current period|integer||
|X-Rate-Limit-Reset|The number of seconds left in the current period|integer||


##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid Order|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* store


<a name="getorderbyid"></a>
### Find purchase order by ID
```
GET /stores/order/{orderId}
```


#### Description
For valid response try integer IDs with value <= 5 or > 10. Other values will generated exceptions


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Path|orderId|ID of pet that needs to be fetched|true|string||


#### Responses

##### HTTP Code 200

|HTTP Code|Description|Schema|
|---|---|---|
|200|successful operation|[Order](#order)|

**Headers**

|Name|Description|Schema|Default|
|---|---|---|---|
|X-Rate-Limit-Limit|The number of allowed requests in the current period|integer||
|X-Rate-Limit-Remaining|The number of remaining requests in the current period|integer||
|X-Rate-Limit-Reset|The number of seconds left in the current period|integer||


##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid ID supplied|No Content|


##### HTTP Code 404

|HTTP Code|Description|Schema|
|---|---|---|
|404|Order not found|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* store


<a name="deleteorder"></a>
### Delete purchase order by ID
```
DELETE /stores/order/{orderId}
```


#### Description
For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers will generate API errors


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Path|orderId|ID of the order that needs to be deleted|true|string||


#### Responses

##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid ID supplied|No Content|


##### HTTP Code 404

|HTTP Code|Description|Schema|
|---|---|---|
|404|Order not found|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* store


<a name="createuser"></a>
### Create user
```
POST /users
```


#### Description
This can only be done by the logged in user.


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Body|body|Created user object|false|[User](#user)||


#### Responses

##### HTTP Code default

|HTTP Code|Description|Schema|
|---|---|---|
|default|successful operation|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* user


<a name="createuserswitharrayinput"></a>
### Creates list of users with given input array
```
POST /users/createWithArray
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Body|body|List of user object|false|[User](#user) array||


#### Responses

##### HTTP Code default

|HTTP Code|Description|Schema|
|---|---|---|
|default|successful operation|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* user


<a name="createuserswithlistinput"></a>
### Creates list of users with given input array
```
POST /users/createWithList
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Body|body|List of user object|false|[User](#user) array||


#### Responses

##### HTTP Code default

|HTTP Code|Description|Schema|
|---|---|---|
|default|successful operation|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* user


<a name="loginuser"></a>
### Logs user into the system
```
GET /users/login
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Query|password|The password for login in clear text|false|string||
|Query|username|The user name for login|false|string||


#### Responses

##### HTTP Code 200

|HTTP Code|Description|Schema|
|---|---|---|
|200|successful operation|string|

**Headers**

|Name|Description|Schema|Default|
|---|---|---|---|
|X-Rate-Limit-Limit|The number of allowed requests in the current period|integer||
|X-Rate-Limit-Remaining|The number of remaining requests in the current period|integer||
|X-Rate-Limit-Reset|The number of seconds left in the current period|integer||


##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid username/password supplied|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* user


<a name="logoutuser"></a>
### Logs out current logged in user session
```
GET /users/logout
```


#### Responses

##### HTTP Code default

|HTTP Code|Description|Schema|
|---|---|---|
|default|successful operation|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* user


<a name="getuserbyname"></a>
### Get user by user name
```
GET /users/{username}
```


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Path|username|The name that needs to be fetched. Use user1 for testing.|true|string||


#### Responses

##### HTTP Code 200

|HTTP Code|Description|Schema|
|---|---|---|
|200|successful operation|[User](#user)|

**Headers**

|Name|Description|Schema|Default|
|---|---|---|---|
|X-Rate-Limit-Limit|The number of allowed requests in the current period|integer||
|X-Rate-Limit-Remaining|The number of remaining requests in the current period|integer||
|X-Rate-Limit-Reset|The number of seconds left in the current period|integer||


##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid username supplied|No Content|


##### HTTP Code 404

|HTTP Code|Description|Schema|
|---|---|---|
|404|User not found|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* user


<a name="updateuser"></a>
### Updated user
```
PUT /users/{username}
```


#### Description
This can only be done by the logged in user.


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Path|username|name that need to be deleted|true|string||
|Body|body|Updated user object|false|[User](#user)||


#### Responses

##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid user supplied|No Content|


##### HTTP Code 404

|HTTP Code|Description|Schema|
|---|---|---|
|404|User not found|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* user


<a name="deleteuser"></a>
### Delete user
```
DELETE /users/{username}
```


#### Description
This can only be done by the logged in user.


#### Parameters

|Type|Name|Description|Required|Schema|Default|
|---|---|---|---|---|---|
|Path|username|The name that needs to be deleted|true|string||


#### Responses

##### HTTP Code 400

|HTTP Code|Description|Schema|
|---|---|---|
|400|Invalid username supplied|No Content|


##### HTTP Code 404

|HTTP Code|Description|Schema|
|---|---|---|
|404|User not found|No Content|


#### Produces

* application/json
* application/xml


#### Tags

* user



