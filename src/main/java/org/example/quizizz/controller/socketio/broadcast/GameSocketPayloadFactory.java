package org.example.quizizz.controller.socketio.broadcast;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameSocketPayloadFactory {

    public Map<String, Object> gameStarted(Long roomId, Object question) {
        Map<String, Object> payload = basePayload();
        payload.put("roomId", roomId);
        payload.put("question", question);
        return payload;
    }

    public Map<String, Object> nextQuestion(Object question) {
        Map<String, Object> payload = basePayload();
        payload.put("question", question);
        return payload;
    }

    public Map<String, Object> gameFinished(Object result) {
        Map<String, Object> payload = basePayload();
        payload.put("result", result);
        return payload;
    }

    public Map<String, Object> playerAnswered(Long userId) {
        Map<String, Object> payload = basePayload();
        payload.put("userId", userId);
        return payload;
    }

    public Map<String, Object> questionResultShown(Long roomId) {
        Map<String, Object> payload = basePayload();
        payload.put("roomId", roomId);
        return payload;
    }

    public Map<String, Object> countdownTick(Long roomId,
                                              String roomCode,
                                              int remainingTime,
                                              long startedAt,
                                              long expiresAt) {
        Map<String, Object> payload = basePayload();
        payload.put("roomId", roomId);
        payload.put("roomCode", roomCode);
        payload.put("remainingTime", remainingTime);
        payload.put("startedAt", startedAt);
        payload.put("expiresAt", expiresAt);
        return payload;
    }

    public Map<String, Object> timeUp(Long roomId, String roomCode) {
        Map<String, Object> payload = basePayload();
        payload.put("roomId", roomId);
        payload.put("roomCode", roomCode);
        return payload;
    }

    private Map<String, Object> basePayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", System.currentTimeMillis());
        return payload;
    }
}