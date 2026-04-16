import { useNavigate } from 'react-router-dom';
import useGameStore from '../../stores/use-game-store';
import '../../styles/components/room/game-results.css';
import { BarChart3, Crown, Medal, Trophy } from 'lucide-react';
import { MdCelebration } from 'react-icons/md';

/**
 * GameResults Component - Final leaderboard and statistics
 * Similar to Kahoot/Quizizz results screen
 */
const GameResults = ({ onExit, gameResults }) => {
    const navigate = useNavigate();

    const {
        leaderboard: storeLeaderboard,
        totalQuestions
    } = useGameStore();

    const leaderboard = gameResults?.ranking || storeLeaderboard;
    const totalQs = gameResults?.totalQuestions || totalQuestions;

    /**
     * Handle play again
     */
    const handlePlayAgain = () => {
        // Navigate back to dashboard to create/join new game
        navigate('/dashboard');
    };

    const getRankIcon = (rank) => {
        if (rank === 1) return <Crown size={16} />;
        if (rank === 2) return <Medal size={16} />;
        if (rank === 3) return <Medal size={16} />;
        return <Medal size={16} />;
    };

    /**
     * Get podium position class
     */
    const getPodiumClass = (rank) => {
        switch (rank) {
            case 1: return 'podium-first';
            case 2: return 'podium-second';
            case 3: return 'podium-third';
            default: return '';
        }
    };

    /**
     * Calculate player accuracy percentage
     */
    const calculateAccuracy = (player) => {
        if (!player.correctAnswers && !player.totalAnswers) return 0;
        const correct = player.correctAnswers || 0;
        const total = player.totalAnswers || totalQs || 0;
        if (total === 0) return 0;
        return Math.round((correct / total) * 100);
    };

    return (
        <div className="game-results-container">
            {/* Confetti animation */}
            <div className="confetti-container">
                {[...Array(50)].map((_, i) => (
                    <div key={i} className="confetti" style={{
                        left: `${Math.random() * 100}%`,
                        animationDelay: `${Math.random() * 3}s`,
                        backgroundColor: ['#e74c3c', '#3498db', '#f39c12', '#2ecc71'][Math.floor(Math.random() * 4)]
                    }} />
                ))}
            </div>

            {/* Header */}
            <div className="results-header">
                <h1><MdCelebration size={40} color="#FFD700" style={{verticalAlign: 'middle'}} /> Trò chơi kết thúc!</h1>
                <p className="game-stats">
                    Tổng số câu: {totalQs} | Người chơi: {leaderboard.length}
                </p>
            </div>

            {/* Podium - Top 3 */}
            <div className="podium-section">
                <h2><Trophy size={18} /> Podium</h2>
                <div className="podium">
                    {leaderboard.slice(0, 3).map((player, index) => {
                        const podiumOrder = index === 0 ? 0 : index === 1 ? 2 : 1; // Center winner
                        const rank = player.rank || (index + 1);
                        return (
                            <div
                                key={player.userId}
                                className={`podium-place ${getPodiumClass(rank)}`}
                                style={{ order: podiumOrder }}
                            >
                                <div className="player-avatar">
                                    <img
                                        src={player.avatar || '/default-avatar.png'}
                                        alt={player.userName || 'Player'}
                                        onError={(e) => {
                                            e.currentTarget.src = '/default-avatar.png';
                                        }}
                                    />
                                </div>
                                <div className="medal">{getRankIcon(rank)}</div>
                                <h3>{player.userName || `User${player.userId}`}</h3>
                                <p className="score">{player.totalScore || 0} điểm</p>
                                <div className={`podium-base rank-${rank}`}>
                                    <span className="rank-number">#{rank}</span>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>

            {/* Full Leaderboard */}
            <div className="leaderboard-section">
                <h2><BarChart3 size={18} /> Bảng xếp hạng</h2>
                <div className="leaderboard-table">
                    <div className="leaderboard-header">
                        <span>Hạng</span>
                        <span>Tên</span>
                        <span>Điểm</span>
                        <span>Độ chính xác</span>
                    </div>
                    <div className="leaderboard-body">
                        {leaderboard.map((player, index) => {
                            const rank = player.rank || (index + 1);
                            return (
                                <div
                                    key={player.userId}
                                    className={`leaderboard-row ${rank <= 3 ? 'top-three' : ''}`}
                                >
                                    <span className="rank">
                                        {rank <= 3 ? getRankIcon(rank) : `#${rank}`}
                                    </span>
                                    <span className="player-name">
                                        <img
                                            src={player.avatar || '/default-avatar.png'}
                                            alt={player.userName || 'Player'}
                                            className="mini-avatar"
                                            onError={(e) => {
                                                e.currentTarget.src = '/default-avatar.png';
                                            }}
                                        />
                                        {player.userName || `User${player.userId}`}
                                    </span>
                                    <span className="score">{player.totalScore || 0}</span>
                                    <span className="accuracy">
                                        {calculateAccuracy(player)}%
                                    </span>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>

            {/* Action buttons */}
            <div className="results-actions">
                <button className="btn-primary" onClick={handlePlayAgain}>
                    Chơi lại
                </button>
                <button className="btn-secondary" onClick={onExit}>
                    Về trang chủ
                </button>
            </div>
        </div>
    );
};

export default GameResults;
