package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.dto.UserDTO;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    // ServiceImpl<UserMapper, User> 是 mybatis-plus 提供的
    @Override
    public Result sendCode(String phone, HttpSession session, Map<String, String> verificationCodes) {
        // 1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2. 如果不符合，返回错误信息
            return Result.fail("invalid phone number!");
        }

        // 3. 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4. 保存验证码到 session
        System.out.println(phone + "===" + code);
        verificationCodes.put(phone, code);
        session.setAttribute(phone, code);

        // 5. 返回验证码
        log.debug("=== 已在 session 保存验证码 " + code + " ===");

        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session, Map<String, String> verificationCodes) {
        // 1. 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("invalid phone number!");
        }
        // 检查是否偷换手机号
        if (!verificationCodes.containsKey(phone)) {
            return Result.fail("incompatible phone number!");
        }

        // 2. 校验验证码
        // 从 session 取出来
        Object cacheCode = session.getAttribute(phone);
        String userCode = loginForm.getCode();
        // 校验验证码是否正确
        if (cacheCode == null || !cacheCode.toString().equals(userCode)) {
            // 不一致
            return Result.fail("invalid verification code!");
        }

        // 3. 根据手机号查用户
        // select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        // 4. 不存在 -> 创建
        if (user == null) {
            user = createUserWithPhone(phone);
        }

        // 5. 存在 -> 保存用户信息到 session
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));

        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        // 1. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomNumbers(10));

        // 2. 保存用户
        save(user);

        return user;
    }


}
