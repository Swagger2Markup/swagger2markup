
<a name="definitions"></a>
## Definitions

<a name="category"></a>
### Category

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|id||false|integer(int64)|||
|name||false|string|||


<a name="order"></a>
### Order

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|complete||false|boolean|||
|id||false|integer(int64)|||
|petId||false|integer(int64)|||
|quantity||false|integer(int32)|||
|shipDate||false|string(date-time)|||
|status|Order Status|false|enum (Ordered, Cancelled)|||


<a name="pet"></a>
### Pet

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|category||false|[Category](#category)|||
|id||false|integer(int64)|||
|name||true|string||"doggie"|
|photoUrls||true|string array|||
|status|pet status in the store,|false|enum (Dead, Alive)|||
|tags||false|[Tag](#tag) array|||


<a name="tag"></a>
### Tag

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|id||false|integer(int64)|||
|name||false|string|||


<a name="user"></a>
### User

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|email||false|string|||
|firstName||false|string|||
|id||false|integer(int64)|||
|lastName||false|string|||
|password||false|string|||
|phone||false|string|||
|userStatus|User Status|false|integer(int32)|||
|username||false|string|||



