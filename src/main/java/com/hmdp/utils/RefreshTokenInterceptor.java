package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;

    // 必须用构造函数，不能用 autowired 或者 resource 等注解
    // 因为这个对象是手动创建的，不是 Spring 创建的，没人帮忙做依赖注入
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取 session
        // HttpSession session = request.getSession();
        // 获取请求头中的 token
        String token = request.getHeader("authorization");

        // 2. 不存在 -> 拦截
        if (StrUtil.isBlank(token)) {
            // response.setStatus(401);
            // 这一行需要删掉，因为这个第一个拦截器，拦截所有请求
            // 如果没有发现 token，则说明未登录，此时不需要返回错误
            // 只需要放行给第二个拦截器
            return true;
        }

        // 检查所有人所有场景下的 token

        // 3. 获取 session 中的用户
        // Object user = session.getAttribute("user");
        // 获取 redis 中的用户信息
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash()
                .entries(LOGIN_USER_KEY + token);
        if (userMap.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        // 4. 将 redis 中获取的哈希数据转换成 UserDTO
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        // 5. 存在 -> 保存到 ThreadLocal
        UserHolder.saveUser(userDTO);

        // 6. 放行
        // 刷新 token 有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
