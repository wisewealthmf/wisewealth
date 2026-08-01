package com.wisewealth.controller;

import com.wisewealth.dto.QueryDto;
import com.wisewealth.dto.QueryReplyRequest;
import com.wisewealth.dto.StatusUpdateRequest;
import com.wisewealth.entity.CategoryEnum;
import com.wisewealth.entity.StatusEnum;
import com.wisewealth.service.QueryService;
import com.wisewealth.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/queries")
@RequiredArgsConstructor
@Slf4j
public class AdminQueryController {
    private final QueryService queryService;

    @GetMapping
    public ResponseEntity<Page<QueryDto>> getAllQueries(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            Pageable pageable) {
        Page<QueryDto> response;

        if (status != null && !status.isEmpty()) {
            StatusEnum statusEnum = parseStatus(status);
            response = queryService.getQueriesByStatus(statusEnum, pageable);
        } else if (category != null && !category.isEmpty()) {
            CategoryEnum categoryEnum = parseCategory(category);
            response = queryService.getQueriesByCategory(categoryEnum, pageable);
        } else {
            response = queryService.getAllQueries(pageable);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{queryId}")
    public ResponseEntity<QueryDto> getQueryById(@PathVariable Long queryId) {
        QueryDto response = queryService.getQueryByIdAdmin(queryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{queryId}/reply")
    public ResponseEntity<QueryDto> replyToQuery(
            @PathVariable Long queryId,
            @Valid @RequestBody QueryReplyRequest request) {
        QueryDto response = queryService.replyToQuery(queryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{queryId}/status")
    public ResponseEntity<QueryDto> updateQueryStatus(
            @PathVariable Long queryId,
            @Valid @RequestBody StatusUpdateRequest request) {
        QueryDto response = queryService.updateQueryStatus(queryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{queryId}")
    public ResponseEntity<Void> deleteQuery(@PathVariable Long queryId) {
        queryService.deleteQuery(queryId);
        return ResponseEntity.noContent().build();
    }

    private StatusEnum parseStatus(String status) {
        try {
            return StatusEnum.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    private CategoryEnum parseCategory(String category) {
        try {
            return CategoryEnum.valueOf(category.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category value: " + category);
        }
    }
}
