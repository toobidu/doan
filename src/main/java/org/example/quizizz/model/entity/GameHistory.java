package org.example.quizizz.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "game_histories",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_game_histories_session_user", columnNames = {"game_session_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_game_histories_session_user", columnList = "game_session_id,user_id"),
        @Index(name = "idx_game_histories_user_id", columnList = "user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameHistory implements Serializable{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_session_id")
    private Long gameSessionId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "game_session_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_game_histories_game_session_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private GameSession gameSession;

    @Column(name = "user_id")
    private Long userId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_game_histories_user_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private User user;

    @Column(name = "score")
    private Integer score;

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
