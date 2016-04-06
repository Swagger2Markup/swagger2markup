
<a name="definitions"></a>
## Definitions

<a name="category"></a>
### Category

|Name|Description|Schema|Default|Example|
|---|---|---|---|---|
|**id**  <br>*optional*||integer(int64)|||
|**name**  <br>*optional*||string|||


<a name="order"></a>
### Order

|Name|Description|Schema|Default|Example|
|---|---|---|---|---|
|**complete**  <br>*optional*||boolean|||
|**id**  <br>*optional*||integer(int64)|||
|**petId**  <br>*optional*||integer(int64)|||
|**quantity**  <br>*optional*||integer(int32)|||
|**shipDate**  <br>*optional*||string(date-time)|||
|**status**  <br>*optional*|Order Status|enum (Ordered, Cancelled)|||


<a name="pet"></a>
### Pet

|Name|Description|Schema|Default|Example|
|---|---|---|---|---|
|**category**  <br>*optional*||[Category](#category)|||
|**id**  <br>*optional*||integer(int64)|||
|**name**  <br>*required*||string||"doggie"|
|**photoUrls**  <br>*required*||string array|||
|**status**  <br>*optional*|pet status in the store,|enum (Dead, Alive)|||
|**tags**  <br>*optional*||[Tag](#tag) array|||


<a name="tag"></a>
### Tag

|Name|Description|Schema|Default|Example|
|---|---|---|---|---|
|**id**  <br>*optional*||integer(int64)|||
|**name**  <br>*optional*||string|||


<a name="user"></a>
### User

|Name|Description|Schema|Default|Example|
|---|---|---|---|---|
|**email**  <br>*optional*||string|||
|**firstName**  <br>*optional*||string|||
|**id**  <br>*optional*||integer(int64)|||
|**lastName**  <br>*optional*||string|||
|**password**  <br>*optional*||string|||
|**phone**  <br>*optional*||string|||
|**userStatus**  <br>*optional*|User Status|integer(int32)|||
|**username**  <br>*optional*||string|||



