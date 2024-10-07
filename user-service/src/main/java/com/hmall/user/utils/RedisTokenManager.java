package com.hmall.user.utils;


import com.hmall.user.config.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTokenManager {

    private static final String TOKEN_KEY_PREFIX = "token:";
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    @Autowired
    public RedisTokenManager(StringRedisTemplate stringRedisTemplate, JwtProperties jwtProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 存储Token
     *
     * @param userId 用户ID
     * @param token  Token
     */
    public void setToken(Long userId, String token) {
        String key = TOKEN_KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key, token, jwtProperties.getTokenTTL());
    }

    /**
     * 获取Token
     *
     * @param userId 用户ID
     * @return Token
     */
    public String getToken(Long userId) {
        String key = TOKEN_KEY_PREFIX + userId;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 更新Token有效期
     */
    public void updateToken(Long userId) {
        String key = TOKEN_KEY_PREFIX + userId;
        stringRedisTemplate.expire(key, jwtProperties.getTokenTTL());
    }
    /**
     * 删除Token
     *
     * @param userId 用户ID
     */
    public void deleteToken(String userId) {
        String key = TOKEN_KEY_PREFIX + userId;
        stringRedisTemplate.delete(key);
    }
}
