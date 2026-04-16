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
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_answers",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_answers_room_user_question", columnNames = {"room_id", "user_id", "question_id"})
    },
    indexes = {
        @Index(name = "idx_user_answers_room_user", columnList = "room_id,user_id"),
        @Index(name = "idx_user_answers_room_question", columnList = "room_id,question_id"),
        @Index(name = "idx_user_answers_user_id", columnList = "user_id")
    }
)
@Check(constraints = "score >= 0 AND (time_taken IS NULL OR time_taken >= 0)")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_answers_user_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private User user;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "question_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_answers_question_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Question question;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "room_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_answers_room_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Room room;

    @Column(name = "answer_id")
    private Long answerId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "answer_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_answers_answer_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Answer answer;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "time_taken")
    private Integer timeTaken;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
