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
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "rooms",
    indexes = {
        @Index(name = "idx_rooms_status", columnList = "status"),
        @Index(name = "idx_rooms_owner_id", columnList = "owner_id"),
        @Index(name = "idx_rooms_topic_id", columnList = "topic_id"),
        @Index(name = "idx_rooms_exam_id", columnList = "exam_id")
    }
)
@Check(constraints = "max_players > 0 AND question_count > 0 AND countdown_time > 0")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, unique = true)
    private String roomCode;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "room_mode", nullable = false)
    private String roomMode;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "topic_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_rooms_topic_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Topic topic;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "exam_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_rooms_exam_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Exam exam;

    @Column(name = "is_private")
    private Boolean isPrivate;

    @Column(name = "owner_id")
    private Long ownerId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "owner_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_rooms_owner_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private User owner;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers;

    /**
     * Số lượng câu hỏi trong phòng
     */
    @Column(name = "question_count", nullable = false)
    private Integer questionCount;

    /**
     * Thời gian trả lời mỗi câu hỏi (giây)
     */
    @Column(name = "countdown_time", nullable = false)
    private Integer countdownTime;

    /**
     * Đánh dấu phòng đã có lịch sử game (không được xóa)
     */
    @Column(name = "has_game_history", nullable = false)
    private Boolean hasGameHistory = false;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoomPlayers> roomPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<GameSession> gameSessions = new ArrayList<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
