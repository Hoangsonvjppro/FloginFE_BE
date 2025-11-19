import { useState, useEffect } from 'react';

export default function ProductForm({ onSubmit, onCancel, initialData }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    quantity: ''
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (initialData) {
      setFormData({
        name: initialData.name || '',
        description: initialData.description || '',
        price: initialData.price?.toString() || '',
        quantity: initialData.quantity?.toString() || ''
      });
    }
  }, [initialData]);

  const validate = () => {
    const newErrors = {};
    
    if (!formData.name?.trim()) {
      newErrors.name = 'Product name is required';
    }
    
    if (!formData.price) {
      newErrors.price = 'Price is required';
    } else if (parseFloat(formData.price) <= 0) {
      newErrors.price = 'Price must be greater than 0';
    }
    
    if (!formData.quantity && formData.quantity !== '0') {
      newErrors.quantity = 'Quantity is required';
    } else if (parseInt(formData.quantity) < 0) {
      newErrors.quantity = 'Quantity must be greater than or equal to 0';
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
        quantity: parseInt(formData.quantity)
      });
      
      // Reset form if creating new product
      if (!initialData) {
        setFormData({ name: '', description: '', price: '', quantity: '' });
      }
    } else {
      setErrors(validationErrors);
    }
  };

  const handleChange = (field, value) => {
    setFormData({ ...formData, [field]: value });
    // Clear error for this field
    if (errors[field]) {
      setErrors({ ...errors, [field]: '' });
    }
  };

  return (
    <form className="product-form" onSubmit={handleSubmit}>
      <h3>{initialData ? 'Edit Product' : 'Add New Product'}</h3>
      
      <div className="form-group">
        <label htmlFor="name">Product Name *</label>
        <input
          id="name"
          type="text"
          value={formData.name}
          onChange={(e) => handleChange('name', e.target.value)}
          placeholder="Enter product name"
        />
        {errors.name && <span className="error">{errors.name}</span>}
      </div>
      
      <div className="form-group">
        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          value={formData.description}
          onChange={(e) => handleChange('description', e.target.value)}
          placeholder="Enter product description"
          rows="3"
        />
      </div>
      
      <div className="form-group">
        <label htmlFor="price">Price *</label>
        <input
          id="price"
          type="number"
          step="0.01"
          value={formData.price}
          onChange={(e) => handleChange('price', e.target.value)}
          placeholder="0.00"
        />
        {errors.price && <span className="error">{errors.price}</span>}
      </div>
      
      <div className="form-group">
        <label htmlFor="quantity">Quantity *</label>
        <input
          id="quantity"
          type="number"
          value={formData.quantity}
          onChange={(e) => handleChange('quantity', e.target.value)}
          placeholder="0"
        />
        {errors.quantity && <span className="error">{errors.quantity}</span>}
      </div>
      
      <div className="form-actions">
        <button type="submit" className="btn-primary">
          {initialData ? 'Update' : 'Create'}
        </button>
        {onCancel && (
          <button type="button" className="btn-secondary" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
