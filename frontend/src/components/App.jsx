import { useState, useEffect } from 'react';
import LoginForm from './auth/LoginForm';
import RegisterForm from './auth/RegisterForm';
import ProductList from './product/ProductList';
import { login } from '../services/authApi';
import { register } from '../services/authApi';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [showRegister, setShowRegister] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [user, setUser] = useState(null);

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
      setError('');
      const response = await login(credentials);
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user || { email: credentials.email }));
      setIsAuthenticated(true);
      setUser(response.user || { email: credentials.email });
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    }
  };

  const handleRegister = async (userData) => {
    try {
      setError('');
      const response = await register(userData);
      setSuccess('Account created successfully! Please sign in.');
      setShowRegister(false);
      // Auto-fill email in login form would be nice
    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
    setUser(null);
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
            
            {error && <div className="alert alert-error">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}
            
            {showRegister ? (
              <RegisterForm 
                onSubmit={handleRegister}
                onSwitchToLogin={() => {
                  setShowRegister(false);
                  setError('');
                  setSuccess('');
                }}
              />
            ) : (
              <LoginForm 
                onSubmit={handleLogin}
                onSwitchToRegister={() => {
                  setShowRegister(true);
                  setError('');
                  setSuccess('');
                }}
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
            <div className="header-left">
              <div className="logo">
                <div className="logo-icon">F</div>
                <span>Flogin</span>
              </div>
              <h1>Product Management</h1>
            </div>
            <div className="user-info">
              <div className="user-avatar">{user?.email?.[0]?.toUpperCase() || 'U'}</div>
              <span>{user?.email || 'User'}</span>
              <button onClick={handleLogout} className="btn-logout">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                  <path d="M6 14H3.33333C2.59695 14 2 13.403 2 12.6667V3.33333C2 2.59695 2.59695 2 3.33333 2H6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                  <path d="M10.6667 11.3333L14 8M14 8L10.6667 4.66667M14 8H6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                Logout
              </button>
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

export default App;