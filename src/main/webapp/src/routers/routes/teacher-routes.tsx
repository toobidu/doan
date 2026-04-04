// Routes dành cho Teacher (giáo viên)

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
