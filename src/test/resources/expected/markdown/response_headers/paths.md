
<a name="paths"></a>
## Paths

<a name="getpetbyid"></a>
### Find pet by ID
```
GET /pets/{petId}
```


#### Description
Returns a pet when ID < 10.  ID > 10 or nonintegers will simulate API error conditions


#### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Path**|**petId**  <br>*required*|ID of pet that needs to be fetched|integer(int64)|


#### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation  <br>**Headers** :   <br>`X-Rate-Limit-Limit` (string) : The number of allowed requests in the current period. **Default** : `"20"`  <br>`X-Rate-Limit-Remaining` (integer) : The number of remaining requests in the current period. +<br>This is an important value !.  <br>`X-Rate-Limit-Reset` (boolean) :  **Default** : `false`|[Pet](#pet)|
|**400**|Invalid ID supplied|No Content|
|**404**|Page not found (override)  <br>**Headers** :   <br>`X-Rate-Limit-Limit` (string) : The number of allowed requests in the current period. **Default** : `"20"`  <br>`X-Rate-Limit-Remaining` (integer) : The number of remaining requests in the current period. +<br>This is an important value !.  <br>`X-Rate-Limit-Reset` (boolean) :  **Default** : `false`  <br>`Nothing` (integer)  <br>`DottedDescription` (string) : This description is terminated with a dot. **Default** : `"20"`|No Content|


#### Produces

* `application/json`
* `application/xml`


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|**apiKey**|**[api_key](#api_key)**||
|**oauth2**|**[petstore_auth](#petstore_auth)**|write_pets,read_pets|



