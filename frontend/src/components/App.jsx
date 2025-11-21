import { useState, useEffect } from 'react';
import { Lock } from 'lucide-react';
import AuthModal from './auth/AuthModal';
import ProductList from './product/ProductList';
import { authService } from '../services/authApi';
import { ToastProvider, useToast } from './ToastContainer';

function AppContent() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [user, setUser] = useState(null);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const { showToast } = useToast();

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (token && savedUser) {
      setIsAuthenticated(true);
      setUser(JSON.parse(savedUser));
    }
  }, []);

  const handleAuthSuccess = () => {
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    if (token && savedUser) {
      setIsAuthenticated(true);
      setUser(JSON.parse(savedUser));
    }
  };

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setUser(null);
    showToast('Logged out successfully', 'success');
  };

  const openAuthModal = () => {
    setShowAuthModal(true);
  };

  return (
    <div className="App">
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
              {isAuthenticated ? (
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
              ) : (
                <button className="btn-primary" onClick={openAuthModal}>
                  <Lock size={16} />
                  Sign In
                </button>
              )}
            </div>
          </div>
        </header>
        <main>
          {isAuthenticated ? (
            <ProductList />
          ) : (
            <div className="locked-content">
              <div className="locked-overlay">
                <div className="locked-icon">
                  <Lock size={64} strokeWidth={2} />
                </div>
                <h2>Content Locked</h2>
                <p>Please sign in to access the product management system</p>
                <button className="btn-primary btn-large" onClick={openAuthModal}>
                  <Lock size={20} />
                  Sign In to Continue
                </button>
              </div>
              <div className="blurred-products">
                <div className="product-header">
                  <h2>Products</h2>
                  <div className="product-header-actions">
                    <div className="view-mode-toggle">
                      <button className="view-mode-btn active">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                          <rect x="2" y="2" width="6" height="6" />
                          <rect x="12" y="2" width="6" height="6" />
                          <rect x="2" y="12" width="6" height="6" />
                          <rect x="12" y="12" width="6" height="6" />
                        </svg>
                      </button>
                      <button className="view-mode-btn">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
                          <rect x="2" y="3" width="16" height="2" />
                          <rect x="2" y="9" width="16" height="2" />
                          <rect x="2" y="15" width="16" height="2" />
                        </svg>
                      </button>
                    </div>
                    <button className="btn-add" disabled>
                      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                        <path d="M8 3V13M3 8H13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                      </svg>
                      Add Product
                    </button>
                  </div>
                </div>
                <div className="products-grid grid-large">
                  {[1, 2, 3, 4, 5, 6].map(i => (
                    <div key={i} className="product-card">
                      <h3>Product Name</h3>
                      <p>Product description goes here</p>
                      <div className="product-price">$99.99</div>
                      <div className="product-stock">In Stock: 10</div>
                      <div className="product-actions">
                        <button className="btn-edit" disabled>Edit</button>
                        <button className="btn-delete" disabled>Delete</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </main>
      </div>

      <AuthModal 
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
        onSuccess={handleAuthSuccess}
      />
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