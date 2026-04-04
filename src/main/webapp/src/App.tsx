import { useState, useEffect } from "react";
import authStore from "./stores/auth-store";
import 'react-toastify/dist/ReactToastify.css';


function App() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    // Initialize auth khi app khởi động
    authStore.getState().initialize();
  }, []);

  return (
    <>
      <Header />
      <Outlet />
      <Footer />
      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="light"
      />
    </>
  );
}

export default App;