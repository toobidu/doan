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
    name = "exams",
    indexes = {
        @Index(name = "idx_exams_topic_id", columnList = "topic_id"),
        @Index(name = "idx_exams_teacher_id", columnList = "teacher_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "topic_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_exams_topic_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Topic topic;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "teacher_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_exams_teacher_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private User teacher;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Room> rooms = new ArrayList<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
