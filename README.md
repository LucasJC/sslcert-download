# sslcert-download
A tool for downloading ssl certs using the command line.

### Valid options:
 - url*: URL for which certificates will be downloaded. Required
 - help: print this help.
 - verbose: print more information.
 - proxy-host: host for using a proxy.
 - proxy-port: port for using a proxy.
 - insecure: include to ignore validation errors such as unknown CA errors.
 - out: output dir. Defaults to current dir.
 
 ### Example:
```bash
sslcert-download --url=https://google.com --proxy-host=myproxy --proxy-port=3128 --insecure=true --out=/tmp'
```
