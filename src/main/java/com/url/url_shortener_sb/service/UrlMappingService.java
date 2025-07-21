package com.url.url_shortener_sb.service;

import com.url.url_shortener_sb.dto.ClickEventDTO;
import com.url.url_shortener_sb.dto.UrlMappingDTO;
import com.url.url_shortener_sb.model.ClickEvent;
import com.url.url_shortener_sb.model.UrlMapping;
import com.url.url_shortener_sb.model.User;
import com.url.url_shortener_sb.repository.ClickEventRepository;
import com.url.url_shortener_sb.repository.UrlMappingRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UrlMappingService {

    private UrlMappingRepository urlMappingRepository;

    private ClickEventRepository clickEventRepository;

    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl(originalUrl);
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);
        return convertToDto(savedUrlMapping);
    }

    public UrlMappingDTO convertToDto(UrlMapping urlMapping) {
        UrlMappingDTO dto = new UrlMappingDTO();
        dto.setId(urlMapping.getId());
        dto.setOriginalUrl(urlMapping.getOriginalUrl());
        dto.setShortUrl(urlMapping.getShortUrl());
        dto.setClickCount(urlMapping.getClickCount());
        dto.setCreatedDate(urlMapping.getCreatedDate());
        dto.setUsername(urlMapping.getUser().getUsername());
        return dto;
    }



    private String generateShortUrl(String originalUrl) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            shortUrl.append(characters.charAt(index));
        }
        return shortUrl.toString();
    }

    public List<UrlMappingDTO> getUrlsByUser(User user) {
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime startDate, LocalDateTime endDate) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null) {
            return clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping, startDate, endDate)
                    .stream()
                    .collect(Collectors.groupingBy(
                            click -> click.getClickDate().toLocalDate(),
                            Collectors.counting()
                    ))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        ClickEventDTO clickEventDTO = new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey());
                        clickEventDTO.setCount(entry.getValue());
                        return clickEventDTO;
                    })
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
        List<UrlMapping> userUrls = urlMappingRepository.findByUser(user);
        if (userUrls.isEmpty()) {
            return Map.of();
        }
        // Convert LocalDate to LocalDateTime for start and end of day
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(userUrls, startDateTime, endDateTime);
        return clickEvents.stream()
                .collect(Collectors.groupingBy(
                        click -> click.getClickDate().toLocalDate(),
                        Collectors.counting()
                ));
    }

    public UrlMapping getOriginalUrl(String shortUrl) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null) {
            // Increment click count
            urlMapping.setClickCount(urlMapping.getClickCount() + 1);
            urlMappingRepository.save(urlMapping);

            // Log click event
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setUrlMapping(urlMapping);
            clickEvent.setClickDate(LocalDateTime.now());
            clickEventRepository.save(clickEvent);
        }
        return urlMapping;
    }
}
