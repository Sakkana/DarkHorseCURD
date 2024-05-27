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