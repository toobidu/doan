// Helper functions for room data mapping
export const mapRoomFromBackend = (backendRoom) => {
    if (!backendRoom) return null;

    return {
        // Primary identifiers
        id: backendRoom.id,
        roomCode: backendRoom.roomCode,
        Code: backendRoom.roomCode, // For frontend compatibility  
        code: backendRoom.roomCode,

        // Room details
        roomName: backendRoom.roomName,
        RoomName: backendRoom.roomName,
        name: backendRoom.roomName, // alias for consistency

        // Topic information
        topicId: backendRoom.topicId,
        topicName: backendRoom.topicName,
        TopicName: backendRoom.topicName,

        // Exam information
        examId: backendRoom.examId,
        examTitle: backendRoom.examTitle,

        // Owner/Host information
        ownerId: backendRoom.ownerId,
        hostId: backendRoom.ownerId, // alias for consistency
        HostName: backendRoom.ownerUsername,
        hostName: backendRoom.ownerUsername,

        // Player counts
        maxPlayers: backendRoom.maxPlayers,
        MaxPlayers: backendRoom.maxPlayers,
        currentPlayers: backendRoom.currentPlayers,
        CurrentPlayers: backendRoom.currentPlayers,

        // Room settings
        isPrivate: backendRoom.isPrivate,
        IsPrivate: backendRoom.isPrivate,
        roomMode: backendRoom.roomMode,
        questionCount: backendRoom.questionCount,
        countdownTime: backendRoom.countdownTime,

        // Status
        status: backendRoom.status?.toLowerCase() || 'waiting',
        Status: backendRoom.status || 'WAITING',

        // Timestamps
        createdAt: backendRoom.createdAt,
        CreatedAt: backendRoom.createdAt,

        // Settings object for UI compatibility
        Settings: {
            timeLimit: backendRoom.countdownTime,
            questionCount: backendRoom.questionCount,
            gameMode: backendRoom.roomMode
        }
    };
};

export const mapRoomsFromBackend = (backendRooms) => {
    if (!Array.isArray(backendRooms)) return [];
    const mapped = backendRooms.map(mapRoomFromBackend).filter(room => room !== null);
    return mapped;
};

// Adapter pattern: chuẩn hóa payload room đến từ Socket.IO realtime events.
export const adaptRealtimeRoomPayload = (room) => {
    if (!room) return null;

    const roomCode = room.roomCode || room.RoomCode || room.code || room.Code;
    const roomName = room.roomName || room.RoomName || room.name;
    const topicName = room.topicName || room.TopicName;
    const maxPlayers = room.maxPlayers ?? room.MaxPlayers;
    const currentPlayers = room.currentPlayers ?? room.CurrentPlayers;
    const status = room.status || (typeof room.Status === 'string' ? room.Status.toLowerCase() : room.Status);

    return {
        ...room,
        id: room.id,
        roomCode,
        code: room.code || roomCode,
        Code: room.Code || roomCode,
        roomName,
        RoomName: room.RoomName || roomName,
        topicName,
        TopicName: room.TopicName || topicName,
        maxPlayers,
        MaxPlayers: room.MaxPlayers ?? maxPlayers,
        currentPlayers,
        CurrentPlayers: room.CurrentPlayers ?? currentPlayers,
        status,
        Status: room.Status || room.status
    };
};

export const mapCreateRoomRequest = (frontendData) => {
    return {
        roomName: frontendData.roomName,
        roomMode: frontendData.roomMode || frontendData.gameMode,
        topicId: parseInt(frontendData.topicId),
        examId: parseInt(frontendData.examId),
        isPrivate: frontendData.isPrivate || false,
        maxPlayers: parseInt(frontendData.maxPlayers),
        questionCount: parseInt(frontendData.questionCount),
        countdownTime: parseInt(frontendData.countdownTime || frontendData.timeLimit)
    };
};