package org.example.quizizz.controller.socketio.broadcast;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomSocketFacade {

    private final SocketIOServer socketIOServer;
    private final RoomSocketPayloadFactory payloadFactory;

    public void notifyRoomCreated(Object roomResponse) {
        emitToRoomList("room-created", payloadFactory.roomCreated(roomResponse));
    }

    public void notifyRoomUpdated(Object roomResponse) {
        emitToRoomList("room-updated", payloadFactory.roomUpdated(roomResponse));
    }

    public void notifyRoomDeleted(Long roomId) {
        emitToRoomList("room-deleted", payloadFactory.roomDeleted(roomId));
    }

    public void notifyPlayerJoined(String roomCode, Long roomId, Object player, int totalPlayers) {
        emitToRoom(roomCode, "player-joined", payloadFactory.playerJoined(roomId, player, totalPlayers));
    }

    public void notifyPlayerLeft(String roomCode, Long roomId, Object player, int totalPlayers) {
        emitToRoom(roomCode, "player-left", payloadFactory.playerLeft(roomId, player, totalPlayers));
    }

    public void notifyRoomPlayers(String roomCode, Long roomId, List<?> players) {
        emitToRoom(roomCode, "room-players", payloadFactory.roomPlayers(roomId, players));
    }

    public void notifyHostChanged(String roomCode, Long roomId, Long newHostId, Long previousHostId, String reason) {
        emitToRoom(roomCode, "host-changed", payloadFactory.hostChanged(roomId, newHostId, previousHostId, reason));
    }

    private void emitToRoomList(String event, Map<String, Object> payload) {
        try {
            socketIOServer.getRoomOperations("room-list").sendEvent(event, payload);
        } catch (Exception e) {
            log.error("Failed to emit {} to room-list: {}", event, e.getMessage(), e);
        }
    }

    private void emitToRoom(String roomCode, String event, Map<String, Object> payload) {
        if (roomCode == null || roomCode.isBlank()) {
            log.warn("Skip emitting {} because roomCode is blank", event);
            return;
        }

        try {
            socketIOServer.getRoomOperations("room-" + roomCode).sendEvent(event, payload);
        } catch (Exception e) {
            log.error("Failed to emit {} to room {}: {}", event, roomCode, e.getMessage(), e);
        }
    }
}