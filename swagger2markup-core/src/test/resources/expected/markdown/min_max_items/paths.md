
<a name="paths"></a>
## Paths

<a name="findpetsbystatus"></a>
### Finds Pets by status
```
GET /pets/findByStatus
```


#### Description
Multiple status values can be provided with comma seperated strings


#### Parameters

|Type|Name|Description|Schema|
|---|---|---|---|
|**Query**|**status**  <br>*optional*|Status values that need to be considered for filter  <br>**Min count** : 1<br>**Max count** : 10|< string > array(multi)|


#### Responses

|HTTP Code|Description|Schema|
|---|---|---|
|**200**|successful operation  <br>**Headers** :   <br>`X-Rate-Limit-Limit` (integer) : The number of allowed requests in the current period.  <br>`X-Rate-Limit-Remaining` (integer) : The number of remaining requests in the current period.  <br>`X-Rate-Limit-Reset` (integer) : The number of seconds left in the current period.|< [Pet](#pet) > array|
|**400**|Invalid status value|No Content|


#### Produces

* `application/json`
* `application/xml`


#### Tags

* pet


#### Security

|Type|Name|Scopes|
|---|---|---|
|**oauth2**|**[petstore_auth](#petstore_auth)**|write_pets,read_pets|



