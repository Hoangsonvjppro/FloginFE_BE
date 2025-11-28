const { defineConfig } = require('cypress');

module.exports = defineConfig({
    e2e: {
        // Base URL của ứng dụng
        baseUrl: 'http://localhost:8080',

        // Thư mục chứa test files
        specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',

        // Thư mục cho fixtures (test data)
        fixturesFolder: 'cypress/fixtures',

        // Thư mục lưu screenshots khi test fail
        screenshotsFolder: 'cypress/screenshots',

        // Thư mục lưu videos
        videosFolder: 'cypress/videos',

        // Cấu hình viewport mặc định
        viewportWidth: 1280,
        viewportHeight: 720,

        // Timeout settings
        defaultCommandTimeout: 10000,
        requestTimeout: 10000,
        responseTimeout: 10000,

        // Video recording
        video: true,
        videoCompression: 32,

        // Screenshot on failure
        screenshotOnRunFailure: true,

        // Retry failed tests
        retries: {
            runMode: 2,
            openMode: 0
        },

        setupNodeEvents(on, config) {
            // implement node event listeners here
            return config;
        },
    },

    // Component testing configuration (optional)
    component: {
        devServer: {
            framework: 'react',
            bundler: 'webpack',
        },
    },
});
