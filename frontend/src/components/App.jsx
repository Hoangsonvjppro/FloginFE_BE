import { useState, useEffect } from 'react';
import LoginForm from './auth/LoginForm';
import ProductList from './product/ProductList';
import { login } from '../services/authApi';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [error, setError] = useState('');
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
      const response = await login(credentials);
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user || { email: credentials.email }));
      setIsAuthenticated(true);
      setUser(response.user || { email: credentials.email });
      setError('');
    } catch (err) {
      setError('Login failed. Please check your credentials.');
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
        <div className="login-container">
          <h1>Flogin Product Management</h1>
          <LoginForm onSubmit={handleLogin} />
          {error && <div className="error">{error}</div>}
        </div>
      ) : (
        <div className="main-container">
          <header className="app-header">
            <h1>Product Management</h1>
            <div className="user-info">
              <span>Welcome, {user?.email || 'User'}</span>
              <button onClick={handleLogout} className="btn-logout">Logout</button>
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