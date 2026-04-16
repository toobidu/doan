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
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "questions",
    indexes = {
        @Index(name = "idx_questions_exam_id", columnList = "exam_id"),
        @Index(name = "idx_questions_type", columnList = "question_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;
    
    @Column(name = "exam_id", nullable = false)
    private Long examId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "exam_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_questions_exam_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Exam exam;

    @Column(name = "question_type", nullable = false)
    private String questionType;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Answer> answers = new ArrayList<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
