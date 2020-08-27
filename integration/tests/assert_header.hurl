GET http://localhost:8000/assert-header
HTTP/1.0 200
Content-Type: text/html; charset=utf-8
Set-Cookie: cookie1=value1; Path=/
Set-Cookie: cookie2=value2; Path=/
[Asserts]
header "Custom" not exists
header "Content-Type" exists
header "Header1" equals "value1"
header "Set-Cookie" exists
header "Set-Cookie" countEquals 3
# Not supported yet, prodicate will be includes
#header "Set-Cookie" contains "cookie1=value1; Path=/"
#header "Set-Cookie" not contains "cookie4=value4; Path=/"