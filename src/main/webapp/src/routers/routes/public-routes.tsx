// Routes công khai (không cần đăng nhập)

const publicRoutes = [
  { index: true, element: <Welcome /> },
  { path: "login", element: <Login /> },
  { path: "register", element: <Register /> },
  { path: "forgot-password", element: <ForgotPassword /> },
  { path: "verify-email", element: <VerifyEmail /> },
  { path: "about", element: <AboutPage /> },
  { path: "blog", element: <BlogPage /> },
  { path: "contact", element: <ContactPage /> },
  { path: "help", element: <HelpPage /> },
  { path: "faq", element: <FaqPage /> },
  { path: "privacy", element: <PrivacyPage /> },
  { path: "terms", element: <TermsPage /> },
  { path: "feedback", element: <FeedbackPage /> },
];

export default publicRoutes;
