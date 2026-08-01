package com.wisewealth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isEmailVerified = false;

    @Column(length = 255)
    private String verificationToken;

    private LocalDateTime verificationTokenExpiry;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean isAdmin = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ConsultationBooking> consultationBookings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Query> queries;
}
