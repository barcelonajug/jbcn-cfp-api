jbcn-cfp-api
============

Operations
----------
**Login**


url: /login
method: POST
authenticated: no

payload:

```
{
    "username":"username_to_login",
    "password":"password_to_login"
}
```

response:

```
{
    "status": true,
    "error": null,
    "data": {
        "token": "JWT_BASE64_TOKEN"
    }
    
}
```

errors:
   'error.auth.username_mandatory'
   'error.auth.password_mandatory'
  
**Logout**

url: /logout
method: GET
authenticated: no
response:

```json
{
    "status": true,
    "error": null
    
}
```


**User Search**

url: /user/search
method: POST
authenticated: yes

payload:

```json
{
	"term":"term_of_search",
	"size":1,
	"page":0,
	"sort":"column_osrt",
	"asc":true
}
```

response:
```json
{
  "status":true,
  "error": null,
  "data": {
    "size":10,
    "page":0,
    "sort":"column_sort",
    "total": 200,
    "totalPages": "20",
    "items": [
      {
        "_id":"USERID0",
        "username""username0",
        
      }
    ]
  }
}
```


