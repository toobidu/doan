import { useEffect } from 'react';
import { CheckCircle2, Flame, HandMetal, X, XCircle } from 'lucide-react';
import '../../styles/components/room/answer-result-popup.css';

/**
 * AnswerResultPopup - Hiển thị kết quả trả lời câu hỏi với animation đẹp mắt
 */
const AnswerResultPopup = ({ result, onClose }) => {
    useEffect(() => {
        // Tự động đóng sau 3 giây nếu không có props onClose
        if (!onClose) return;

        const timer = setTimeout(() => {
            onClose();
        }, 3000);

        return () => clearTimeout(timer);
    }, [onClose]);

    if (!result) return null;

    const { isCorrect, score, streak } = result;

    // Lấy icon và message dựa trên kết quả
    const getResultInfo = () => {
        if (isCorrect) {
            return {
                icon: <CheckCircle2 />,
                title: 'Đúng rồi!',
                bgColor: '#4CAF50',
                animation: 'popup-bounce-in'
            };
        } else {
            return {
                icon: <XCircle />,
                title: 'Sai rồi!',
                bgColor: '#f44336',
                animation: 'popup-shake'
            };
        }
    };

    // Lấy streak badge
    const getStreakBadge = () => {
        if (!isCorrect || !streak || streak < 3) return null;

        if (streak >= 10) {
            return {
                text: <><Flame /><Flame /><Flame /> UNSTOPPABLE!</>,
                color: '#FF1744',
                label: 'x2.0 Bonus'
            };
        } else if (streak >= 5) {
            return {
                text: <><Flame /><Flame /> ON FIRE!</>,
                color: '#FF6F00',
                label: 'x1.5 Bonus'
            };
        } else if (streak >= 3) {
            return {
                text: <><Flame /> FIRE STREAK!</>,
                color: '#FFC400',
                label: 'x1.2 Bonus'
            };
        }
        return null;
    };

    const resultInfo = getResultInfo();
    const streakBadge = getStreakBadge();

    return (
        <div className="answer-result-overlay">
            <div
                className={`answer-result-popup ${resultInfo.animation}`}
                style={{ backgroundColor: resultInfo.bgColor }}
            >
                <div className="popup-icon">{resultInfo.icon}</div>
                <h2 className="popup-title">{resultInfo.title}</h2>

                {isCorrect && (
                    <div className="popup-score">
                        <div className="score-value">+{score}</div>
                        <div className="score-label">điểm</div>
                    </div>
                )}

                {streakBadge && (
                    <div className="popup-streak" style={{ backgroundColor: `${streakBadge.color}33` }}>
                        <div className="streak-text" style={{ color: streakBadge.color }}>
                            {streakBadge.text}
                        </div>
                        <div className="streak-label">{streakBadge.label}</div>
                        <div className="streak-count">Streak: {streak}</div>
                    </div>
                )}

                {!isCorrect && (
                    <div className="popup-message">
                        Đừng lo! Cố gắng ở câu tiếp theo nhé! <HandMetal />
                    </div>
                )}

                <button className="popup-close-btn" onClick={onClose}>
                    <X />
                </button>
            </div>
        </div>
    );
};

export default AnswerResultPopup;
