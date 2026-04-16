// Routes dành cho Admin (quản trị viên)

import AdminDashboard from "../../features/admin/pages/admin-dashboard";
import PermissionManagement from "../../features/admin/pages/permission-management";
import RoleManagement from "../../features/admin/pages/role-management";
import TopicManagement from "../../features/admin/pages/topic-management";
import UserManagement from "../../features/admin/pages/user-management";
import RoleBasedRoute from "../guards/role-based-route";

const adminRoutes = [
  {
    path: "admin/dashboard",
    element: (
      <RoleBasedRoute allowedRoles={["ADMIN"]}>
        <AdminDashboard />
      </RoleBasedRoute>
    ),
  },
  {
    path: "admin/users",
    element: (
      <RoleBasedRoute allowedRoles={["ADMIN"]}>
        <UserManagement />
      </RoleBasedRoute>
    ),
  },
  {
    path: "admin/roles",
    element: (
      <RoleBasedRoute allowedRoles={["ADMIN"]}>
        <RoleManagement />
      </RoleBasedRoute>
    ),
  },
  {
    path: "admin/permissions",
    element: (
      <RoleBasedRoute allowedRoles={["ADMIN"]}>
        <PermissionManagement />
      </RoleBasedRoute>
    ),
  },
  {
    path: "admin/topics",
    element: (
      <RoleBasedRoute allowedRoles={["ADMIN"]}>
        <TopicManagement />
      </RoleBasedRoute>
    ),
  },
];

export default adminRoutes;
