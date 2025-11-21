export default function ProductItem({ product, onEdit, onDelete, viewMode = 'large' }) {
  return (
    <div className={`product-card ${viewMode === 'small' ? 'card-small' : 'card-large'}`}>
      <h3>{product.name}</h3>
      {product.description && <p>{product.description}</p>}
      <div className="product-price">${product.price?.toFixed(2)}</div>
      <div className="product-stock">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <path d="M8 4a.5.5 0 01.5.5V6h1.5a.5.5 0 010 1H8.5v1.5a.5.5 0 01-1 0V7H6a.5.5 0 010-1h1.5V4.5A.5.5 0 018 4z"/>
          <path d="M2 2a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V4a2 2 0 00-2-2H2zm12 1a1 1 0 011 1v8a1 1 0 01-1 1H2a1 1 0 01-1-1V4a1 1 0 011-1h12z"/>
        </svg>
        Stock: {product.quantity}
      </div>
      <div className="product-actions">
        <button 
          className="btn-edit" 
          onClick={() => onEdit(product)}
          aria-label="Edit product"
        >
          ✏️ Edit
        </button>
        <button 
          className="btn-delete" 
          onClick={() => onDelete(product.id)}
          aria-label="Delete product"
        >
          🗑️ Delete
        </button>
      </div>
    </div>
  );
}
