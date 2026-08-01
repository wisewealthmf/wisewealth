package com.wisewealth.controller;

import com.wisewealth.dto.FreeGuideRequest;
import com.wisewealth.service.FreeGuideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/free-guide")
@RequiredArgsConstructor
@Slf4j
public class FreeGuideController {

    private final FreeGuideService freeGuideService;

    /**
     * Public endpoint — no authentication required.
     * Accepts an email, saves it to user_emails, and dispatches the guide PDF link.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> requestFreeGuide(
            @Valid @RequestBody FreeGuideRequest request) {
        log.info("Free guide requested for name={} email={}", request.getName(), request.getEmail());
        freeGuideService.requestFreeGuide(request.getName(), request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Guide sent! Check your inbox."));
    }
}
