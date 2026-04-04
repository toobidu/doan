// Routes dùng chung cho tất cả users (không yêu cầu role cụ thể)

const commonRoutes = [
  {
    path: "profile",
    element: (
      <ProtectedRoute>
        <Profile />
      </ProtectedRoute>
    ),
  },
];

export default commonRoutes;
