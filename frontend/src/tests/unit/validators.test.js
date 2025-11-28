/**
 * Unit Tests cho Validation Utilities
 * Testing với Jest - Coverage >= 90%
 */

import {
    validateUsername,
    validatePassword,
    validateProduct
} from '../../utils/validators';

describe('validateUsername', () => {
    // ==================== HAPPY PATH ====================

    test('should return null for valid username with lowercase letters', () => {
        expect(validateUsername('john')).toBeNull();
    });

    test('should return null for valid username with mixed case', () => {
        expect(validateUsername('JohnDoe')).toBeNull();
    });

    test('should return null for valid username with numbers', () => {
        expect(validateUsername('user123')).toBeNull();
    });

    test('should return null for valid username with dots', () => {
        expect(validateUsername('john.doe')).toBeNull();
    });

    test('should return null for valid username with hyphens', () => {
        expect(validateUsername('john-doe')).toBeNull();
    });

    test('should return null for valid username with underscores', () => {
        expect(validateUsername('john_doe')).toBeNull();
    });

    test('should return null for valid username with all allowed characters', () => {
        expect(validateUsername('User_123.test-name')).toBeNull();
    });

    test('should return null for minimum valid length (3 characters)', () => {
        expect(validateUsername('abc')).toBeNull();
    });

    test('should return null for maximum valid length (50 characters)', () => {
        const maxUsername = 'a'.repeat(50);
        expect(validateUsername(maxUsername)).toBeNull();
    });

    // ==================== ERROR CASES ====================

    test('should return error for null username', () => {
        expect(validateUsername(null)).toBe('Username is required');
    });

    test('should return error for undefined username', () => {
        expect(validateUsername(undefined)).toBe('Username is required');
    });

    test('should return error for empty string', () => {
        expect(validateUsername('')).toBe('Username is required');
    });

    test('should return error for username too short (< 3 characters)', () => {
        expect(validateUsername('ab')).toBe('Username must be at least 3 characters');
    });

    test('should return error for username too long (> 50 characters)', () => {
        const longUsername = 'a'.repeat(51);
        expect(validateUsername(longUsername)).toBe('Username must not exceed 50 characters');
    });

    test('should return error for username with spaces', () => {
        expect(validateUsername('john doe')).toBe('Username can only contain letters, numbers, dots, hyphens, and underscores');
    });

    test('should return error for username with @ symbol', () => {
        expect(validateUsername('john@example')).toBe('Username can only contain letters, numbers, dots, hyphens, and underscores');
    });

    test('should return error for username with # symbol', () => {
        expect(validateUsername('john#123')).toBe('Username can only contain letters, numbers, dots, hyphens, and underscores');
    });

    test('should return error for username with $ symbol', () => {
        expect(validateUsername('user$name')).toBe('Username can only contain letters, numbers, dots, hyphens, and underscores');
    });

    test('should return error for username with % symbol', () => {
        expect(validateUsername('user%123')).toBe('Username can only contain letters, numbers, dots, hyphens, and underscores');
    });

    test('should return error for username with special characters', () => {
        expect(validateUsername('user!@#$%')).toBe('Username can only contain letters, numbers, dots, hyphens, and underscores');
    });

    test('should return error for username with only special characters', () => {
        expect(validateUsername('!@#$%')).toBe('Username can only contain letters, numbers, dots, hyphens, and underscores');
    });
});

describe('validatePassword', () => {
    // ==================== HAPPY PATH ====================

    test('should return null for valid password with letters and numbers', () => {
        expect(validatePassword('User123')).toBeNull();
    });

    test('should return null for valid password with minimum length (6 characters)', () => {
        expect(validatePassword('Pass12')).toBeNull();
    });

    test('should return null for valid password with maximum length (100 characters)', () => {
        const maxPassword = 'a'.repeat(50) + '1'.repeat(50); // 50 chữ + 50 số
        expect(validatePassword(maxPassword)).toBeNull();
    });

    test('should return null for password with uppercase, lowercase, and numbers', () => {
        expect(validatePassword('MyPassword123')).toBeNull();
    });

    test('should return null for password with special characters, letters, and numbers', () => {
        expect(validatePassword('Pass@word123')).toBeNull();
    });

    test('should return null for password starting with numbers', () => {
        expect(validatePassword('123Password')).toBeNull();
    });

    test('should return null for password with mixed format', () => {
        expect(validatePassword('aB1cD2eF3')).toBeNull();
    });

    // ==================== ERROR CASES ====================

    test('should return error for null password', () => {
        expect(validatePassword(null)).toBe('Password is required');
    });

    test('should return error for undefined password', () => {
        expect(validatePassword(undefined)).toBe('Password is required');
    });

    test('should return error for empty string', () => {
        expect(validatePassword('')).toBe('Password is required');
    });

    test('should return error for password too short (< 6 characters)', () => {
        expect(validatePassword('Pass1')).toBe('Password must be at least 6 characters');
    });

    test('should return error for password with only 5 characters', () => {
        expect(validatePassword('abcd1')).toBe('Password must be at least 6 characters');
    });

    test('should return error for password too long (> 100 characters)', () => {
        const longPassword = 'a'.repeat(50) + '1'.repeat(51); // 101 characters
        expect(validatePassword(longPassword)).toBe('Password must not exceed 100 characters');
    });

    test('should return error for password with only letters (no numbers)', () => {
        expect(validatePassword('Password')).toBe('Password must contain at least one number');
    });

    test('should return error for password with only lowercase letters', () => {
        expect(validatePassword('password')).toBe('Password must contain at least one number');
    });

    test('should return error for password with only uppercase letters', () => {
        expect(validatePassword('PASSWORD')).toBe('Password must contain at least one number');
    });

    test('should return error for password with only numbers (no letters)', () => {
        expect(validatePassword('123456')).toBe('Password must contain at least one letter');
    });

    test('should return error for password with special characters only', () => {
        expect(validatePassword('!@#$%^')).toBe('Password must contain at least one letter');
    });

    test('should return error for password with special characters and numbers only', () => {
        expect(validatePassword('!@#123')).toBe('Password must contain at least one letter');
    });
});

describe('validateProduct', () => {
    // ==================== HAPPY PATH ====================

    test('should return valid for complete valid product', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: 10,
            description: 'A great product'
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
        expect(result.errors).toEqual({});
    });

    test('should return valid for product without description (optional)', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
        expect(result.errors).toEqual({});
    });

    test('should return valid for product with null description', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: 10,
            description: null
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
        expect(result.errors).toEqual({});
    });

    test('should return valid for minimum valid name length (3 characters)', () => {
        const product = {
            name: 'Tea',
            price: 5.99,
            quantity: 100
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
    });

    test('should return valid for maximum valid name length (100 characters)', () => {
        const product = {
            name: 'a'.repeat(100),
            price: 10.99,
            quantity: 50
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
    });

    test('should return valid for minimum valid price (0.01)', () => {
        const product = {
            name: 'Cheap Item',
            price: 0.01,
            quantity: 100
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
    });

    test('should return valid for maximum valid price (999,999,999)', () => {
        const product = {
            name: 'Expensive Item',
            price: 999999999,
            quantity: 1
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
    });

    test('should return valid for zero quantity', () => {
        const product = {
            name: 'Out of Stock',
            price: 19.99,
            quantity: 0
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
    });

    test('should return valid for maximum valid quantity (99,999)', () => {
        const product = {
            name: 'Bulk Item',
            price: 1.99,
            quantity: 99999
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
    });

    test('should return valid for maximum description length (500 characters)', () => {
        const product = {
            name: 'Product',
            price: 10.99,
            quantity: 5,
            description: 'a'.repeat(500)
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(true);
    });

    // ==================== ERROR CASES - GENERAL ====================

    test('should return error for null product', () => {
        const result = validateProduct(null);
        expect(result.valid).toBe(false);
        expect(result.errors.general).toBe('Product must be an object');
    });

    test('should return error for undefined product', () => {
        const result = validateProduct(undefined);
        expect(result.valid).toBe(false);
        expect(result.errors.general).toBe('Product must be an object');
    });

    test('should return error for non-object product', () => {
        const result = validateProduct('not an object');
        expect(result.valid).toBe(false);
        expect(result.errors.general).toBe('Product must be an object');
    });

    // ==================== ERROR CASES - NAME ====================

    test('should return error for missing name', () => {
        const product = {
            price: 99.99,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.name).toBe('Product name is required');
    });

    test('should return error for null name', () => {
        const product = {
            name: null,
            price: 99.99,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.name).toBe('Product name is required');
    });

    test('should return error for empty name', () => {
        const product = {
            name: '',
            price: 99.99,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.name).toBe('Product name is required');
    });

    test('should return error for whitespace-only name', () => {
        const product = {
            name: '   ',
            price: 99.99,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.name).toBe('Product name cannot be empty');
    });

    test('should return error for name too short (< 3 characters)', () => {
        const product = {
            name: 'ab',
            price: 99.99,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.name).toBe('Product name must be at least 3 characters');
    });

    test('should return error for name too long (> 100 characters)', () => {
        const product = {
            name: 'a'.repeat(101),
            price: 99.99,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.name).toBe('Product name must not exceed 100 characters');
    });

    // ==================== ERROR CASES - PRICE ====================

    test('should return error for missing price', () => {
        const product = {
            name: 'Test Product',
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.price).toBe('Price is required');
    });

    test('should return error for null price', () => {
        const product = {
            name: 'Test Product',
            price: null,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.price).toBe('Price is required');
    });

    test('should return error for negative price', () => {
        const product = {
            name: 'Test Product',
            price: -10.99,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.price).toBe('Price must be greater than 0');
    });

    test('should return error for zero price', () => {
        const product = {
            name: 'Test Product',
            price: 0,
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.price).toBe('Price must be greater than 0');
    });

    test('should return error for price exceeding maximum (> 999,999,999)', () => {
        const product = {
            name: 'Test Product',
            price: 1000000000, // 1 billion
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.price).toBe('Price must not exceed 999,999,999');
    });

    test('should return error for invalid price (NaN)', () => {
        const product = {
            name: 'Test Product',
            price: 'not a number',
            quantity: 10
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.price).toBe('Price must be a valid number');
    });

    // ==================== ERROR CASES - QUANTITY ====================

    test('should return error for missing quantity', () => {
        const product = {
            name: 'Test Product',
            price: 99.99
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.quantity).toBe('Quantity is required');
    });

    test('should return error for null quantity', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: null
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.quantity).toBe('Quantity is required');
    });

    test('should return error for negative quantity', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: -5
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.quantity).toBe('Quantity must be greater than or equal to 0');
    });

    test('should return error for quantity exceeding maximum (> 99,999)', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: 100000
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.quantity).toBe('Quantity must not exceed 99,999');
    });

    test('should return error for invalid quantity (NaN)', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: 'not a number'
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.quantity).toBe('Quantity must be a valid number');
    });

    test('should return error for non-integer quantity', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: 10.5
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.quantity).toBe('Quantity must be an integer');
    });

    // ==================== ERROR CASES - DESCRIPTION ====================

    test('should return error for description too long (> 500 characters)', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: 10,
            description: 'a'.repeat(501)
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.description).toBe('Description must not exceed 500 characters');
    });

    test('should return error for non-string description', () => {
        const product = {
            name: 'Test Product',
            price: 99.99,
            quantity: 10,
            description: 12345
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.description).toBe('Description must be a string');
    });

    // ==================== MULTIPLE ERRORS ====================

    test('should return multiple errors for invalid product', () => {
        const product = {
            name: 'ab', // Too short
            price: -10, // Negative
            quantity: -5 // Negative
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(result.errors.name).toBeDefined();
        expect(result.errors.price).toBeDefined();
        expect(result.errors.quantity).toBeDefined();
    });

    test('should return all possible errors for completely invalid product', () => {
        const product = {
            name: '', // Empty
            price: 0, // Zero
            quantity: 100000, // Too large
            description: 'a'.repeat(501) // Too long
        };
        const result = validateProduct(product);
        expect(result.valid).toBe(false);
        expect(Object.keys(result.errors).length).toBeGreaterThan(0);
    });
});
