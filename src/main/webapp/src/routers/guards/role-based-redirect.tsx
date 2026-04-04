import authStore from "../../stores/auth-store";

/**
 * RoleBasedRedirect - Component tự động điều hướng dựa trên role
 * Redirect user về dashboard tương ứng với role của họ
 */
const RoleBasedRedirect = () => {
  const typeAccount = authStore((state) => state.typeAccount);

  return (
    <ProtectedRoute>
      {typeAccount === "ADMIN" && <Navigate to="/admin/dashboard" replace />}
      {typeAccount === "TEACHER" && <Navigate to="/teacher/dashboard" replace />}
      {typeAccount === "PLAYER" && <Navigate to="/dashboard" replace />}
      {!typeAccount && <Navigate to="/" replace />}
    </ProtectedRoute>
  );
};

export default RoleBasedRedirect;
