import PropTypes from 'prop-types';

function ConfirmDialog({ isOpen, title, message, onConfirm, onCancel, confirmText = 'Confirm', cancelText = 'Cancel', type = 'danger' }) {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="confirm-dialog" onClick={(e) => e.stopPropagation()}>
        <div className={`confirm-icon confirm-icon-${type}`}>
          {type === 'danger' ? (
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
              <path d="M16 10V16M16 22H16.01" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
              <circle cx="16" cy="16" r="12" stroke="currentColor" strokeWidth="2"/>
            </svg>
          ) : (
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
              <path d="M16 10V16M16 22H16.01" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"/>
              <circle cx="16" cy="16" r="12" stroke="currentColor" strokeWidth="2"/>
            </svg>
          )}
        </div>
        
        <div className="confirm-content">
          <h3>{title}</h3>
          <p>{message}</p>
        </div>
        
        <div className="confirm-actions">
          <button className="btn-secondary" onClick={onCancel}>
            {cancelText}
          </button>
          <button className={`btn-${type}`} onClick={onConfirm}>
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

ConfirmDialog.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  title: PropTypes.string.isRequired,
  message: PropTypes.string.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  confirmText: PropTypes.string,
  cancelText: PropTypes.string,
  type: PropTypes.oneOf(['danger', 'warning', 'info'])
};

export default ConfirmDialog;
