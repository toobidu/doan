package org.example.quizizz.repository;

import org.example.quizizz.model.entity.RoomPlayers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho quản lý người chơi trong phòng
 */
@Repository
public interface RoomPlayerRepository extends JpaRepository<RoomPlayers, Long> {

    /**
     * Lấy danh sách người chơi đang active trong phòng theo thứ tự join
     */
    @Query("SELECT rp FROM RoomPlayers rp WHERE rp.roomId = :roomId AND rp.status = 'ACTIVE' ORDER BY rp.joinOrder ASC")
    List<RoomPlayers> findByRoomIdOrderByJoinOrder(@Param("roomId") Long roomId);

    /**
     * Lấy danh sách người chơi trong phòng
     */
    List<RoomPlayers> findByRoomId(Long roomId);

    /**
     * Kiểm tra user có trong phòng và đang active không
     */
    @Query("SELECT COUNT(rp) > 0 FROM RoomPlayers rp WHERE rp.roomId = :roomId AND rp.userId = :userId AND rp.status = 'ACTIVE'")
    boolean existsByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * Lấy thông tin player trong phòng
     */
    Optional<RoomPlayers> findByRoomIdAndUserId(Long roomId, Long userId);

    /**
     * Đếm số lượng người chơi đang active trong phòng
     */
    @Query("SELECT COUNT(rp) FROM RoomPlayers rp WHERE rp.roomId = :roomId AND rp.status = 'ACTIVE'")
    Integer countPlayersInRoom(@Param("roomId") Long roomId);

    /**
     * Đếm số lượng người chơi active theo nhiều phòng để tránh N+1 queries.
     */
    @Query("SELECT rp.roomId, COUNT(rp) FROM RoomPlayers rp WHERE rp.roomId IN :roomIds AND rp.status = 'ACTIVE' GROUP BY rp.roomId")
    List<Object[]> countPlayersInRooms(@Param("roomIds") List<Long> roomIds);

    /**
     * Lấy join order cao nhất trong phòng
     */
    @Query("SELECT COALESCE(MAX(rp.joinOrder), 0) FROM RoomPlayers rp WHERE rp.roomId = :roomId")
    Integer getMaxJoinOrderInRoom(@Param("roomId") Long roomId);

    /**
     * Xóa player khỏi phòng
     */
    void deleteByRoomIdAndUserId(Long roomId, Long userId);

    /**
     * Kiểm tra user có bị kick khỏi phòng không
     */
    @Query("SELECT COUNT(rp) > 0 FROM RoomPlayers rp WHERE rp.roomId = :roomId AND rp.userId = :userId AND rp.status = 'KICKED'")
    boolean isUserKicked(@Param("roomId") Long roomId, @Param("userId") Long userId);

    /**
     * Xóa tất cả player của phòng
     */
    void deleteByRoomId(Long roomId);

}
