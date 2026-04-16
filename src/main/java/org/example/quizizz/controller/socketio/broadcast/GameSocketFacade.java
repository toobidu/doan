package org.example.quizizz.controller.socketio.broadcast;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameSocketFacade {

    private final SocketIOServer socketIOServer;
    private final GameSocketPayloadFactory payloadFactory;

    public void notifyGameStarted(String roomCode, Long roomId, Object question) {
        emitToRoom(roomCode, "game-started", payloadFactory.gameStarted(roomId, question));
    }

    public void notifyNextQuestion(String roomCode, Object question) {
        emitToRoom(roomCode, "next-question", payloadFactory.nextQuestion(question));
    }

    public void notifyGameFinished(String roomCode, Object result) {
        emitToRoom(roomCode, "game-finished", payloadFactory.gameFinished(result));
    }

    public void notifyPlayerAnswered(String roomCode, Long userId) {
        emitToRoom(roomCode, "player-answered", payloadFactory.playerAnswered(userId));
    }

    public void notifyQuestionResultShown(String roomCode, Long roomId) {
        emitToRoom(roomCode, "question-result-shown", payloadFactory.questionResultShown(roomId));
    }

    public void notifyCountdownTick(String roomCode,
                                    Long roomId,
                                    int remainingTime,
                                    long startedAt,
                                    long expiresAt) {
        emitToRoom(
                roomCode,
                "countdown-tick",
                payloadFactory.countdownTick(roomId, roomCode, remainingTime, startedAt, expiresAt)
        );
    }

    public void notifyTimeUp(String roomCode, Long roomId) {
        emitToRoom(roomCode, "time-up", payloadFactory.timeUp(roomId, roomCode));
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