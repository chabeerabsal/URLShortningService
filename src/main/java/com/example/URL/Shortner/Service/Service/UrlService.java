package com.example.URL.Shortner.Service.Service;



import com.example.URL.Shortner.Service.Model.UrlMapping;
import com.example.URL.Shortner.Service.Repository.UrlRepository;
import com.example.URL.Shortner.Service.Util.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlService {
    private final UrlRepository urlRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
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

        return baseUrl + "/" + shortCode;
    }

    public String getLongUrl(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(UrlMapping::getLongUrl)
                .orElse(null);
    }
}

