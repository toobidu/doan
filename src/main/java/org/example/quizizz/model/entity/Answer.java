package org.example.quizizz.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Entity
@Table(
    name = "answers",
    indexes = {
        @Index(name = "idx_answers_question_id", columnList = "question_id"),
        @Index(name = "idx_answers_question_correct", columnList = "question_id,is_correct")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "question_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_answers_question_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Question question;

    @Column(name = "answer_text", nullable = false, length = 500)
    private String answerText;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
}
