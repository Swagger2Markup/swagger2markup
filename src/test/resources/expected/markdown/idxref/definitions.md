
<a name="definitions"></a>
## Definitions

<a name="category"></a>
### Category

|Name|Description|Schema|
|---|---|---|
|**id**  <br>*optional*||integer(int64)|
|**name**  <br>*optional*|The name of the category  <br>**Default** : `"DefaultCategory"`  <br>**Length** : `0 - 255`  <br>**Pattern** : `"[A-Za-zäöüÄÖÜß]{0,255}"`  <br>**Example** : `"FoobarCategory"`|string|


<a name="order"></a>
### Order

|Name|Description|Schema|
|---|---|---|
|**complete**  <br>*optional*||boolean|
|**id**  <br>*optional*||integer(int64)|
|**petId**  <br>*optional*||integer(int64)|
|**quantity**  <br>*optional*|**Default** : `0`  <br>**Minimum value** : `0`  <br>**Maximum value** : `10000`  <br>**Example** : `10`|integer(int32)|
|**shipDate**  <br>*optional*||string(date-time)|
|**status**  <br>*optional*|Order Status|enum (Ordered, Cancelled)|


<a name="pet"></a>
### Pet

|Name|Description|Schema|
|---|---|---|
|**category**  <br>*optional*||[Category](definitions.md#category)|
|**id**  <br>*optional*||integer(int64)|
|**name**  <br>*required*|**Example** : `"doggie"`|string|
|**photoUrls**  <br>*required*||< string > array|
|**status**  <br>*optional*|pet status in the store,|enum (Dead, Alive)|
|**tags**  <br>*optional*||< [Tag](definitions.md#tag) > array|


<a name="tag"></a>
### Tag

|Name|Schema|
|---|---|
|**id**  <br>*optional*|integer(int64)|
|**name**  <br>*optional*|string|


<a name="user"></a>
### User

|Name|Description|Schema|
|---|---|---|
|**email**  <br>*optional*||string|
|**firstName**  <br>*optional*||string|
|**id**  <br>*optional*||integer(int64)|
|**lastName**  <br>*optional*||string|
|**password**  <br>*optional*||string|
|**phone**  <br>*optional*||string|
|**userStatus**  <br>*optional*|User Status|integer(int32)|
|**username**  <br>*optional*||string|



