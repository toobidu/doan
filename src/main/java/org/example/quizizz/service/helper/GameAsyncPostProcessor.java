package org.example.quizizz.service.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.quizizz.service.Implement.PlayerProfileServiceImplement;
import org.example.quizizz.service.Implement.RankServiceImplement;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameAsyncPostProcessor {

    private final RankServiceImplement rankService;
    private final PlayerProfileServiceImplement playerProfileService;

    @Async("gameTaskExecutor")
    public void updateRankAndProfileAfterGame(Long userId, Integer score, Long totalTime, Long roomId) {
        try {
            rankService.updateRankAfterGame(userId, score, totalTime);
        } catch (Exception e) {
            log.error("Error updating rank for user {}: {}", userId, e.getMessage());
        }

        try {
            playerProfileService.updateProfileAfterGame(userId, roomId);
        } catch (Exception e) {
            log.error("Error updating player profile for user {}: {}", userId, e.getMessage());
        }
    }
}
