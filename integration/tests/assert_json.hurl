GET http://localhost:8000/assert-json
HTTP/1.0 200
[Asserts]
jsonpath "$.success" equals false
jsonpath "$.success" equals false
jsonpath "$.errors" countEquals 2
jsonpath "$.warnings" countEquals 0
jsonpath "$.toto" not exists
jsonpath "$.warnings" exists
jsonpath "$.warnings" exists
jsonpath "$.errors[0]" exists
jsonpath "$.errors[0].id" equals "error1"
jsonpath "$.errors[0]['id']" equals "error1"
jsonpath "$.duration" equals 1.5

{
  "success": false,
  "errors": [{"id":"error1"},{"id":"error2"}],
  "warnings": [],
  "duration": 1.5
}

GET http://localhost:8000/assert-json/index
HTTP/* 200
[Captures]
index: body

GET http://localhost:8000/assert-json
HTTP/1.0 200
[Asserts]
jsonpath "$.errors[{{index}}].id" equals "error2"
