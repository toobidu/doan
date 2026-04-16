// Routes dành cho Teacher (giáo viên)

import AIQuestionGenerator from "../../features/teacher/pages/aiquestion-generator";
import ExamManagement from "../../features/teacher/pages/exam-management";
import QuestionManagement from "../../features/teacher/pages/question-management";
import TeacherDashboard from "../../features/teacher/pages/teacher-dashboard";
import RoleBasedRoute from "../guards/role-based-route";

const teacherRoutes = [
  {
    path: "teacher/dashboard",
    element: (
      <RoleBasedRoute allowedRoles={["TEACHER"]}>
        <TeacherDashboard />
      </RoleBasedRoute>
    ),
  },
  {
    path: "teacher/exams",
    element: (
      <RoleBasedRoute allowedRoles={["TEACHER"]}>
        <ExamManagement />
      </RoleBasedRoute>
    ),
  },
  {
    path: "teacher/questions",
    element: (
      <RoleBasedRoute allowedRoles={["TEACHER"]}>
        <QuestionManagement />
      </RoleBasedRoute>
    ),
  },
  {
    path: "teacher/ai-generator",
    element: (
      <RoleBasedRoute allowedRoles={["TEACHER"]}>
        <AIQuestionGenerator />
      </RoleBasedRoute>
    ),
  },
];

export default teacherRoutes;
