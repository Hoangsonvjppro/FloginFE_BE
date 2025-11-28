/* Flat config cho React 18 */
const js = require('@eslint/js');
const reactPlugin = require('eslint-plugin-react');
const globals = require('globals');

module.exports = [
    // Ignore patterns (thay cho .eslintignore)
    {
        ignores: [
            'node_modules/**',
            'dist/**',
            'build/**',
            'coverage/**',
            'webpack.config.*',
            'babel.config.*',
            'jest.config.*',
        ],
    },

    // Base JS recommended rules
    js.configs.recommended,

    // Rules cho mã nguồn React
    {
        files: ['src/**/*.{js,jsx}'],
        languageOptions: {
            ecmaVersion: 2021,
            sourceType: 'module',
            parserOptions: { ecmaFeatures: { jsx: true } },
            globals: {
                ...globals.browser,
                ...globals.jest,
            },
        },
        plugins: {
            react: reactPlugin,
        },
        rules: {
            'react/react-in-jsx-scope': 'off',  // React 18+
            'react/prop-types': 'warn',
            'no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
            'no-console': ['warn', { allow: ['warn', 'error'] }],
            'no-var': 'error',
            'prefer-const': 'warn',
        },
        settings: {
            react: { version: 'detect' },
        },
    },
];