import { create } from 'zustand';
import authApi from '../services/auth-api';
import { JwtPayload, jwtDecode } from 'jwt-decode';
import Cookies from 'js-cookie';

type UserRole = 'PLAYER' | 'TEACHER' | 'ADMIN' | null;

type AuthUser = {
  id?: number | string;
  role?: Exclude<UserRole, null>;
  [key: string]: unknown;
} | null;

type DecodedToken = JwtPayload & {
  typeAccount?: Exclude<UserRole, null>;
};

type AuthState = {
  user: AuthUser;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  typeAccount: UserRole;
  setUser: (user: AuthUser) => void;
  clearUser: () => void;
  setLoading: (isLoading: boolean) => void;
  setError: (error: string | null) => void;
  initialize: () => Promise<void>;
  login: (username: string, password: string) => Promise<unknown>;
  logout: () => Promise<void>;
};

const authStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,
  typeAccount: null, // PLAYER, TEACHER, ADMIN

  setUser: (user) =>
      set(() => {
        return {
          user,
          isAuthenticated: !!user,
          typeAccount: user?.role || null,
          error: null,
        };
      }),

  clearUser: () =>
      set({
        user: null,
        isAuthenticated: false,
        typeAccount: null,
        error: null,
      }),

  setLoading: (isLoading) => set({ isLoading }),

  setError: (error) => set({ error }),

  initialize: async () => {
    // Kiểm tra token từ Cookies trước, nếu không có thì từ sessionStorage
    let token = Cookies.get('accessToken');
    let refreshToken = Cookies.get('refreshToken');

    if (!token) {
      token = sessionStorage.getItem('accessToken');
      refreshToken = sessionStorage.getItem('refreshToken');
      // Nếu có token từ sessionStorage, đồng bộ lại vào Cookies
      if (token && refreshToken) {
        Cookies.set('accessToken', token, { expires: 7, secure: true, sameSite: 'strict' });
        Cookies.set('refreshToken', refreshToken, { expires: 7, secure: true, sameSite: 'strict' });
      }
    }

    if (!token || !refreshToken) {
      set({ user: null, isAuthenticated: false });
      return;
    }

    try {
      let decoded: DecodedToken;
      try {
        decoded = jwtDecode<DecodedToken>(token);
      } catch {
        // Token không hợp lệ, clear và return
        Cookies.remove('accessToken');
        Cookies.remove('refreshToken');
        sessionStorage.removeItem('accessToken');
        sessionStorage.removeItem('refreshToken');
        set({ user: null, isAuthenticated: false });
        return;
      }

      if ((decoded.exp ?? 0) * 1000 < Date.now()) {
        // Token hết hạn, thử refresh
        const refreshResponse = await authApi.refreshToken();
        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = refreshResponse.data.data;

        // Cập nhật token mới
        Cookies.set('accessToken', newAccessToken, { expires: 7, secure: true, sameSite: 'strict' });
        Cookies.set('refreshToken', newRefreshToken, { expires: 7, secure: true, sameSite: 'strict' });
        sessionStorage.setItem('accessToken', newAccessToken);
        sessionStorage.setItem('refreshToken', newRefreshToken);

        token = newAccessToken;
      }

      // Set role from decoded token immediately
      const userWithRole = { role: decoded.typeAccount || undefined };
      set({ 
        user: userWithRole, 
        isAuthenticated: true, 
        typeAccount: decoded.typeAccount,
        isLoading: true 
      });
      
      // Then fetch full user data
      try {
        const response = await authApi.getUser();
        const fullUserWithRole = { ...response.data, role: decoded.typeAccount || undefined };
        set({ 
          user: fullUserWithRole, 
          isAuthenticated: true, 
          typeAccount: decoded.typeAccount,
          isLoading: false 
        });
      } catch {
        // If getUser fails, keep the basic user with role
        set({ isLoading: false });
      }
    } catch (error) {
      // Nếu refresh thất bại, clear token và chuyển về login
      Cookies.remove('accessToken');
      Cookies.remove('refreshToken');
      sessionStorage.removeItem('accessToken');
      sessionStorage.removeItem('refreshToken');
      // Chỉ set error nếu đã có token (đã từng đăng nhập)
      if (token) {
        set({ user: null, isAuthenticated: false, error: error.message, isLoading: false });
      } else {
        set({ user: null, isAuthenticated: false, isLoading: false });
      }
    }
  },

  login: async (username, password) => {
    try {
      set({ isLoading: true, error: null });
      const response = await authApi.login({ username, password });
      const { accessToken, refreshToken, ...user } = response.data.data;

      // Decode token to get typeAccount
      const decoded = jwtDecode<DecodedToken>(accessToken);
      const userWithRole = { ...user, role: decoded.typeAccount || undefined };

      // Lưu token vào Cookies và sessionStorage để persist khi reload
      Cookies.set('accessToken', accessToken, { expires: 7, secure: true, sameSite: 'strict' });
      Cookies.set('refreshToken', refreshToken, { expires: 7, secure: true, sameSite: 'strict' });
      sessionStorage.setItem('accessToken', accessToken);
      sessionStorage.setItem('refreshToken', refreshToken);

      set({ 
        user: userWithRole, 
        isAuthenticated: true, 
        typeAccount: decoded.typeAccount,
        isLoading: false 
      });
      return response;
    } catch (error) {
      set({ isLoading: false, error: error.message });
      throw error;
    }
  },

  logout: async () => {
    try {
      set({ isLoading: true, error: null });
      await authApi.logout();
      Cookies.remove('accessToken');
      Cookies.remove('refreshToken');
      sessionStorage.removeItem('accessToken');
      sessionStorage.removeItem('refreshToken');
      set({ 
        user: null, 
        isAuthenticated: false, 
        typeAccount: null,
        isLoading: false 
      });
    } catch (error) {
      set({ isLoading: false, error: error.message });
      throw error;
    }
  },
}));

export default authStore;