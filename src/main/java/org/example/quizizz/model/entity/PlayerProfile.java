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

import java.time.LocalDateTime;

@Entity
@Table(
    name = "player_profiles",
    indexes = {
        @Index(name = "idx_player_profiles_user_id", columnList = "user_id")
    }
)
@Check(constraints = "age > 0 AND average_score >= 0 AND total_play_time >= 0")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_player_profiles_user_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private User user;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "average_score", nullable = false)
    private Double averageScore;

    @Column(name = "total_play_time", nullable = false)
    private Integer totalPlayTime;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
