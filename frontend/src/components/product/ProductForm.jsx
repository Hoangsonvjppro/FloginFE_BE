import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { getAllCategories } from '../../services/categoryApi';

export default function ProductForm({ onSubmit, onCancel, initialData, isOpen }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    quantity: '',
    categoryId: ''
  });
  const [categories, setCategories] = useState([]);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isOpen) {
      loadCategories();
    }

    if (initialData) {
      setFormData({
        name: initialData.name || '',
        description: initialData.description || '',
        price: initialData.price?.toString() || '',
        quantity: initialData.quantity?.toString() || '',
        categoryId: initialData.category?.id?.toString() || ''
      });
    } else {
      setFormData({ name: '', description: '', price: '', quantity: '', categoryId: '' });
    }
  }, [initialData, isOpen]);

  const loadCategories = async () => {
    try {
      const data = await getAllCategories();
      setCategories(data);
    } catch (error) {
      console.error('Failed to load categories:', error);
    }
  };

  const validate = () => {
    const newErrors = {};
    
    if (!formData.name?.trim()) {
      newErrors.name = 'Product name is required';
    } else if (formData.name.trim().length < 3) {
      newErrors.name = 'Product name must be at least 3 characters';
    } else if (formData.name.trim().length > 100) {
      newErrors.name = 'Product name must not exceed 100 characters';
    }

    if (formData.description && formData.description.length > 500) {
      newErrors.description = 'Description must not exceed 500 characters';
    }
    
    if (!formData.price) {
      newErrors.price = 'Price is required';
    } else if (parseFloat(formData.price) <= 0) {
      newErrors.price = 'Price must be greater than 0';
    } else if (parseFloat(formData.price) > 999999999) {
      newErrors.price = 'Price must not exceed 999,999,999';
    }
    
    if (!formData.quantity && formData.quantity !== '0') {
      newErrors.quantity = 'Quantity is required';
    } else if (parseInt(formData.quantity) < 0) {
      newErrors.quantity = 'Quantity must be greater than or equal to 0';
    } else if (parseInt(formData.quantity) > 99999) {
      newErrors.quantity = 'Quantity must not exceed 99,999';
    }

    if (!formData.categoryId) {
      newErrors.categoryId = 'Category is required';
    }
    
    return newErrors;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const validationErrors = validate();
    
    if (Object.keys(validationErrors).length === 0) {
      onSubmit({
        name: formData.name.trim(),
        description: formData.description?.trim() || '',
        price: parseFloat(formData.price),
        quantity: parseInt(formData.quantity),
        categoryId: parseInt(formData.categoryId)
      });
    } else {
      setErrors(validationErrors);
    }
  };

  const handleChange = (field, value) => {
    setFormData({ ...formData, [field]: value });
    if (errors[field]) {
      setErrors({ ...errors, [field]: '' });
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-content product-form-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{initialData ? 'Edit Product' : 'Add New Product'}</h3>
          <button className="btn-close" onClick={onCancel} type="button">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body">
            <div className="form-group">
              <input
                id="name"
                type="text"
                value={formData.name}
                onChange={(e) => handleChange('name', e.target.value)}
                placeholder="Product name * (3-100 characters)"
                className={errors.name ? 'error' : ''}
              />
              {errors.name && <span className="error-message">{errors.name}</span>}
            </div>

            <div className="form-group">
              <select
                id="category"
                value={formData.categoryId}
                onChange={(e) => handleChange('categoryId', e.target.value)}
                className={errors.categoryId ? 'error' : ''}
              >
                <option value="">Select category *</option>
                {categories.map((cat) => (
                  <option key={cat.id} value={cat.id}>
                    {cat.name}
                  </option>
                ))}
              </select>
              {errors.categoryId && <span className="error-message">{errors.categoryId}</span>}
            </div>
            
            <div className="form-group">
              <textarea
                id="description"
                value={formData.description}
                onChange={(e) => handleChange('description', e.target.value)}
                placeholder="Product description (optional, max 500 characters)"
                rows="4"
              />
              {errors.description && <span className="error-message">{errors.description}</span>}
              <small className="char-count">{formData.description.length}/500</small>
            </div>
            
            <div className="form-row">
              <div className="form-group">
                <input
                  id="price"
                  type="number"
                  step="0.01"
                  value={formData.price}
                  onChange={(e) => handleChange('price', e.target.value)}
                  placeholder="Price * (max 999,999,999)"
                  className={errors.price ? 'error' : ''}
                />
                {errors.price && <span className="error-message">{errors.price}</span>}
              </div>
              
              <div className="form-group">
                <input
                  id="quantity"
                  type="number"
                  value={formData.quantity}
                  onChange={(e) => handleChange('quantity', e.target.value)}
                  placeholder="Quantity * (max 99,999)"
                  className={errors.quantity ? 'error' : ''}
                />
                {errors.quantity && <span className="error-message">{errors.quantity}</span>}
              </div>
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn-secondary" onClick={onCancel}>
              Cancel
            </button>
            <button type="submit" className="btn-primary">
              {initialData ? 'Update Product' : 'Create Product'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

ProductForm.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  initialData: PropTypes.object,
  isOpen: PropTypes.bool.isRequired
};
