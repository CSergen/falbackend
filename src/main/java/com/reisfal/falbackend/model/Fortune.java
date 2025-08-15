package com.reisfal.falbackend.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.reisfal.falbackend.model.enums.FortuneCategory;

@Entity
@Table(name = "fortunes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Fortune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // JSON'a user sokma
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FortuneCategory category = FortuneCategory.GENEL; // 👈 EKLENDİ

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fortuneText;

    @Column(nullable = false)
    private String createdAt; // (opsiyonel: Instant yapabilirsin)
}
