# v1.0.41

## ⭐ New Features

- Add `--to-entry` cli option (<https://hurl.dev/docs/man-page.html#to-entry>)
- Add `--compress` cli option (<https://hurl.dev/docs/man-page.html#compress>)
- Add decoding support for Brotli, GZIP, Deflate encoder
- Add `--user` cli option (<https://hurl.dev/docs/man-page.html#user>)
- Add `--connect-timeout` cli option (<https://hurl.dev/docs/man-page.html#connect-timeout>)
- Add `--max-time` cli option (<https://hurl.dev/docs/man-page.html#max-time>)
- Simplify log management and remove sl4j dependency

## 🐞 Bug Fixes

- Fix predicates with not qualifier <https://github.com/Orange-OpenSource/hurl/issues/39> 
- Remove `Content-Length` request header when request body is empty
- Output only last HTTP response headers when using `--include` option <https://github.com/Orange-OpenSource/hurl/issues/59>
 
## 📔 Documentation

## 🔨 Dependency Upgrades

- Upgrade to Kotlin 1.4.10
- Upgrade to Junit 5.7.0
- Upgrade to SL4J 1.7.30
- Upgrade to Jackson 2.11.3
- Upgrade to jsoup 1.13.1
- Upgrade to Apache HttpComponents 4.5.13