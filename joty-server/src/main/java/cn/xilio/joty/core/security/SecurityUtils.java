package cn.xilio.joty.core.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SecurityUtils {
    /**
     * 获取当前登录用户ID
     *
     * @return 用户唯一标识
     */
    public static String getLoginIdAsString() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!ObjectUtils.isEmpty(authentication) && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();

            }
        }
        return null;
    }

    public static TokenInfo login(Object id) {
        String tokenValue = createLoginSession(id);
        return createTokenInfo(id, tokenValue);
    }

    public static SecurityProperties getConfig() {
        return SpringHelper.getBean(SecurityProperties.class);
    }

    private static TokenInfo createTokenInfo(Object id, String tokenValue) {
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setLoginId(id);
        tokenInfo.setTokenName(getConfig().getTokenName());
        tokenInfo.setTokenValue(tokenValue);
        tokenInfo.setTokenTimeout(getConfig().getTimeout());
        return tokenInfo;
    }

    private static String createLoginSession(Object id) {
        checkLoginId(id);
        String tokenValue = UUID.randomUUID().toString().replaceAll("-", ""); // 生成一个随机字符串
        String tokenName = getTokenName();
        StringRedisTemplate redisTemplate = SpringHelper.getBean(StringRedisTemplate.class);
        String key = tokenName + ":login:token:" + tokenValue;
        redisTemplate.opsForValue().set(key, id.toString(), getConfig().getActiveTimeout(), TimeUnit.SECONDS);
        return tokenValue;
    }

    private static void checkLoginId(Object id) {
        if (ObjectUtils.isEmpty(id) && !StringUtils.hasText(id.toString())) {
            throw new IllegalArgumentException("登陆id不能为空");
        }

    }

    public static String getTokenValue() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(attributes)) {
            throw new IllegalArgumentException("当前请求不存在");
        }
        HttpServletRequest request = attributes.getRequest();
        String tokenName = getTokenName();
        String tokenValue = request.getHeader(tokenName);
        if (!StringUtils.hasText(tokenValue)) {
            return null;
        }
        if (StringUtils.hasText(getTokenPrefix()) && !tokenValue.startsWith(getTokenPrefix() + " ")) {
            throw new IllegalArgumentException("非法的token");
        }
        if (StringUtils.hasText(getTokenPrefix()) && tokenValue.startsWith(getTokenPrefix() + " ")) {
            tokenValue = tokenValue.substring(getTokenPrefix().length() + 1);
        }
        return tokenValue;
    }

    public static void logout() {
        String tokenValue = getTokenValue();
        String tokenName = getTokenName();
        String key = tokenName + ":login:token:" + tokenValue;
        StringRedisTemplate redisTemplate = SpringHelper.getBean(StringRedisTemplate.class);
        redisTemplate.delete(key);
    }

    public static String getTokenName() {
        return getConfig().getTokenName();
    }

    public static String getTokenPrefix() {
        return getConfig().getTokenPrefix();
    }

    public static boolean isLogin() {
        String tokenValue = getTokenValue();
        if (tokenValue == null) {
            return false;
        }
        StringRedisTemplate redisTemplate = SpringHelper.getBean(StringRedisTemplate.class);
        String key = getTokenName() + ":login:token:" + tokenValue;
        String loginId = redisTemplate.opsForValue().get(key);
        return StringUtils.hasText(loginId);
    }

    /**
     * 获取用户的角色列表
     *
     * @return 角色列表
     */
    public static List<String> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_")) // 过滤出角色
                .map(role -> role.substring(5)) // 去除"ROLE_"前缀（5个字符）
                .collect(Collectors.toList());

    }

    /**
     * 从Security上下文中获取当前用户的权限列表（非角色权限）
     *
     * @return 权限列表（如 ["user:read", "order:delete"]）
     */
    public static List<String> getPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_")) // 过滤掉角色
                .collect(Collectors.toList());
    }
}
