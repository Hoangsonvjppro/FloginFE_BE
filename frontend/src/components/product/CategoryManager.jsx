import { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import { getAllCategories, createCategory, updateCategory, deleteCategory } from '../../services/categoryApi';
import { useToast } from '../ToastContainer';

export default function CategoryManager({ isOpen, onClose }) {
  const [categories, setCategories] = useState([]);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [formData, setFormData] = useState({ name: '', description: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const { showToast } = useToast();

  useEffect(() => {
    if (isOpen) {
      loadCategories();
    }
  }, [isOpen]);

  const loadCategories = async () => {
    try {
      const data = await getAllCategories();
      setCategories(data);
    } catch (error) {
      showToast(error.response?.data?.message || 'Failed to load categories', 'error');
    }
  };

  const validate = () => {
    const newErrors = {};
    
    if (!formData.name?.trim()) {
      newErrors.name = 'Category name is required';
    } else if (formData.name.trim().length < 3) {
      newErrors.name = 'Category name must be at least 3 characters';
    } else if (formData.name.trim().length > 50) {
      newErrors.name = 'Category name must not exceed 50 characters';
    }

    if (formData.description && formData.description.length > 200) {
      newErrors.description = 'Description must not exceed 200 characters';
    }
    
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    try {
      const categoryData = {
        name: formData.name.trim(),
        description: formData.description?.trim() || ''
      };

      if (editingCategory) {
        await updateCategory(editingCategory.id, categoryData);
        showToast('Category updated successfully', 'success');
      } else {
        await createCategory(categoryData);
        showToast('Category created successfully', 'success');
      }
      
      setFormData({ name: '', description: '' });
      setEditingCategory(null);
      setIsFormOpen(false);
      await loadCategories();
    } catch (error) {
      showToast(error.response?.data?.message || 'Operation failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (category) => {
    setEditingCategory(category);
    setFormData({
      name: category.name,
      description: category.description || ''
    });
    setIsFormOpen(true);
    setErrors({});
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this category? Products using this category may be affected.')) {
      return;
    }

    try {
      await deleteCategory(id);
      showToast('Category deleted successfully', 'success');
      await loadCategories();
    } catch (error) {
      showToast(error.response?.data?.message || 'Failed to delete category', 'error');
    }
  };

  const handleAddNew = () => {
    setEditingCategory(null);
    setFormData({ name: '', description: '' });
    setIsFormOpen(true);
    setErrors({});
  };

  const handleCancelForm = () => {
    setIsFormOpen(false);
    setEditingCategory(null);
    setFormData({ name: '', description: '' });
    setErrors({});
  };

  const handleChange = (field, value) => {
    setFormData({ ...formData, [field]: value });
    if (errors[field]) {
      setErrors({ ...errors, [field]: '' });
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content category-manager-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Manage Categories</h3>
          <button className="btn-close" onClick={onClose} type="button">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
          </button>
        </div>

        <div className="modal-body">
          {!isFormOpen && (
            <>
              <div className="category-manager-header">
                <button className="btn-primary" onClick={handleAddNew}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                    <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                  </svg>
                  Add Category
                </button>
              </div>

              <div className="category-list">
                {categories.length === 0 ? (
                  <div className="empty-state">
                    <p>No categories yet. Create your first category.</p>
                  </div>
                ) : (
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {categories.map((category) => (
                        <tr key={category.id}>
                          <td><strong>{category.name}</strong></td>
                          <td>{category.description || '—'}</td>
                          <td>
                            <div className="action-buttons">
                              <button 
                                className="btn-icon btn-edit" 
                                onClick={() => handleEdit(category)}
                                title="Edit"
                              >
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                              </button>
                              <button 
                                className="btn-icon btn-delete" 
                                onClick={() => handleDelete(category.id)}
                                title="Delete"
                              >
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                                  <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </>
          )}

          {isFormOpen && (
            <form onSubmit={handleSubmit} className="category-form">
              <div className="form-group">
                <label htmlFor="categoryName">Category Name *</label>
                <input
                  id="categoryName"
                  type="text"
                  value={formData.name}
                  onChange={(e) => handleChange('name', e.target.value)}
                  placeholder="Enter category name (3-50 characters)"
                  className={errors.name ? 'error' : ''}
                  disabled={loading}
                />
                {errors.name && <span className="error-message">{errors.name}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="categoryDescription">Description</label>
                <textarea
                  id="categoryDescription"
                  value={formData.description}
                  onChange={(e) => handleChange('description', e.target.value)}
                  placeholder="Enter description (optional, max 200 characters)"
                  rows="3"
                  disabled={loading}
                />
                {errors.description && <span className="error-message">{errors.description}</span>}
                <small className="char-count">{formData.description.length}/200</small>
              </div>

              <div className="form-actions">
                <button 
                  type="button" 
                  className="btn-secondary" 
                  onClick={handleCancelForm}
                  disabled={loading}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  className="btn-primary"
                  disabled={loading}
                >
                  {loading ? 'Saving...' : (editingCategory ? 'Update Category' : 'Create Category')}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

CategoryManager.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired
};
