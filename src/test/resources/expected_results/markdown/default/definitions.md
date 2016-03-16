
<a name="definitions"></a>
## Definitions

### Category

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|id||false|integer(int64)|||
|name||false|string|||


### Order

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|complete||false|boolean|||
|id||false|integer(int64)|||
|petId||false|integer(int64)|||
|quantity||false|integer(int32)|||
|shipDate||false|string(date-time)|||
|status|Order Status|false|enum (Ordered, Cancelled)|||


### Pet

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|category||false|[Category](#category)|||
|id||false|integer(int64)|||
|name||true|string||"doggie"|
|photoUrls||true|string array|||
|status|pet status in the store,|false|enum (Dead, Alive)|||
|tags||false|[Tag](#tag) array|||


### Tag

|Name|Description|Required|Schema|Default|Example|
|---|---|---|---|---|---|
|id||false|integer(int64)|||
|name||false|string|||


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



