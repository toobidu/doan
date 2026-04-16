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
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "game_sessions",
    indexes = {
        @Index(name = "idx_game_sessions_room_id", columnList = "room_id"),
        @Index(name = "idx_game_sessions_status", columnList = "game_status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSession implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "room_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_game_sessions_room_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Room room;

    @Column(name = "room_status", nullable = false)
    private String roomStatus;

    @Column(name = "game_status", nullable = false)
    private String gameStatus;

    @Column(name = "current_question_id")
    private Long currentQuestionId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "current_question_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_game_sessions_current_question_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Question currentQuestion;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * Thời gian giới hạn cho phiên chơi.
     * Sử dụng Duration để lưu trữ thời gian.
     */
    @Column(name = "time_limit")
    private Duration timeLimit;

    @OneToMany(mappedBy = "gameSession", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<GameQuestion> gameQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<GameHistory> gameHistories = new ArrayList<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
