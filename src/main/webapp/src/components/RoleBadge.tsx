import authStore from "../stores/auth-store";
import { formatRoleName, getRoleColor, getRoleIcon } from "../utils/role-utils";
import "./RoleBadge";
import { Gamepad2, GraduationCap, Shield, User } from "lucide-react";

const ROLE_ICON_COMPONENTS = {
  gamepad: Gamepad2,
  "graduation-cap": GraduationCap,
  shield: Shield,
  user: User,
};

/**
 * RoleBadge - Component hiển thị badge role của user
 * Có thể dùng trong header, profile, etc.
 */
const RoleBadge = ({ showIcon = true, showText = true, size = "medium" }) => {
  const typeAccount = authStore((state) => state.typeAccount);

  if (!typeAccount) return null;

  const roleName = formatRoleName(typeAccount);
  const roleColor = getRoleColor(typeAccount);
  const roleIcon = getRoleIcon(typeAccount);
  const RoleIcon = ROLE_ICON_COMPONENTS[roleIcon] || User;

  return (
    <div
      className={`role-badge role-badge--${size}`}
      style={{ backgroundColor: roleColor }}
      title={roleName}
    >
      {showIcon && <span className="role-badge__icon"><RoleIcon size={14} /></span>}
      {showText && <span className="role-badge__text">{roleName}</span>}
    </div>
  );
};

export default RoleBadge;
