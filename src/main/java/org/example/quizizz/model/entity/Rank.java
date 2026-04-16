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
    name = "ranks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_ranks_user_id", columnNames = {"user_id"})
    },
    indexes = {
        @Index(name = "idx_ranks_user_id", columnList = "user_id"),
        @Index(name = "idx_ranks_total_score", columnList = "total_score")
    }
)
@Check(constraints = "total_score >= 0 AND game_played >= 0 AND total_time >= 0")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rank implements Serializable{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_ranks_user_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private User user;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "game_played", nullable = false)
    private Integer gamePlayed;

    @Column(name = "total_time", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long totalTime = 0L;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
