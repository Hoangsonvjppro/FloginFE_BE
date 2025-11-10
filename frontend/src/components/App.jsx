import { useState } from 'react';
import LoginForm from './auth/LoginForm';
import { login } from '../services/authApi';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (credentials) => {
    try {
      const response = await login(credentials);
      localStorage.setItem('token', response.token);
      setIsAuthenticated(true);
    } catch (err) {
      setError('Login failed. Please check your credentials.');
    }
  };

  return (
    <div className="App">
      {!isAuthenticated ? (
        <LoginForm onSubmit={handleLogin} />
      ) : (
        <h1>Welcome! (Product list coming soon...)</h1>
      )}
      {error && <div className="error">{error}</div>}
    </div>
  );
}

export default App;