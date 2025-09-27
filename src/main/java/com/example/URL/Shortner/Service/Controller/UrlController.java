package com.example.URL.Shortner.Service.Controller;




import com.example.URL.Shortner.Service.Service.UrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody Map<String, String> body) {
        String longUrl = body.get("longUrl");
        if (longUrl == null || longUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "longUrl is required"));
        }
        String shortUrl = urlService.shortenUrl(longUrl);
        return ResponseEntity.ok(Map.of("shortUrl", shortUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        String longUrl = urlService.getLongUrl(shortCode);
        if (longUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(longUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

