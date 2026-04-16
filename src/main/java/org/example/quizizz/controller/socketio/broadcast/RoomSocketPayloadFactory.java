package org.example.quizizz.controller.socketio.broadcast;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RoomSocketPayloadFactory {

    public Map<String, Object> roomCreated(Object room) {
        Map<String, Object> payload = basePayload();
        payload.put("room", room);
        return payload;
    }

    public Map<String, Object> roomUpdated(Object room) {
        Map<String, Object> payload = basePayload();
        payload.put("room", room);
        return payload;
    }

    public Map<String, Object> roomDeleted(Long roomId) {
        Map<String, Object> payload = basePayload();
        payload.put("roomId", roomId);
        return payload;
    }

    public Map<String, Object> playerJoined(Long roomId, Object player, int totalPlayers) {
        Map<String, Object> payload = basePayload();
        payload.put("roomId", roomId);
        payload.put("player", player);
        payload.put("totalPlayers", totalPlayers);
        return payload;
    }

    public Map<String, Object> playerLeft(Long roomId, Object player, int totalPlayers) {
        Map<String, Object> payload = basePayload();
        payload.put("roomId", roomId);
        payload.put("player", player);
        payload.put("totalPlayers", totalPlayers);
        return payload;
    }

    public Map<String, Object> roomPlayers(Long roomId, List<?> players) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("roomId", roomId);
        payload.put("players", players);
        return payload;
    }

    public Map<String, Object> hostChanged(Long roomId, Long newHostId, Long previousHostId, String reason) {
        Map<String, Object> payload = basePayload();
        payload.put("roomId", roomId);
        payload.put("newHostId", newHostId);
        if (previousHostId != null) {
            payload.put("previousHostId", previousHostId);
        }
        if (reason != null && !reason.isBlank()) {
            payload.put("reason", reason);
        }
        return payload;
    }

    private Map<String, Object> basePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", System.currentTimeMillis());
        return payload;
    }
}