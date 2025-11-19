import { useState, useEffect } from 'react';
import ProductItem from './ProductItem';
import ProductForm from './ProductForm';
import { getAllProducts, createProduct, updateProduct, deleteProduct } from '../../services/productApi';

export default function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await getAllProducts();
      setProducts(data);
      setError('');
    } catch (err) {
      setError('Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (productData) => {
    try {
      await createProduct(productData);
      setShowForm(false);
      loadProducts();
    } catch (err) {
      setError('Failed to create product');
    }
  };

  const handleUpdate = async (id, productData) => {
    try {
      await updateProduct(id, productData);
      setEditingProduct(null);
      loadProducts();
    } catch (err) {
      setError('Failed to update product');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      try {
        await deleteProduct(id);
        loadProducts();
      } catch (err) {
        setError('Failed to delete product');
      }
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

  if (loading) return <div>Loading products...</div>;

  return (
    <div className="product-list">
      <div className="product-list-header">
        <h2>Products</h2>
        <button onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancel' : 'Add Product'}
        </button>
      </div>

      {error && <div className="error">{error}</div>}

      {showForm && (
        <ProductForm
          onSubmit={editingProduct ? (data) => handleUpdate(editingProduct.id, data) : handleCreate}
          onCancel={handleCancelEdit}
          initialData={editingProduct}
        />
      )}

      <div className="products">
        {products.length === 0 ? (
          <p>No products found. Add your first product!</p>
        ) : (
          products.map((product) => (
            <ProductItem
              key={product.id}
              product={product}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
          ))
        )}
      </div>
    </div>
  );
}
