import React, { useState } from "react";
import Login from "./components/Login";
import Register from "./components/Register";
import { PlaylistProvider } from "./context/PlaylistContext";
import Tabs from "./components/Tabs";

function App() {
  const [loggedIn, setLoggedIn] = useState(false);
  const [showRegister, setShowRegister] = useState(false);

  const handleLogin = () => setLoggedIn(true);
  const handleLogout = () => {
    localStorage.removeItem("token");
    setLoggedIn(false);
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        backgroundColor: "#000",
        color: "#fff",
        padding: "20px",
        fontFamily: "Arial, sans-serif",
      }}
    >
      {!loggedIn ? (
        showRegister ? (
          <>
            <Register onRegister={() => setShowRegister(false)} />
            <p style={{ textAlign: "center" }}>
              Already have an account?{" "}
              <button
                onClick={() => setShowRegister(false)}
                style={{
                  padding: "5px 10px",
                  borderRadius: "5px",
                  border: "none",
                  backgroundColor: "#ed859f",
                  color: "#fff",
                  cursor: "pointer",
                }}
              >
                Login
              </button>
            </p>
          </>
        ) : (
          <>
            <Login onLogin={handleLogin} />
            <p style={{ textAlign: "center" }}>
              Don't have an account?{" "}
              <button
                onClick={() => setShowRegister(true)}
                style={{
                  padding: "5px 10px",
                  borderRadius: "5px",
                  border: "none",
                  backgroundColor: "#ed859f",
                  color: "#fff",
                  cursor: "pointer",
                }}
              >
                Register
              </button>
            </p>
          </>
        )
      ) : (
        <>
          <h2 style={{ textAlign: "center" }}>Welcome! You are logged in ✅</h2>
          <div style={{ textAlign: "center", marginBottom: "30px" }}>
            <button
              onClick={handleLogout}
              style={{
                padding: "10px 20px",
                border: "none",
                borderRadius: "5px",
                backgroundColor: "#ed859f",
                color: "#fff",
                cursor: "pointer",
              }}
            >
              Logout
            </button>
          </div>

          {/* Music App */}
          <PlaylistProvider>
            <Tabs />
          </PlaylistProvider>
        </>
      )}
    </div>
  );
}

export default App;