# third-party-api-server

## Prerequisite
### Connect to mysql
- Run ControlSysThirdPartyApiServerApplication.java


### Hint
* server port: 8083
* API prefix: /open/api/v1


### Swagger API
http://localhost:8083/open/api/v1/swagger-ui.html

### How to Run?
## add request header
A sample:
```
    Content-Type:application/json
    apiKey:1
    adminUuid:1
    timestamp:1635941486166
    sign:7uy4oIHS06AFTzEBwKV7vIbDces=
```
How to get timestamp, api and sign?
 Run SignUtilTest.java#testSign, copy output:

 for example :
```
outParams: {"apiKey":"1","sign":"7uy4oIHS06AFTzEBwKV7vIbDces=","timestamp":"1635941486166"}
```
## GET localhost:8081/open/api/v1/alertData/findAll

### If fetch a request failed, please check console. It will out put the sign in develop phase.