package com.wisewealth.service;

import com.wisewealth.dto.QueryDto;
import com.wisewealth.dto.QueryReplyRequest;
import com.wisewealth.dto.QueryRequest;
import com.wisewealth.dto.StatusUpdateRequest;
import com.wisewealth.entity.CategoryEnum;
import com.wisewealth.entity.Query;
import com.wisewealth.entity.StatusEnum;
import com.wisewealth.entity.User;
import com.wisewealth.exception.ResourceNotFoundException;
import com.wisewealth.repository.QueryRepository;
import com.wisewealth.repository.UserRepository;
import com.wisewealth.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {
    private final QueryRepository queryRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public QueryDto createQuery(QueryRequest request, Long userId) {
        log.info("Creating query for user id: {}", userId);

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required to submit a query.");
        }

        User user = emailVerificationService.requireVerifiedOrCreateUser(request.getName(), request.getEmail(), request.getPhone());

        CategoryEnum category;
        try {
            category = CategoryEnum.valueOf(request.getCategory().toUpperCase().replace(" ", "_"));
        } catch (Exception e) {
            category = CategoryEnum.OTHER;
        }

        Query query = Query.builder()
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .queryText(request.getQueryText())
                .category(category)
                .status(StatusEnum.NEW)
                .build();

        Query saved = queryRepository.save(query);
        log.info("Query created with id: {}", saved.getQueryId());
        QueryDto dto = mapToDto(saved);
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String subject = "Thanks for contacting WiseWealth";
            String body = String.format("<p>Hello %s,</p><p>Thanks for contacting WiseWealth. We received your query and will respond shortly.</p><p><b>Your Query</b></p><p>%s</p>", dto.getName(), dto.getQueryText());
            try {
                emailService.sendHtmlEmail(dto.getEmail(), subject, body);
            } catch (Exception ex) {
                log.error("Failed to queue confirmation email for queryId={}", saved.getQueryId(), ex);
            }
        }
        return dto;
    }

    public QueryDto getQueryById(Long queryId, Long userId) {
        log.info("Fetching query id: {} for user: {}", queryId, userId);

        Query query = queryRepository.findByQueryIdAndUserUserId(queryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found"));

        return mapToDto(query);
    }

    public QueryDto getQueryByIdAdmin(Long queryId) {
        log.info("Admin fetching query id: {}", queryId);

        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found"));

        return mapToDto(query);
    }

    public Page<QueryDto> getMyQueries(Long userId, Pageable pageable) {
        log.info("Fetching queries for user id: {}", userId);
        return queryRepository.findByUserUserId(userId, pageable)
                .map(this::mapToDto);
    }

    public Page<QueryDto> getMyQueriesByStatus(Long userId, StatusEnum status, Pageable pageable) {
        log.info("Fetching queries for user id: {} with status: {}", userId, status);
        return queryRepository.findByUserUserIdAndStatus(userId, status, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public QueryDto updateQuery(Long queryId, QueryRequest request) {
        log.info("Updating query id: {}", queryId);

        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found"));

        if (request.getName() != null) query.setName(request.getName());
        if (request.getEmail() != null) query.setEmail(request.getEmail());
        if (request.getPhone() != null) query.setPhone(request.getPhone());
        if (request.getQueryText() != null) query.setQueryText(request.getQueryText());
        
        if (request.getCategory() != null) {
            try {
                CategoryEnum category = CategoryEnum.valueOf(request.getCategory().toUpperCase().replace(" ", "_"));
                query.setCategory(category);
            } catch (Exception e) {
                log.warn("Invalid category provided: {}", request.getCategory());
            }
        }

        Query updated = queryRepository.save(query);
        log.info("Query updated: {}", queryId);
        return mapToDto(updated);
    }

    @Transactional
    public QueryDto replyToQuery(Long queryId, QueryReplyRequest request) {
        log.info("Adding reply to query id: {}", queryId);

        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found"));

        query.setReply(request.getReplyText());
        query.setStatus(StatusEnum.REPLIED);

        Query updated = queryRepository.save(query);
        log.info("Reply added to query: {}", queryId);
        QueryDto dto = mapToDto(updated);
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            try {
                emailService.sendReplyEmail(dto.getEmail(), dto.getName(), dto.getQueryText(), request.getReplyText());
            } catch (Exception ex) {
                log.error("Failed to queue reply email for queryId={}", queryId, ex);
            }
        }
        return dto;
    }

    @Transactional
    public QueryDto updateQueryStatus(Long queryId, StatusUpdateRequest request) {
        log.info("Updating status for query id: {} to: {}", queryId, request.getStatus());

        Query query = queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found"));

        query.setStatus(request.getStatus());
        Query updated = queryRepository.save(query);
        log.info("Query status updated: {}", queryId);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteQuery(Long queryId) {
        log.info("Deleting query id: {}", queryId);
        if (!queryRepository.existsById(queryId)) {
            throw new ResourceNotFoundException("Query not found with id: " + queryId);
        }
        queryRepository.deleteById(queryId);
        log.info("Query deleted: {}", queryId);
    }

    public Page<QueryDto> getAllQueries(Pageable pageable) {
        log.info("Admin fetching all queries");
        return queryRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    public Page<QueryDto> getQueriesByStatus(StatusEnum status, Pageable pageable) {
        log.info("Admin fetching queries with status: {}", status);
        return queryRepository.findByStatus(status, pageable)
                .map(this::mapToDto);
    }

    public Page<QueryDto> getQueriesByCategory(CategoryEnum category, Pageable pageable) {
        log.info("Admin fetching queries with category: {}", category);
        return queryRepository.findByCategory(category, pageable)
                .map(this::mapToDto);
    }

    private QueryDto mapToDto(Query query) {
        return QueryDto.builder()
                .queryId(query.getQueryId())
                .userId(query.getUser() != null ? query.getUser().getUserId() : null)
                // consultation relation removed; no consultationId
                .name(query.getName())
                .email(query.getEmail())
                .phone(query.getPhone())
                .queryText(query.getQueryText())
                .category(query.getCategory())
                .status(query.getStatus())
                .reply(query.getReply())
                .createdAt(query.getCreatedAt())
                .updatedAt(query.getUpdatedAt())
                .build();
    }
}
