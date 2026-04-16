package org.example.quizizz.service.Implement;

import org.example.quizizz.common.config.JwtConfig;
import org.example.quizizz.common.constants.MessageCode;
import org.example.quizizz.common.constants.PermissionCode;
import org.example.quizizz.common.constants.RoleCode;
import org.example.quizizz.common.constants.SystemFlag;
import org.example.quizizz.common.exception.ApiException;
import org.example.quizizz.mapper.UserMapper;
import org.example.quizizz.model.dto.authentication.*;
import org.example.quizizz.model.entity.User;
import org.example.quizizz.model.entity.UserRole;
import org.example.quizizz.repository.PermissionRepository;
import org.example.quizizz.repository.RoleRepository;
import org.example.quizizz.repository.UserRepository;
import org.example.quizizz.repository.UserRoleRepository;
import org.example.quizizz.security.JwtUtil;
import org.example.quizizz.service.Interface.IAuthService;
import org.example.quizizz.service.Interface.IEmailService;
import org.example.quizizz.service.Interface.IRedisService;
import org.example.quizizz.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImplement implements IAuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final IRedisService redisService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final IEmailService emailService;
    private final PasswordGenerator passwordGenerator;
    private final JwtConfig jwtConfig;
    private final PlayerProfileServiceImplement playerProfileService;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT.value(), MessageCode.USER_ALREADY_EXISTS, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT.value(), MessageCode.USER_ALREADY_EXISTS, "Email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ApiException(HttpStatus.CONFLICT.value(), MessageCode.USER_ALREADY_EXISTS, "Phone number already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setSystemFlag(SystemFlag.NORMAL.getValue());
        user.setEmailVerified(false);
        
        // Lưu user trước để có ID
        User savedUser = userRepository.save(user);
        
        // Tạo verification token sau khi đã có userId
        String verificationToken = jwtUtil.generateAccessToken(savedUser.getId(), "VERIFICATION", "NONE");
        savedUser.setVerificationToken(verificationToken);
        savedUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24)); // Token hết hạn sau 24 giờ
        savedUser = userRepository.save(savedUser);

        // Gán role PLAYER mặc định
        var playerRole = roleRepository.findByRoleName(RoleCode.PLAYER.name())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), MessageCode.ROLE_NOT_FOUND, "Default role PLAYER not found"));
        UserRole userRole = new UserRole();
        userRole.setUserId(savedUser.getId());
        userRole.setRoleId(playerRole.getId());
        userRoleRepository.save(userRole);

        // Lưu quyền vào Redis
        refreshUserPermissionsInRedis(savedUser.getId());

        // Khởi tạo player profile cho user mới
        try {
            playerProfileService.initializeProfile(savedUser.getId(), 18); // Default age 18
            log.info("Initialized player profile for new user {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Error initializing player profile: {}", e.getMessage());
        }

        // Gửi email xác thực
        try {
            boolean emailSent = emailService.sendVerificationEmail(
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getVerificationToken()
            );
            if (!emailSent) {
                log.error("Failed to send verification email to {}", savedUser.getEmail());
            } else {
                log.info("Verification email sent successfully to {}", savedUser.getEmail());
            }
        } catch (Exception e) {
            log.error("Error sending verification email: {}", e.getMessage());
        }

        return userMapper.toRegisterResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED.value(), MessageCode.USER_NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), MessageCode.AUTH_PASSWORD_INCORRECT, "Incorrect password");
        }
        
        if (!user.getEmailVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN.value(), MessageCode.AUTH_EMAIL_NOT_VERIFIED, "Email not verified. Please check your email to verify your account.");
        }
        user.setOnline(true);
        userRepository.save(user);
        redisService.setUserOnline(user.getId());

        // Làm mới quyền trong Redis mỗi lần login
        refreshUserPermissionsInRedis(user.getId());

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getTypeAccount(), "BRONZE");
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return userMapper.toLoginResponse(user, accessToken, refreshToken);
    }

    @Override
    public void logout(String token) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), MessageCode.USER_NOT_FOUND));
            user.setOnline(false);
            user.setLastOnlineTime(LocalDateTime.now());
            userRepository.save(user);
            redisService.setUserOffline(userId);

            // Lấy thời gian hết hạn của refresh token từ config
            long refreshTokenExpiration = System.currentTimeMillis() + jwtConfig.getRefreshExpiration();
            redisService.addTokenToBlacklistWithRefreshTTL(token, refreshTokenExpiration);

        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(), MessageCode.AUTH_INVALID_TOKEN, "Invalid token");
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        try {
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED.value(), MessageCode.AUTH_INVALID_TOKEN, "Invalid or expired refresh token");
            }
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), MessageCode.USER_NOT_FOUND));

            // Làm mới quyền khi refresh token
            refreshUserPermissionsInRedis(userId);

            String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getTypeAccount(), "BRONZE");
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

            return userMapper.toLoginResponse(user, newAccessToken, newRefreshToken);

        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED.value(), MessageCode.AUTH_INVALID_TOKEN, "Invalid refresh token");
        }
    }

    @Override
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        try {
            // 1. Tìm user theo email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(),
                            MessageCode.USER_NOT_FOUND, "User not found with email: " + request.getEmail()));

            // 2. Generate password mới
            String newPassword = passwordGenerator.generateSecurePassword();

            // 3. Hash và cập nhật password trong database
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);

            // 4. Logout tất cả devices của user này
            logoutAllDevices(user.getId());

            // 5. Gửi email với password mới
            boolean emailSent = emailService.sendPasswordResetEmail(
                    request.getEmail(),
                    user.getUsername(),
                    newPassword
            );

            if (!emailSent) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        MessageCode.AUTH_EMAIL_SEND_FAILED, "Failed to send reset password email");
            }

            return new ResetPasswordResponse("Password reset successfully. Please check your email for the new password.", request.getEmail());

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    MessageCode.AUTH_PASSWORD_RESET_FAILED, "Password reset failed");
        }
    }

    @Override
    public ChangePasswordResponse changePassword(Long userId, ChangePasswordRequest request) {
        // Validate new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(),
                    MessageCode.AUTH_PASSWORD_MISMATCH, "New password and confirm password do not match");
        }

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(),
                        MessageCode.USER_NOT_FOUND, "User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST.value(),
                    MessageCode.AUTH_PASSWORD_INCORRECT, "Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new ChangePasswordResponse("Password changed successfully", user.getUsername());
    }

    @Override
    public void logoutAllDevices(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(), MessageCode.USER_NOT_FOUND));

            // Set user offline
            user.setOnline(false);
            user.setLastOnlineTime(LocalDateTime.now());
            userRepository.save(user);

            // Xóa user khỏi Redis online users
            redisService.setUserOffline(userId);

            // Xóa cache permissions của user (force refresh khi login lại)
            redisService.deleteUserPermissionsCache(userId);

            // Note: Trong thực tế, ta cần blacklist tất cả JWT tokens của user này
            // Nhưng vì JWT là stateless, cách đơn giản nhất là thay đổi secret key hoặc
            // lưu thời gian logout trong database và kiểm tra khi validate token
            // Ở đây ta sẽ implement cách đơn giản bằng cách lưu thời gian logout

        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    MessageCode.INTERNAL_ERROR, "Failed to logout all devices");
        }
    }

    @Override
    public boolean verifyEmail(String token) {
        try {
            // Tìm user theo verification token
            User user = userRepository.findByVerificationToken(token)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND.value(),
                            MessageCode.USER_NOT_FOUND, "Invalid verification token"));

            // Kiểm tra token đã hết hạn chưa
            if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(),
                        MessageCode.AUTH_INVALID_TOKEN, "Verification token has expired");
            }

            // Kiểm tra email đã được xác thực chưa
            if (user.getEmailVerified()) {
                throw new ApiException(HttpStatus.BAD_REQUEST.value(),
                        MessageCode.USER_ALREADY_EXISTS, "Email already verified");
            }

            // Cập nhật trạng thái xác thực
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            userRepository.save(user);

            return true;

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    MessageCode.INTERNAL_ERROR, "Email verification failed");
        }
    }

    /**
     * Làm mới quyền người dùng trong Redis
     */
    private void refreshUserPermissionsInRedis(Long userId) {
        try {
            // Lấy danh sách permission từ database (dạng "user:manage_profile")
            Set<String> permissionNames = permissionRepository.findPermissionsByUserId(userId)
                    .stream()
                    .map(p -> p.getPermissionName())
                    .collect(Collectors.toSet());

            // Chuyển từ code sang enum PermissionCode
            Set<PermissionCode> permissionCodes = permissionNames.stream()
                    .map(code -> PermissionCode.fromCode(code).orElse(null))
                    .filter(p -> p != null)
                    .collect(Collectors.toSet());

            // Lưu vào Redis
            redisService.saveUserPermissions(userId, permissionCodes);

        } catch (Exception e) {
            log.error("Failed to refresh user permissions cache for userId={}", userId, e);
        }
    }

    @Override
    public Long countUsers() {
        return userRepository.count();
    }
}
