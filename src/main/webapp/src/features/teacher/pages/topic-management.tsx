import { useState, useEffect, useCallback, type FormEvent } from 'react';
import {
    FiChevronLeft,
    FiChevronRight,
    FiEdit2,
    FiPlus,
    FiSearch,
    FiTrash2
} from 'react-icons/fi';
import { toast } from 'react-toastify';
import teacherApi from '../services/teacher-api';
import '../../../styles/features/teacher/management.css';

const TopicManagement = () => {
    const [topics, setTopics] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [editingTopic, setEditingTopic] = useState<any | null>(null);
    const [formData, setFormData] = useState({ name: '', description: '' });
    
    // Pagination states
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');

    const loadTopics = useCallback(async () => {
        try {
            setLoading(true);
            const sortParam = `name,${sortOrder}`;
            const response = await teacherApi.searchTopics(searchTerm, currentPage, 10, sortParam);
            
            if (response.data) {
                const pageData = response.data;
                setTopics(pageData.content || []);
                setTotalPages(pageData.totalPages || 0);
                setTotalElements(pageData.totalElements || 0);
            }
        } catch (error) {
            console.error('Error loading topics:', error);
            toast.error('Không thể tải danh sách chủ đề');
            setTopics([]);
        } finally {
            setLoading(false);
        }
    }, [currentPage, sortOrder, searchTerm]);

    useEffect(() => {
        loadTopics();
    }, [loadTopics]);

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        try {
            if (editingTopic) {
                await teacherApi.updateTopic(editingTopic.id, formData);
                toast.success('Cập nhật chủ đề thành công');
            } else {
                await teacherApi.createTopic(formData);
                toast.success('Tạo chủ đề thành công');
            }
            setShowModal(false);
            setFormData({ name: '', description: '' });
            setEditingTopic(null);
            // Reset về trang đầu sau khi tạo/cập nhật
            setCurrentPage(0);
            loadTopics();
        } catch {
            toast.error(editingTopic ? 'Không thể cập nhật chủ đề' : 'Không thể tạo chủ đề');
        }
    };

    const handleSearch = useCallback((value) => {
        setSearchTerm(value);
        setCurrentPage(0); // Reset về trang đầu khi search
    }, []);

    const handleSort = () => {
        setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        setCurrentPage(0);
    };

    const handlePageChange = (newPage: number) => {
        if (newPage >= 0 && newPage < totalPages) {
            setCurrentPage(newPage);
        }
    };

    const handleEdit = (topic: any) => {
        setEditingTopic(topic);
        setFormData({ name: topic.name, description: topic.description });
        setShowModal(true);
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm('Bạn có chắc muốn xóa chủ đề này?')) return;
        try {
            await teacherApi.deleteTopic(id);
            toast.success('Xóa chủ đề thành công');
            loadTopics();
        } catch {
            toast.error('Không thể xóa chủ đề');
        }
    };

    const getSortIcon = () => {
        return sortOrder === 'asc' ? '↑' : '↓';
    };

    const getRowNumber = (index: number) => {
        return currentPage * 10 + index + 1;
    };

    if (loading && topics.length === 0) return <div className="loading">Đang tải...</div>;

    return (
        <div className="management-page">
            <div className="management-header">
                <h1>Quản lý Chủ đề</h1>
                <button className="btn-primary" onClick={() => { setShowModal(true); setEditingTopic(null); setFormData({ name: '', description: '' }); }}>
                    <FiPlus /> Thêm chủ đề
                </button>
            </div>

            <div className="filter-section">
                <div className="search-bar-compact">
                    <FiSearch />
                    <input
                        type="text"
                        placeholder="Tìm kiếm theo tên hoặc mô tả..."
                        value={searchTerm}
                        onChange={(e) => handleSearch(e.target.value)}
                    />
                </div>
            </div>

            <div className="table-info">
                <span>Hiển thị {topics.length > 0 ? currentPage * 10 + 1 : 0} - {Math.min((currentPage + 1) * 10, totalElements)} trong tổng số {totalElements} chủ đề</span>
            </div>

            <div className="table-container">
                <table>
                    <thead>
                        <tr>
                            <th style={{ width: '80px' }}>STT</th>
                            <th style={{ cursor: 'pointer' }} onClick={handleSort}>
                                Tên chủ đề {getSortIcon()}
                            </th>
                            <th>Mô tả</th>
                            <th style={{ width: '120px' }}>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr>
                                <td colSpan={4} style={{ textAlign: 'center', padding: '2rem' }}>
                                    <div className="spinner-small"></div>
                                    Đang tải...
                                </td>
                            </tr>
                        ) : topics.length === 0 ? (
                            <tr>
                                <td colSpan={4} style={{ textAlign: 'center', padding: '2rem' }}>
                                    {searchTerm ? 'Không tìm thấy chủ đề phù hợp' : 'Chưa có chủ đề nào'}
                                </td>
                            </tr>
                        ) : (
                            topics.map((topic, index) => (
                                <tr key={topic.id}>
                                    <td style={{ fontWeight: '600', color: 'var(--text-secondary)' }}>
                                        {getRowNumber(index)}
                                    </td>
                                    <td style={{ fontWeight: '600' }}>{topic.name}</td>
                                    <td style={{ maxWidth: '400px' }}>{topic.description}</td>
                                    <td>
                                        <div className="action-buttons">
                                            <button className="btn-edit" onClick={() => handleEdit(topic)} title="Sửa">
                                                <FiEdit2 />
                                            </button>
                                            <button className="btn-delete" onClick={() => handleDelete(topic.id)} title="Xóa">
                                                <FiTrash2 />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {totalPages > 1 && (
                <div className="pagination">
                    <button 
                        className="pagination-btn" 
                        onClick={() => handlePageChange(currentPage - 1)}
                        disabled={currentPage === 0}
                    >
                        <FiChevronLeft /> Trước
                    </button>
                    
                    <div className="pagination-pages">
                        {[...Array(totalPages)].map((_, index) => {
                            // Hiển thị trang đầu, cuối và các trang xung quanh trang hiện tại
                            if (
                                index === 0 || 
                                index === totalPages - 1 || 
                                (index >= currentPage - 1 && index <= currentPage + 1)
                            ) {
                                return (
                                    <button
                                        key={index}
                                        className={`pagination-page ${index === currentPage ? 'active' : ''}`}
                                        onClick={() => handlePageChange(index)}
                                    >
                                        {index + 1}
                                    </button>
                                );
                            } else if (index === currentPage - 2 || index === currentPage + 2) {
                                return <span key={index} className="pagination-ellipsis">...</span>;
                            }
                            return null;
                        })}
                    </div>
                    
                    <button 
                        className="pagination-btn" 
                        onClick={() => handlePageChange(currentPage + 1)}
                        disabled={currentPage === totalPages - 1}
                    >
                        Sau <FiChevronRight />
                    </button>
                </div>
            )}

            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <h2>{editingTopic ? 'Cập nhật chủ đề' : 'Thêm chủ đề mới'}</h2>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>Tên chủ đề *</label>
                                <input
                                    type="text"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Mô tả</label>
                                <textarea
                                    value={formData.description}
                                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                    rows={4}
                                />
                            </div>
                            <div className="modal-actions">
                                <button type="button" className="btn-cancel" onClick={() => setShowModal(false)}>Hủy</button>
                                <button type="submit" className="btn-submit">
                                    {editingTopic ? 'Cập nhật' : 'Tạo mới'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default TopicManagement;
