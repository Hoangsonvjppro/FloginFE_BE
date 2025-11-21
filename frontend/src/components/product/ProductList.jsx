import { useState, useEffect } from 'react';
import ProductItem from './ProductItem';
import ProductForm from './ProductForm';
import { getAllProducts, createProduct, updateProduct, deleteProduct } from '../../services/productApi';
import { useToast } from '../ToastContainer';
import ConfirmDialog from '../ConfirmDialog';

export default function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [viewMode, setViewMode] = useState('large');
  const { showSuccess, showError } = useToast();

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await getAllProducts();
      setProducts(data);
    } catch (err) {
      showError('Failed to load products. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (productData) => {
    try {
      await createProduct(productData);
      setShowForm(false);
      loadProducts();
      showSuccess(`Product "${productData.name}" created successfully! 🎉`);
    } catch (err) {
      showError('Failed to create product. Please try again.');
    }
  };

  const handleUpdate = async (id, productData) => {
    try {
      await updateProduct(id, productData);
      setEditingProduct(null);
      loadProducts();
      showSuccess(`Product "${productData.name}" updated successfully! ✨`);
    } catch (err) {
      showError('Failed to update product. Please try again.');
    }
  };

  const handleDelete = async (id) => {
    const product = products.find(p => p.id === id);
    setConfirmDelete(product);
  };

  const confirmDeleteProduct = async () => {
    if (!confirmDelete) return;
    
    try {
      await deleteProduct(confirmDelete.id);
      loadProducts();
      showSuccess(`Product "${confirmDelete.name}" deleted successfully! 🗑️`);
    } catch (err) {
      showError('Failed to delete product. Please try again.');
    } finally {
      setConfirmDelete(null);
    }
  };

  const handleEdit = (product) => {
    setEditingProduct(product);
    setShowForm(true);
  };

  const handleCancelEdit = () => {
    setEditingProduct(null);
    setShowForm(false);
  };

  if (loading) return <div className="loading">Loading products...</div>;

  return (
    <div className="product-section">
      <div className="product-header">
        <h2>Products</h2>
        <div className="product-header-actions">
          <div className="view-mode-toggle">
            <button 
              className={`view-mode-btn ${viewMode === 'large' ? 'active' : ''}`}
              onClick={() => setViewMode('large')}
              title="Large view"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <rect x="2" y="2" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.5"/>
                <rect x="11" y="2" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.5"/>
                <rect x="2" y="11" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.5"/>
                <rect x="11" y="11" width="7" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.5"/>
              </svg>
            </button>
            <button 
              className={`view-mode-btn ${viewMode === 'small' ? 'active' : ''}`}
              onClick={() => setViewMode('small')}
              title="Small view"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <rect x="2" y="2" width="16" height="3" rx="1" stroke="currentColor" strokeWidth="1.5"/>
                <rect x="2" y="8.5" width="16" height="3" rx="1" stroke="currentColor" strokeWidth="1.5"/>
                <rect x="2" y="15" width="16" height="3" rx="1" stroke="currentColor" strokeWidth="1.5"/>
              </svg>
            </button>
          </div>
          <button className="btn-add" onClick={() => setShowForm(!showForm)}>
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <path d="M10 4V16M4 10H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            {showForm ? 'Cancel' : 'Add Product'}
          </button>
        </div>
      </div>

      {showForm && (
        <ProductForm
          onSubmit={editingProduct ? (data) => handleUpdate(editingProduct.id, data) : handleCreate}
          onCancel={handleCancelEdit}
          initialData={editingProduct}
        />
      )}

      <div className={`products-grid ${viewMode === 'small' ? 'grid-small' : 'grid-large'}`}>
        {products.length === 0 ? (
          <div className="empty-state">
            <svg width="64" height="64" viewBox="0 0 64 64" fill="none">
              <rect x="8" y="16" width="48" height="40" rx="4" stroke="currentColor" strokeWidth="2"/>
              <path d="M8 28H56M20 36H44M20 44H36" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <p>No products found. Add your first product!</p>
          </div>
        ) : (
          products.map((product) => (
            <ProductItem
              key={product.id}
              product={product}
              onEdit={handleEdit}
              onDelete={handleDelete}
              viewMode={viewMode}
            />
          ))
        )}
      </div>

      <ConfirmDialog
        isOpen={!!confirmDelete}
        title="Delete Product"
        message={`Are you sure you want to delete "${confirmDelete?.name}"? This action cannot be undone.`}
        confirmText="Delete"
        cancelText="Cancel"
        type="danger"
        onConfirm={confirmDeleteProduct}
        onCancel={() => setConfirmDelete(null)}
      />
    </div>
  );
}
