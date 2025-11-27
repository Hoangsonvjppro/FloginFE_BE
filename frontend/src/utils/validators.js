/**
 * Frontend Validation Utilities
 * Pure functions cho validation theo quy tắc đề bài
 */

/**
 * Validate username theo quy tắc:
 * - 3-50 ký tự
 * - Chỉ chứa a-z, A-Z, 0-9, dấu chấm (.), gạch ngang (-), gạch dưới (_)
 * 
 * @param {string} username - Username cần validate
 * @returns {string|null} - null nếu hợp lệ, message lỗi nếu không hợp lệ
 */
export function validateUsername(username) {
    // Check null/undefined
    if (!username) {
        return 'Username is required';
    }

    // Convert to string nếu cần
    const usernameStr = String(username);

    // Check độ dài
    if (usernameStr.length < 3) {
        return 'Username must be at least 3 characters';
    }

    if (usernameStr.length > 50) {
        return 'Username must not exceed 50 characters';
    }

    // Check ký tự hợp lệ: a-z, A-Z, 0-9, ., -, _
    const usernameRegex = /^[a-zA-Z0-9._-]+$/;
    if (!usernameRegex.test(usernameStr)) {
        return 'Username can only contain letters, numbers, dots, hyphens, and underscores';
    }

    return null; // Hợp lệ
}

/**
 * Validate password theo quy tắc:
 * - 6-100 ký tự
 * - Bắt buộc có cả chữ VÀ số
 * 
 * @param {string} password - Password cần validate
 * @returns {string|null} - null nếu hợp lệ, message lỗi nếu không hợp lệ
 */
export function validatePassword(password) {
    // Check null/undefined
    if (!password) {
        return 'Password is required';
    }

    // Convert to string nếu cần
    const passwordStr = String(password);

    // Check độ dài
    if (passwordStr.length < 6) {
        return 'Password must be at least 6 characters';
    }

    if (passwordStr.length > 100) {
        return 'Password must not exceed 100 characters';
    }

    // Check có chứa chữ
    const hasLetter = /[a-zA-Z]/.test(passwordStr);
    if (!hasLetter) {
        return 'Password must contain at least one letter';
    }

    // Check có chứa số
    const hasNumber = /[0-9]/.test(passwordStr);
    if (!hasNumber) {
        return 'Password must contain at least one number';
    }

    return null; // Hợp lệ
}

/**
 * Validate product theo quy tắc:
 * - Name: 3-100 ký tự, không rỗng
 * - Price: > 0 và <= 999,999,999
 * - Quantity: >= 0 và <= 99,999
 * - Description: <= 500 ký tự (optional)
 * 
 * @param {Object} product - Product object cần validate
 * @param {string} product.name - Tên sản phẩm
 * @param {number} product.price - Giá sản phẩm
 * @param {number} product.quantity - Số lượng
 * @param {string} [product.description] - Mô tả (optional)
 * @returns {Object} - { valid: boolean, errors: Object }
 */
export function validateProduct(product) {
    const errors = {};

    // Validate product object
    if (!product || typeof product !== 'object') {
        return {
            valid: false,
            errors: { general: 'Product must be an object' }
        };
    }

    // Validate Name
    if (!product.name || typeof product.name !== 'string') {
        errors.name = 'Product name is required';
    } else {
        const nameTrimmed = product.name.trim();

        if (nameTrimmed.length === 0) {
            errors.name = 'Product name cannot be empty';
        } else if (nameTrimmed.length < 3) {
            errors.name = 'Product name must be at least 3 characters';
        } else if (nameTrimmed.length > 100) {
            errors.name = 'Product name must not exceed 100 characters';
        }
    }

    // Validate Price
    if (product.price === null || product.price === undefined) {
        errors.price = 'Price is required';
    } else {
        const price = Number(product.price);

        if (isNaN(price)) {
            errors.price = 'Price must be a valid number';
        } else if (price <= 0) {
            errors.price = 'Price must be greater than 0';
        } else if (price > 999999999) {
            errors.price = 'Price must not exceed 999,999,999';
        }
    }

    // Validate Quantity
    if (product.quantity === null || product.quantity === undefined) {
        errors.quantity = 'Quantity is required';
    } else {
        const quantity = Number(product.quantity);

        if (isNaN(quantity)) {
            errors.quantity = 'Quantity must be a valid number';
        } else if (!Number.isInteger(quantity)) {
            errors.quantity = 'Quantity must be an integer';
        } else if (quantity < 0) {
            errors.quantity = 'Quantity must be greater than or equal to 0';
        } else if (quantity > 99999) {
            errors.quantity = 'Quantity must not exceed 99,999';
        }
    }

    // Validate Description (optional)
    if (product.description !== null && product.description !== undefined) {
        if (typeof product.description !== 'string') {
            errors.description = 'Description must be a string';
        } else if (product.description.length > 500) {
            errors.description = 'Description must not exceed 500 characters';
        }
    }

    return {
        valid: Object.keys(errors).length === 0,
        errors: errors
    };
}
