package org.example.quizizz.controller.socketio.listener;

import com.corundumstudio.socketio.SocketIOServer;
import org.example.quizizz.controller.socketio.broadcast.RoomSocketFacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomListEventHandler {
    

    private final SocketIOServer socketIOServer;
    private final RoomSocketFacade roomSocketFacade;
    
    public void registerEvents(SocketIOServer server) {
        
        server.addEventListener("subscribe-room-list", Object.class, (client, data, ackRequest) -> {
            try {
                client.joinRoom("room-list");
                log.info("Client {} subscribed to room-list", client.getSessionId());
                
                // Initial room list will be sent by the client after subscription
                client.sendEvent("room-list-subscribed", Map.of("message", "Successfully subscribed to room list"));
                
            } catch (Exception e) {
                log.error("Error subscribing to room-list: {}", e.getMessage());
                client.sendEvent("error", Map.of("message", "Failed to subscribe to room list"));
            }
        });
        
        server.addEventListener("unsubscribe-room-list", Object.class, (client, data, ackRequest) -> {
            client.leaveRoom("room-list");
            log.info("Client {} unsubscribed from room-list", client.getSessionId());
        });
    }
    
    /**
     * Notify all clients subscribed to room-list about a new room
     * @param roomResponse The created room
     */
    public void notifyRoomCreated(Object roomResponse) {
        roomSocketFacade.notifyRoomCreated(roomResponse);
    }
    
    /**
     * Notify all clients subscribed to room-list about a room update
     * @param roomResponse The updated room
     */
    public void notifyRoomUpdated(Object roomResponse) {
        roomSocketFacade.notifyRoomUpdated(roomResponse);
    }
    
    /**
     * Notify all clients subscribed to room-list about a room deletion
     * @param roomId The deleted room ID
     */
    public void notifyRoomDeleted(Long roomId) {
        roomSocketFacade.notifyRoomDeleted(roomId);
    }
}
