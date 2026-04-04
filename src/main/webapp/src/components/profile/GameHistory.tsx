// TODO: Fix react-icons import issue
// import { useState, useEffect } from 'react';
// import { FiTrophy, FiClock, FiTarget, FiAward } from 'react-icons/fi';
// import { getGameHistory, getPlayerStats } from '../../services/profile-api';
// import { calculateAchievements } from '../../utils/achievements';
// import AchievementsList from './AchievementsList';
import '../../styles/components/game-history.css';

const GameHistory = () => {
  return (
    <div className="game-history-container">
      <div className="no-history">
        <p>Chức năng lịch sử game đang được phát triển.</p>
      </div>
    </div>
  );
};

// TODO: Uncomment when react-icons and API are fixed
// const GameHistory = () => {
//   const [gameHistory, setGameHistory] = useState([]);
//   const [playerStats, setPlayerStats] = useState(null);
//   const [achievements, setAchievements] = useState([]);
//   const [loading, setLoading] = useState(true);
//   const [error, setError] = useState('');
//   ... rest of the code
// };

export default GameHistory;
