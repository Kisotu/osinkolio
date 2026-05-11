package com.musicstore.user.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtDenylistService {

    private static final String DENYLIST_PREFIX = "jwt:denylist:";
    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Add a token to the denylist (for logout).
     * The token will be kept in Redis until it would have naturally expired.
     */
    public void addToDenylist(String token) {
        String key = DENYLIST_PREFIX + token.hashCode();
        long ttlMs = jwtTokenProvider.getAccessTokenExpirationMs();
        redisTemplate.opsForValue().set(key, "revoked", ttlMs, TimeUnit.MILLISECONDS);
        log.debug("Added token to denylist, TTL: {}ms", ttlMs);
    }

    /**
     * Check if a token is in the denylist.
     */
    public boolean isDenylisted(String token) {
        String key = DENYLIST_PREFIX + token.hashCode();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
