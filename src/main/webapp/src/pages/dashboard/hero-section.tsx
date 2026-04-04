import { useNavigate } from 'react-router-dom';
import '../../styles/pages/dashboard/hero-section.css';

function HeroSection({ userName }) {
    const navigate = useNavigate();

    return (
        <div className="dh-hero-section">
            <div className="dh-hero-content">
                <div className="dh-hero-left">
                    <h1 className="dh-hero-title">Xin chào, {userName}!</h1>
                    <div className="dh-hero-subtitle">
                        <WelcomeMessage userName={userName} />
                    </div>
                    <div className="dh-hero-actions">
                        <button
                            className="dh-hero-button primary"
                            onClick={() => navigate('/rooms')}
                        >
                            <FiZap /> Bắt đầu ngay
                        </button>
                        <button
                            className="dh-hero-button secondary"
                            onClick={() => navigate('/leaderboard')}
                        >
                            <FiTrendingUp /> Xem xếp hạng
                        </button>
                    </div>
                </div>
                <div className="dh-hero-right">
                    <div className="dh-hero-decoration">
                        <div className="dh-brain-icon-container">
                            <GiBrain className="dh-brain-icon-main" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default HeroSection;