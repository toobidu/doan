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
    name = "room_players",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_room_players_room_user", columnNames = {"room_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_room_players_room_status", columnList = "room_id,status"),
        @Index(name = "idx_room_players_room_user", columnList = "room_id,user_id"),
        @Index(name = "idx_room_players_room_join_order", columnList = "room_id,join_order")
    }
)
@Check(constraints = "join_order > 0 AND status IN ('ACTIVE','KICKED','LEFT')")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoomPlayers implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", nullable = false)
    private Long roomId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "room_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_room_players_room_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Room room;

    @Column(name = "user_id", nullable = false)
    private Long userId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_room_players_user_id"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private User user;

    @Column(name = "is_host", nullable = false)
    private Boolean isHost = false;

    /**
     * Thứ tự tham gia phòng (dùng để xác định host tiếp theo khi host rời phòng)
     */
    @Column(name = "join_order", nullable = false)
    private Integer joinOrder;

    /**
     * Trạng thái: ACTIVE, KICKED, LEFT
     */
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "time_taken")
    private Integer timeTaken;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
