import {FaRunning} from 'react-icons/fa';
import {FiAward, FiClock, FiTarget, FiUsers} from 'react-icons/fi';
import '../../styles/components/profile/profile-stats.css';
import '../../styles/components/profile/profile-stats-loading.css';
import { useStats } from '../../hooks/use-stats';
import { formatScore, formatRank, formatTime } from '../../utils/stats-utils';

const ProfileStats = () => {
    const { stats, loading: statsLoading, error: statsError } = useStats('profile');
    
    const displayStats = stats || {};

    return (
        <>
            {/* Main Stats Section */}
            <div className="pf-stats-section">
                <h2 className="pf-section-title">
                    <FiTarget className="pf-section-icon" />
                    Thống kê & Thành tích
                </h2>

                {statsLoading ? (
                    <div className="pf-stats-loading">Đang tải thống kê...</div>
                ) : statsError ? (
                    <div className="pf-stats-error">
                        <p>Lỗi: {statsError}</p>
                    </div>
                ) : (
                    <>
                        {/* Stats Grid */}
                        <div className="pf-stats-grid">
                            <div className="pf-stat-card pf-stat-primary">
                                <FiAward className="pf-stat-icon" />
                                <div className="pf-stat-value">{formatScore(displayStats.highestScore)}</div>
                                <div className="pf-stat-label">Điểm cao nhất</div>
                            </div>
                            <div className="pf-stat-card pf-stat-secondary">
                                <FiUsers className="pf-stat-icon" />
                                <div className="pf-stat-value">{formatRank(displayStats.highestRank)}</div>
                                <div className="pf-stat-label">Xếp hạng cao nhất</div>
                            </div>
                            <div className="pf-stat-card pf-stat-accent">
                                <FiClock className="pf-stat-icon" />
                                <div className="pf-stat-value">{formatTime(displayStats.fastestTime)}</div>
                                <div className="pf-stat-label">Thời gian nhanh nhất</div>
                            </div>
                            <div className="pf-stat-card pf-stat-info">
                                <FaRunning className="pf-stat-icon" />
                                <div className="pf-stat-value">{displayStats.bestTopic || 'N/A'}</div>
                                <div className="pf-stat-label">Chủ đề tốt nhất</div>
                            </div>
                        </div>
                    </>
                )}


            </div>
        </>
    );
};

export default ProfileStats;