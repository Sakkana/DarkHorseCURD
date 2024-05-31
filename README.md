# DarkHorseCURD
> 克隆自：https://github.com/cs001020/hmdp

写着玩玩。

\ 😄😞🐷 / 

### nginx 配置
nginx.conf 地址：
我的配置：
```conf
user root;
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/json;
    types_hash_bucket_size 128;

    sendfile        on;

    keepalive_timeout  65;

    server {
        listen       7777;
        server_name  localhost;
        
        # 指定前端项目所在的位置
        location / {
            root   /YourLocation;
            index  index.html index.htm;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }


        location /api {
            default_type  application/json;
            #internal;
            keepalive_timeout   30s;
            keepalive_requests  1000;
            #支持keep-alive
            proxy_http_version 1.1;
            rewrite /api(/.*) $1 break;
            proxy_pass_request_headers on;
            #more_clear_input_headers Accept-Encoding;
            proxy_next_upstream error timeout;
            proxy_pass http://127.0.0.1:9999;
        }
    }

    upstream backend {
        server 127.0.0.1:9999 max_fails=5 fail_timeout=10s weight=1;
    }
}
```

##### 2024.5.28
手机号码校验与验证码生成。

##### 2024.5.30
登陆拦截器。

配置 config.MvcConfig.addInterceptors 以及排除名单。

配置 utils.LoginInterceptor，这个类`继承 HandlerInterceptor`，重载 `preHandle` 和 `afterCompletion`。

##### 2024.5.31
1. 使用 redis 代替 session 实现用户 token 的存储和更新服务。

超过 30 分钟不登陆就从 redis 删除 token。

刚登录的时候后台会生成一个 token：
```java
String code = RandomUtil.randomNumbers(6);
```
后面每次请求都会在 `authorization` 字段带上这个 token 鉴权。

redis 的 String 和 Hash 数据结构。


