import React from 'react';
import '../../styles/components/room/game-start-popup.css';
import { Clover, Gamepad2 } from 'lucide-react';

/**
 * GameStartPopup - Popup thông báo khi game bắt đầu
 */
const GameStartPopup = ({ onClose }) => {
    React.useEffect(() => {
        const timer = setTimeout(() => {
            onClose?.();
        }, 2500);
        return () => clearTimeout(timer);
    }, [onClose]);

    return (
        <div className="game-start-overlay">
            <div className="game-start-popup">
                <div className="game-start-icon"><Gamepad2 size={48} /></div>
                <h2 className="game-start-title">Game Bắt Đầu!</h2>
                <p className="game-start-message">Chúc bạn may mắn! <Clover size={18} /></p>
                <div className="game-start-loader">
                    <div className="loader-bar"></div>
                </div>
            </div>
        </div>
    );
};

export default GameStartPopup;

