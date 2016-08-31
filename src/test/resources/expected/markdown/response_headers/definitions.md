
<a name="definitions"></a>
## Definitions

<a name="category"></a>
### Category

|Name|Schema|
|---|---|
|**id**  <br>*optional*|integer(int64)|
|**name**  <br>*optional*|string|


<a name="pet"></a>
### Pet

|Name|Description|Schema|
|---|---|---|
|**category**  <br>*optional*||[Category](#category)|
|**id**  <br>*optional*||integer(int64)|
|**name**  <br>*required*|**Example** : `"doggie"`|string|
|**photoUrls**  <br>*required*||< string > array|
|**status**  <br>*optional*|pet status in the store,|enum (Dead, Alive)|
|**tags**  <br>*optional*||< [Tag](#tag) > array|


<a name="tag"></a>
### Tag

|Name|Schema|
|---|---|
|**id**  <br>*optional*|integer(int64)|
|**name**  <br>*optional*|string|



