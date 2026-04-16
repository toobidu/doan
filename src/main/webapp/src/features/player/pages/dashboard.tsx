import "../../../styles/pages/dashboard.css";
import { useEffect } from 'react';
import authStore from '../../../stores/auth-store';
import profileApi from '../../../services/profile-api';
import { useStats } from '../../../hooks/use-stats';
import Decoration from '../../../shared/components/Decoration';
import HeroSection from '../../../pages/dashboard/hero-section';
import QuickActionsSection from '../../../pages/dashboard/quick-actions-section';
import StatsSection from '../../../pages/dashboard/stats-section';

function Dashboard() {
    const { user, isAuthenticated } = authStore();
    const { stats: apiStats, loading: statsLoading } = useStats('dashboard');

    useEffect(() => {
        const fetchUserProfile = async () => {
            try {
                await profileApi.getMyProfile();
            } catch {
            }
        };

        if (isAuthenticated && !user) {
            fetchUserProfile();
        }
    }, [isAuthenticated, user]);

    // Prepare user data for components
    const userName = user?.username || 'User';
    const stats = apiStats || {
        gamesPlayed: 0,
        highScore: 0,
        rank: 0
    };

    return (
        <div className="mp-main-layout">
            <Decoration />
            <main className="mp-main-content">
                <HeroSection userName={userName} />
                <StatsSection stats={stats} loading={statsLoading} />
                <QuickActionsSection />
            </main>
        </div>
    );
}

export default Dashboard;