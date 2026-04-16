import { useState } from 'react';
import { FaExclamationTriangle } from 'react-icons/fa';
import { FiLoader } from 'react-icons/fi';
import EditActions from '../components/profile/EditActions';
import PasswordChangeModal from '../components/profile/PasswordChangeModal';
import ProfileDetails from '../components/profile/ProfileDetails';
import ProfileHeader from '../components/profile/ProfileHeader';
import ProfileStats from '../components/profile/ProfileStats';
import SuccessModal from '../components/profile/SuccessModal';
import authStore from '../stores/auth-store';
import useDocumentTitle from '../hooks/use-document-title';
import { useProfileData } from '../hooks/use-profile-data';
import { useProfileEdit } from '../hooks/use-profile-edit';
import { usePasswordChange } from '../hooks/use-password-change';
import { useAvatarUpload } from '../hooks/use-avatar-upload';
import Decoration from '../shared/components/Decoration';
import '../styles/pages/profile.css';

function Profile() {
    const { setUser } = authStore();
    const [error, setError] = useState('');
    const [showPasswordModal, setShowPasswordModal] = useState(false);

    // Custom hooks
    const { profileData, loading, isOwnProfile, setProfileData } = useProfileData();
    const profileEdit = useProfileEdit(profileData, setProfileData);
    const passwordChange = usePasswordChange();
    const avatarUpload = useAvatarUpload(profileData);

    useDocumentTitle(isOwnProfile ? 'Hồ sơ của tôi' : `Hồ sơ của ${profileData?.username}`);

    // Handlers
    const handleUpdateProfile = () => {
        profileEdit.handleUpdateProfile(setError, () => {});
    };

    const handleOpenPasswordModal = () => {
        setShowPasswordModal(true);
    };

    const handleClosePasswordModal = () => {
        setShowPasswordModal(false);
        // Reset form errors when closing
        passwordChange.setFormErrors({});
    };

    const handlePasswordSubmit = () => {
        passwordChange.handleChangePassword(setError, () => {});
        if (!passwordChange.updateLoading) {
            setShowPasswordModal(false);
        }
    };

    if (loading) {
        return (
            <div className="pf-loading-container">
                <FiLoader className="pf-loading-spinner" />
                <p>Đang tải thông tin...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="pf-error-container">
                <p className="pf-error-message">{error}</p>
                <button onClick={() => window.history.back()} className="pf-back-button">
                    Quay lại trang chủ
                </button>
            </div>
        );
    }

    return (
        <div className="pf-layout">
            <Decoration />
            <main className="pf-content">
                {(error || avatarUpload.uploadError) && (
                    <div className="pf-error-container">
                        <FaExclamationTriangle />
                        <span>{error || avatarUpload.uploadError}</span>
                    </div>
                )}

                {profileData && (
                    <div className="pf-profile-container">
                        {/* Profile Header */}
                        <ProfileHeader
                            profileData={profileData}
                            isOwnProfile={isOwnProfile}
                            avatarUrl={avatarUpload.avatarUrl}
                            avatarLoading={avatarUpload.avatarLoading}
                            isEditing={profileEdit.isEditing}
                            isChangingPassword={passwordChange.isChangingPassword}
                            setIsEditing={profileEdit.setIsEditing}
                            handleFileSelect={(event) => avatarUpload.handleFileSelect(event, setUser)}
                            uploadingAvatar={avatarUpload.uploadingAvatar}
                            setUser={setUser}
                            handleOpenPasswordModal={handleOpenPasswordModal}
                        />

                        {/* Profile Details */}
                        <ProfileDetails
                            profileData={profileData}
                            isOwnProfile={isOwnProfile}
                            isEditing={profileEdit.isEditing}
                            formData={profileEdit.formData}
                            formErrors={profileEdit.formErrors}
                            handleProfileChange={profileEdit.handleProfileChange}
                        />

                        {/* Edit Actions */}
                        <EditActions
                            isEditing={profileEdit.isEditing}
                            updateLoading={profileEdit.updateLoading || passwordChange.updateLoading}
                            handleUpdateProfile={handleUpdateProfile}
                            setIsEditing={profileEdit.setIsEditing}
                            setIsChangingPassword={passwordChange.setIsChangingPassword}
                            setFormErrors={() => {}} // Có thể implement sau
                        />

                        {/* Profile Stats */}
                        <ProfileStats />
                    </div>
                )}

                {/* Success Modal for Avatar Upload */}
                <SuccessModal
                    show={avatarUpload.showSuccessModal}
                    message={avatarUpload.successMessage}
                    onClose={avatarUpload.closeSuccessModal}
                />

                {/* Password Change Modal */}
                <PasswordChangeModal
                    showModal={showPasswordModal}
                    onClose={handleClosePasswordModal}
                    passwordData={passwordChange.passwordData}
                    formErrors={passwordChange.formErrors}
                    updateLoading={passwordChange.updateLoading}
                    handlePasswordChange={passwordChange.handlePasswordChange}
                    handleSubmit={handlePasswordSubmit}
                />
            </main>
        </div>
    );
}

export default Profile;