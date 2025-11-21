import { useState, useEffect } from 'react';
import LoginForm from './auth/LoginForm';
import RegisterForm from './auth/RegisterForm';
import ProductList from './product/ProductList';
import { login } from '../services/authApi';
import { register } from '../services/authApi';
import { ToastProvider, useToast } from './ToastContainer';

function AppContent() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [user, setUser] = useState(null);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const { showSuccess, showError } = useToast();

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (token && savedUser) {
      setIsAuthenticated(true);
      setUser(JSON.parse(savedUser));
    }
  }, []);

  const handleLogin = async (credentials) => {
    try {
      const response = await login(credentials);
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user || { email: credentials.email }));
      setIsAuthenticated(true);
      setUser(response.user || { email: credentials.email });
      showSuccess(`Welcome back, ${credentials.email}! 🎉`);
    } catch (err) {
      showError(err.message || 'Login failed. Please check your credentials.');
    }
  };

  const handleRegister = async (userData) => {
    try {
      const response = await register(userData);
      showSuccess('Account created successfully! Please sign in. ✨');
      setShowRegister(false);
    } catch (err) {
      showError(err.message || 'Registration failed. Please try again.');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
    setUser(null);
    showSuccess('Logged out successfully. See you soon! 👋');
  };

  return (
    <div className="App">
      {!isAuthenticated ? (
        <div className="auth-page">
          <div className="auth-container">
            <div className="auth-header">
              <div className="logo">
                <div className="logo-icon">F</div>
                <span>Flogin</span>
              </div>
              <p className="tagline">Product Management System</p>
            </div>
            
            {showRegister ? (
              <RegisterForm 
                onSubmit={handleRegister}
                onSwitchToLogin={() => setShowRegister(false)}
              />
            ) : (
              <LoginForm 
                onSubmit={handleLogin}
                onSwitchToRegister={() => setShowRegister(true)}
              />
            )}
            
            <div className="auth-footer">
              <p>&copy; 2025 Flogin. All rights reserved.</p>
            </div>
          </div>
        </div>
      ) : (
        <div className="main-container">
          <header className="app-header">
            <div className="header-content">
              <div className="header-left">
                <div className="logo">
                  <div className="logo-icon">F</div>
                  <span>Flogin</span>
                </div>
                <h1>Product Management</h1>
              </div>
              <div className="header-right">
                <div className="user-menu-wrapper">
                  <button 
                    className="user-menu-trigger"
                    onClick={() => setShowUserMenu(!showUserMenu)}
                  >
                    <div className="user-avatar">{user?.email?.[0]?.toUpperCase() || 'U'}</div>
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" className={showUserMenu ? 'rotate' : ''}>
                      <path d="M4 6L8 10L12 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                  </button>
                  {showUserMenu && (
                    <div className="user-dropdown">
                      <div className="dropdown-header">
                        <div className="user-avatar-large">{user?.email?.[0]?.toUpperCase() || 'U'}</div>
                        <div className="user-details">
                          <div className="user-email">{user?.email || 'User'}</div>
                          <div className="user-role">Account</div>
                        </div>
                      </div>
                      <div className="dropdown-divider"></div>
                      <button className="dropdown-item" onClick={handleLogout}>
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                          <path d="M6 14H3.33333C2.59695 14 2 13.403 2 12.6667V3.33333C2 2.59695 2.59695 2 3.33333 2H6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                          <path d="M10.6667 11.3333L14 8M14 8L10.6667 4.66667M14 8H6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        Logout
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </header>
          <main>
            <ProductList />
          </main>
        </div>
      )}
    </div>
  );
}

function App() {
  return (
    <ToastProvider>
      <AppContent />
    </ToastProvider>
  );
}

export default App;