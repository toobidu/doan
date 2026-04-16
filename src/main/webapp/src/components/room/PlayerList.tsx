import { useEffect, useRef, useState } from 'react';
import { FaCrown } from 'react-icons/fa';
import { FiUser } from 'react-icons/fi';
import useRoomStore from '../../stores/use-room-store';
import '../../styles/components/room/player-list.css';

const PlayerList = () => {
    const { roomPlayers, currentRoom } = useRoomStore();
    const [animatingPlayers, setAnimatingPlayers] = useState<Set<string>>(new Set());
    const [playerStates, setPlayerStates] = useState<Map<string, any>>(new Map());
    const previousPlayerIdsRef = useRef<Set<string>>(new Set());

    // Track player changes for animations
    useEffect(() => {
        const currentPlayerIds: Set<string> = new Set<string>(roomPlayers.map((p: any) => String(p.id)));
        const previousPlayerIds = new Set<string>(Array.from(previousPlayerIdsRef.current));

        // Find newly joined players
        const newPlayers = roomPlayers.filter((p: any) => !previousPlayerIds.has(String(p.id)));
        // Find players who left
        const leftPlayerIds = Array.from(previousPlayerIds).filter((id) => !currentPlayerIds.has(id));

        // Animate new players
        newPlayers.forEach((player: any) => {
            setAnimatingPlayers(prev => new Set(prev).add(`join-${player.id}`));
            setTimeout(() => {
                setAnimatingPlayers(prev => {
                    const newSet = new Set(prev);
                    newSet.delete(`join-${player.id}`);
                    return newSet;
                });
            }, 600);
        });

        // Animate leaving players (remove from DOM after animation)
        leftPlayerIds.forEach((playerId) => {
            setAnimatingPlayers(prev => new Set(prev).add(`leave-${playerId}`));
            setTimeout(() => {
                setPlayerStates(prev => {
                    const newMap = new Map(prev);
                    newMap.delete(playerId);
                    return newMap;
                });
                setAnimatingPlayers(prev => {
                    const newSet = new Set(prev);
                    newSet.delete(`leave-${playerId}`);
                    return newSet;
                });
            }, 400);
        });

        // Update player states
        const newStates = new Map();
        roomPlayers.forEach((player: any) => {
            newStates.set(String(player.id), player);
        });
        setPlayerStates(newStates);
        previousPlayerIdsRef.current = currentPlayerIds;

    }, [roomPlayers]);

    const isPlayerHost = (playerId: string) => {
        return String(currentRoom?.hostId) === playerId || String(currentRoom?.ownerId) === playerId;
    };

    const getPlayerDisplayName = (player: any) => {
        return player.username || player.name || `Player ${player.id}`;
    };

    const allPlayers = Array.from(playerStates.values()).concat(
        // Include leaving players for animation
        Array.from(animatingPlayers)
            .filter(key => key.startsWith('leave-'))
            .map(key => ({ id: key.replace('leave-', ''), username: 'Rời phòng...', isLeaving: true }))
    );

    return (
        <div className="player-list">
            <div className="player-list-header">
                <h3>
                    <FiUser />
                    Người chơi ({roomPlayers.length}/{currentRoom?.maxPlayers || 10})
                </h3>
            </div>

            <div className="player-list-content">
                {allPlayers.map(player => {
                    const isJoining = animatingPlayers.has(`join-${player.id}`);
                    const isLeaving = animatingPlayers.has(`leave-${player.id}`) || player.isLeaving;
                    const isHost = isPlayerHost(player.id);

                    return (
                        <div
                            key={player.id}
                            className={`player-item ${isJoining ? 'joining' : ''} ${isLeaving ? 'leaving' : ''} ${isHost ? 'host' : ''}`}
                        >
                            <div className="player-avatar">
                                {isHost && <FaCrown className="host-crown" />}
                                <FiUser />
                            </div>

                            <div className="player-info">
                                <span className="player-name">
                                    {getPlayerDisplayName(player)}
                                    {isHost && <span className="host-badge">Host</span>}
                                </span>
                                <span className="player-status">
                                    {isLeaving ? 'Đang rời...' : 'Sẵn sàng'}
                                </span>
                            </div>

                            {isJoining && (
                                <div className="join-indicator">
                                    <span>Vào phòng</span>
                                </div>
                            )}
                        </div>
                    );
                })}

                {roomPlayers.length === 0 && (
                    <div className="empty-player-list">
                        <FiUser size={48} />
                        <p>Chưa có người chơi nào</p>
                        <span>Chia sẻ mã phòng để mời bạn bè!</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PlayerList;