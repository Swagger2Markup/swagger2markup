## Definitions
### User
|Name|Description|Schema|Required|
|----|----|----|----|
|id||integer (int64)|false|
|username||string|false|
|firstName||string|false|
|lastName||string|false|
|email||string|false|
|password||string|false|
|phone||string|false|
|userStatus|User Status|integer (int32)|false|


### Category
|Name|Description|Schema|Required|
|----|----|----|----|
|id||integer (int64)|false|
|name||string|false|


### Pet
|Name|Description|Schema|Required|
|----|----|----|----|
|id||integer (int64)|false|
|category||Category|false|
|name||string|true|
|photoUrls||string array|true|
|tags||Tag array|false|
|status|pet status in the store|string|false|


### Tag
|Name|Description|Schema|Required|
|----|----|----|----|
|id||integer (int64)|false|
|name||string|false|


### Order
|Name|Description|Schema|Required|
|----|----|----|----|
|id||integer (int64)|false|
|petId||integer (int64)|false|
|quantity||integer (int32)|false|
|shipDate||string (date-time)|false|
|status|Order Status|string|false|
|complete||boolean|false|


