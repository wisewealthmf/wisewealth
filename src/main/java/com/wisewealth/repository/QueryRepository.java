package com.wisewealth.repository;

import com.wisewealth.entity.Query;
import com.wisewealth.entity.StatusEnum;
import com.wisewealth.entity.CategoryEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueryRepository extends JpaRepository<Query, Long> {
    Page<Query> findByUserUserId(Long userId, Pageable pageable);
    Page<Query> findByUserUserIdAndStatus(Long userId, StatusEnum status, Pageable pageable);
    Optional<Query> findByQueryIdAndUserUserId(Long queryId, Long userId);
    Page<Query> findByStatus(StatusEnum status, Pageable pageable);
    Page<Query> findByCategory(CategoryEnum category, Pageable pageable);
    Page<Query> findByStatusAndCategory(StatusEnum status, CategoryEnum category, Pageable pageable);
    Page<Query> findAll(Pageable pageable);
}
