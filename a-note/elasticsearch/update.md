"max_docs": 9 控制更新的数量
POST single_object_data_2022/_update_by_query
```json
{
  "query": {
    "match": {
      "status": -1
    }
  },
  "script": {
    "source": "ctx._source['status'] =0"
  },
  "max_docs": 9 
}
```