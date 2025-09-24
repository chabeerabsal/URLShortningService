package com.example.URL.Shortner.Service.Service;

import com.example.URL.Shortner.Service.Model.UrlMapping;
import com.example.URL.Shortner.Service.Repository.UrlRepository;
import com.example.URL.Shortner.Service.Util.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UrlService {
    private final UrlRepository urlRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlService(UrlRepository urlRepository, StringRedisTemplate redisTemplate) {
        this.urlRepository = urlRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public String shortenUrl(String longUrl) {
        // Save entity first to get auto-generated ID
        UrlMapping mapping = new UrlMapping();
        mapping.setLongUrl(longUrl);
        UrlMapping saved = urlRepository.saveAndFlush(mapping);

        // Generate short code from ID
        String shortCode = Base62.encode(saved.getId());
        saved.setShortCode(shortCode);
        urlRepository.save(saved);

        // Store in Redis for fast lookup
        redisTemplate.opsForValue().set(shortCode, longUrl, 1, TimeUnit.DAYS);

        return baseUrl + "/" + shortCode;
    }

    public String getLongUrl(String shortCode) {
        // 1️⃣ Check Redis cache first
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);
        if (cachedUrl != null) {
            System.out.println("Cache HIT for " + shortCode);
            return cachedUrl;
        }

        // 2️⃣ Cache miss → query DB
        Optional<UrlMapping> mapping = urlRepository.findByShortCode(shortCode);
        if (mapping.isPresent()) {
            String longUrl = mapping.get().getLongUrl();

            // 3️⃣ Populate Redis for future requests
            redisTemplate.opsForValue().set(shortCode, longUrl, 1, TimeUnit.DAYS);

            System.out.println("Cache MISS → DB HIT for " + shortCode);
            return longUrl;
        }

        // Not found in DB either
        return null;
    }
}
