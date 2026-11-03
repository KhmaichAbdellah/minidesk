package com.minidesk.entity;

import jakarta.persistence.*;
import lombok.*;
import com.minidesk.enums.Role;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;

    @Builder.Default // pour creer liste vide au lieu de null
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private List<Ticket> createdTickets = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "assignedTo")
    private List<Ticket> assignedTickets = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "changedBy", cascade = CascadeType.ALL)
    private List<StatutLog> statutLogs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "author")
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "author")
    private List<KnowledgeArticle> articles = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<AiSession> aiSessions = new ArrayList<>();
}