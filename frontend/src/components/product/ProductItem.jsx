export default function ProductItem({ product, onEdit, onDelete }) {
  return (
    <div className="product-item">
      <div className="product-info">
        <h3>{product.name}</h3>
        {product.description && <p className="description">{product.description}</p>}
        <div className="product-details">
          <span className="price">${product.price?.toFixed(2)}</span>
          <span className="quantity">
            Stock: {product.quantity} {product.quantity === 0 && <span className="out-of-stock">(Out of stock)</span>}
          </span>
        </div>
      </div>
      <div className="product-actions">
        <button 
          className="btn-edit" 
          onClick={() => onEdit(product)}
          aria-label="Edit product"
        >
          Edit
        </button>
        <button 
          className="btn-delete" 
          onClick={() => onDelete(product.id)}
          aria-label="Delete product"
        >
          Delete
        </button>
      </div>
    </div>
  );
}
