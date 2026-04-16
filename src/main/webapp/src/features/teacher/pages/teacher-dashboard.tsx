import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FiBarChart2, FiCpu, FiFileText, FiHelpCircle, FiZap } from 'react-icons/fi';
import teacherApi from '../services/teacher-api';
import '../../../styles/features/teacher/teacher-dashboard.css';
import { Lightbulb } from 'lucide-react';

const TeacherDashboard = () => {
    const [stats, setStats] = useState({ exams: 0, questions: 0 });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadStats = async () => {
            try {
                const data = await teacherApi.getStatistics();
                setStats(data);
            } catch (error) {
                console.error('Error loading stats:', error);
            } finally {
                setLoading(false);
            }
        };
        loadStats();
    }, []);

    return (
        <div className="teacher-dashboard">
            <div className="teacher-header">
                <h1>Bảng điều khiển Giáo viên</h1>
                <p>Quản lý bộ đề, câu hỏi và tạo câu hỏi bằng AI</p>
            </div>

            <div className="teacher-content">
                <div className="teacher-cards">
                    <Link to="/teacher/exams" className="teacher-card">
                        <div className="card-icon">
                            <FiFileText />
                        </div>
                        <div className="card-content">
                            <h3>Quản lý Bộ đề</h3>
                            <p>Tạo, sửa, xóa các bộ đề thi</p>
                        </div>
                    </Link>

                    <Link to="/teacher/questions" className="teacher-card">
                        <div className="card-icon">
                            <FiHelpCircle />
                        </div>
                        <div className="card-content">
                            <h3>Quản lý Câu hỏi</h3>
                            <p>Quản lý câu hỏi và đáp án</p>
                        </div>
                    </Link>

                    <Link to="/teacher/ai-generator" className="teacher-card">
                        <div className="card-icon">
                            <FiCpu />
                        </div>
                        <div className="card-content">
                            <h3>Tạo câu hỏi AI</h3>
                            <p>Sử dụng AI để tạo câu hỏi tự động</p>
                        </div>
                    </Link>
                </div>

                <div className="teacher-sidebar">
                    <div className="sidebar-section">
                        <h3><FiBarChart2 /> Thống kê nhanh trên hệ thống</h3>
                        <div className="quick-stats">
                            <div className="stat-item">
                                <span className="stat-label">Bộ đề</span>
                                <span className="stat-value">{loading ? '...' : stats.exams}</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-label">Câu hỏi</span>
                                <span className="stat-value">{loading ? '...' : stats.questions}</span>
                            </div>
                        </div>
                    </div>

                    <div className="sidebar-section">
                        <h3><FiZap /> Mẹo sử dụng</h3>
                        <ul className="quick-tips">
                            <li><Lightbulb size={14} /> Tạo bộ đề trước khi thêm câu hỏi</li>
                            <li><Lightbulb size={14} /> Dùng AI để tạo câu hỏi nhanh chóng</li>
                            <li><Lightbulb size={14} /> Đặt tên bộ đề rõ ràng, dễ tìm</li>
                            <li><Lightbulb size={14} /> Kiểm tra kỹ trước khi sử dụng</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TeacherDashboard;
