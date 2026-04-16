package org.example.quizizz.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "game_questions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_game_questions_session_order", columnNames = {"game_session_id", "question_order"})
    },
    indexes = {
        @Index(name = "idx_game_questions_session_order", columnList = "game_session_id,question_order"),
        @Index(name = "idx_game_questions_question_id", columnList = "question_id")
    }
)
@Check(constraints = "question_order >= 0")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameQuestion implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "game_session_id", nullable = false)
    private Long gameSessionId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "game_session_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_game_questions_game_session_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private GameSession gameSession;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "question_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_game_questions_question_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Question question;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(name = "time_limit", nullable = false)
    private Duration timeLimit;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
