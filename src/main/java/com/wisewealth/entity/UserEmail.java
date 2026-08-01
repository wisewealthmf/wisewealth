package com.wisewealth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_emails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    /**
     * Purpose of the lead capture: FREE_GUIDE or WEALTH_CHECK.
     * Stored as a plain VARCHAR so no enum migration is needed.
     */
    @Column(nullable = false, length = 50, columnDefinition = "varchar(50) default 'FREE_GUIDE'")
    @Builder.Default
    private String purpose = "FREE_GUIDE";

    /** True if this email also has an account in the users table. */
    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isUser = false;

    /** Set to true once the admin has followed up with this lead. */
    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean hasFollowedUp = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
