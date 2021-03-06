package com.qs.game.aspect;

import com.qs.game.annotation.IgnoreSecurity;
import com.qs.game.auth.TokenManager;
import com.qs.game.constant.Env;
import com.qs.game.constant.SecurityConstants;
import com.qs.game.utils.WebContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Created by zun.wei on 2018/8/14 12:37.
 * Description: 安全检查切面(是否登录检查)   通过验证Token维持登录状态
 */
@Slf4j
@Aspect
@Profile({Env.DEV, Env.LOCAL})
@Configuration
public class SecurityAspectDevLocal {

    @Resource(name = "redisTokenManager")
    private TokenManager tokenManager;

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)"
    + " || @annotation(org.springframework.web.bind.annotation.PostMapping)"
    + " || @annotation(org.springframework.web.bind.annotation.GetMapping)"
    + " || @annotation(org.springframework.web.bind.annotation.PutMapping)"
    + " || @annotation(org.springframework.web.bind.annotation.DeleteMapping)"
    )
    public Object execute(ProceedingJoinPoint pjp) throws Throwable {
        // 从切点上获取目标方法
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        log.info("methodSignature : " + methodSignature);
        Method method = methodSignature.getMethod();
        log.info("Method : " + method.getName() + " : "
                + method.isAnnotationPresent(IgnoreSecurity.class));
        // 若目标方法忽略了安全性检查,则直接调用目标方法
        Object[] args = pjp.getArgs();
        if (method.isAnnotationPresent(IgnoreSecurity.class)) {
            return Objects.nonNull(args) ? pjp.proceed(args) : pjp.proceed();
        }

        // 从 request header 中获取当前 token
        String token = WebContextUtil.getRequest().getHeader(SecurityConstants.DEFAULT_TOKEN_NAME);
        log.info("SecurityAspectDevLocal -- execute -- token --::{}", token);
        Cookie[] cookies = WebContextUtil.getRequest().getCookies();
        if (Objects.nonNull(cookies)) {
            for (int i = 0; i < cookies.length; i++) {
                System.out.println("cookies = " + cookies[i].getName() + "  -- " + cookies[i].getValue());

            }
        }
        // 检查 token 有效性
        // 调用目标方法
        return pjp.proceed();
    }

}
