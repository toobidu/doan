// Routes dành cho Player (người chơi)

import GameRoom from "../../components/room/GameRoom";
import WaitingRoom from "../../components/room/WaitingRoom";
import Dashboard from "../../features/player/pages/dashboard";
import Leaderboard from "../../features/player/pages/leaderboard";
import RoomPage from "../../features/player/pages/room/room-page";
import RoleBasedRoute from "../guards/role-based-route";

const playerRoutes = [
  {
    path: "dashboard",
    element: (
      <RoleBasedRoute allowedRoles={["PLAYER"]}>
        <Dashboard />
      </RoleBasedRoute>
    ),
  },
  {
    path: "rooms",
    element: (
      <RoleBasedRoute allowedRoles={["PLAYER"]}>
        <RoomPage />
      </RoleBasedRoute>
    ),
  },
  {
    path: "waiting-room/:roomCode",
    element: (
      <RoleBasedRoute allowedRoles={["PLAYER"]}>
        <WaitingRoom />
      </RoleBasedRoute>
    ),
  },
  {
    path: "game/:roomCode",
    element: (
      <RoleBasedRoute allowedRoles={["PLAYER"]}>
        <GameRoom />
      </RoleBasedRoute>
    ),
  },
  {
    path: "leaderboard",
    element: (
      <RoleBasedRoute allowedRoles={["PLAYER"]}>
        <Leaderboard />
      </RoleBasedRoute>
    ),
  },
];

export default playerRoutes;
