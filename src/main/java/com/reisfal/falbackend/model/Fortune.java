package com.reisfal.falbackend.model;

import jakarta.persistence.*;
import lombok.*;

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
    private User user;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fortuneText;

    @Column(nullable = false)
    private String createdAt;
}
