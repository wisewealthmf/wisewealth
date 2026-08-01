package com.wisewealth.controller;

import com.wisewealth.dto.QueryDto;
import com.wisewealth.dto.QueryRequest;
import com.wisewealth.entity.StatusEnum;
import com.wisewealth.exception.UnauthorizedException;
import com.wisewealth.service.QueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/queries")
@RequiredArgsConstructor
public class QueryController {
    private final QueryService queryService;

    /** Public — unauthenticated users may submit a query (lead capture). */
    @PostMapping
    public ResponseEntity<QueryDto> createQuery(@Valid @RequestBody QueryRequest request) {
        log.info("Create query request received: email={} name={}", request.getEmail(), request.getName());
        QueryDto response = queryService.createQuery(request, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** Authenticated — returns only the calling user's queries. */
    @GetMapping
    public ResponseEntity<Page<QueryDto>> getMyQueries(
            Authentication authentication,
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable) {
        Long userId = requireUserId(authentication);
        log.info("GetMyQueries called for userId={} status={}", userId, status);
        Page<QueryDto> response;
        if (status != null && !status.isEmpty()) {
            StatusEnum statusEnum = parseStatus(status);
            response = queryService.getMyQueriesByStatus(userId, statusEnum, pageable);
        } else {
            response = queryService.getMyQueries(userId, pageable);
        }
        return ResponseEntity.ok(response);
    }

    /** Authenticated — returns only the calling user's specific query. */
    @GetMapping("/{queryId}")
    public ResponseEntity<QueryDto> getQueryById(
            Authentication authentication,
            @PathVariable Long queryId) {
        Long userId = requireUserId(authentication);
        log.info("GetQueryById called for userId={} queryId={}", userId, queryId);
        QueryDto response = queryService.getQueryById(queryId, userId);
        return ResponseEntity.ok(response);
    }

    private Long requireUserId(Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken auth)
                || !(auth.getPrincipal() instanceof Long userId)) {
            throw new UnauthorizedException("Authorization header is required");
        }
        return userId;
    }

    private StatusEnum parseStatus(String status) {
        try {
            return StatusEnum.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }
}
