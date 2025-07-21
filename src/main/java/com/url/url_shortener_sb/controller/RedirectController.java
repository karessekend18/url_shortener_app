package com.url.url_shortener_sb.controller;

import com.url.url_shortener_sb.model.UrlMapping;
import com.url.url_shortener_sb.service.UrlMappingService;
import org.apache.coyote.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedirectController {

    private UrlMappingService urlMappingService;

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {
        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);
        if(urlMapping != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", urlMapping.getOriginalUrl());
            return ResponseEntity.status(302).headers(headers).build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
}}
