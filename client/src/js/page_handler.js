/* Основные методы для обработки страницы */
class PageHandler {

    constructor() {
        this.authHandler = null;
        this.uiHandler   = null;
        this.currentUser = null;
        
        this.initialize();
    }
    
    async initialize() {
        console.log('Initializing Energy Monitoring System...');
        
        this.uiHandler   = new UIHandler(this);
        this.authHandler = new AuthHandler(this);
        
        this.initEventListeners();
        this.uiHandler.initUI();
        
        await this.authHandler.checkAuth()
        
        console.log('Application initialized successfully');
    }
    
    // Обработчик событий
    initEventListeners() {
        document.getElementById('loginForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.authHandler.handleLogin();
        });
        
        document.getElementById('registerButton')?.addEventListener('click', () => {
            this.uiHandler.showRegisterForm();
        });
        
        document.getElementById('forgotPassword')?.addEventListener('click', (e) => {
            this.uiHandler.showForgotPasswordForm();
        });

        document.getElementById('backToLoginFromRegister')?.addEventListener('click', () => {
            this.uiHandler.showLoginForm();
        });

        document.getElementById('backToLoginFromForgot')?.addEventListener('click', () => {
            this.uiHandler.showLoginForm();
        });

        document.getElementById('forgotSubmitButton')?.addEventListener('click', () => {
            this.uiHandler.showLoginForm();
        });
        
        document.getElementById('registerForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.authHandler.handleRegister();
        });
        
        document.getElementById('logoutButton')?.addEventListener('click', () => {
            this.authHandler.handleLogout();
        });

        document.getElementById('myProfileButton')?.addEventListener('click', () => {
            this.uiHandler.showUserProfile();
        });

        document.getElementById('cancelAddCoordinator')?.addEventListener('click', () => {
            this.uiHandler.showUserProfile();
        });

        document.getElementById('addCoordinatorButtonInMenu')?.addEventListener('click', () => {
            this.uiHandler.showAddCoordinator();
        });

        document.getElementById('addCoordinatorButton')?.addEventListener('click', () => {
            this.uiHandler.showAddCoordinator();
        });
        
        document.getElementById('userMenuButton')?.addEventListener('click', () => {
            this.uiHandler.toggleUserMenu();
        });
        
        document.addEventListener('click', (e) => {
            const userMenu       = document.getElementById('userMenuDropdown');
            const userMenuButton = document.getElementById('userMenuButton');
            
            if (userMenu?.classList.contains('show') && !userMenu.contains(e.target) && 
                !userMenuButton.contains(e.target)) {
                this.uiHandler.toggleUserMenu(false);
            }
        });
    }
    
    // Отображение экрана авторизации
    showLoginScreen() {
        this.uiHandler.showLoginScreen();
    }
    
    // Отображение экрана основного приложения
    showAppScreen() {
        this.uiHandler.showAppScreen();
    }
    
    // Отображение формы авторизации
    showLoginForm() {
        this.uiHandler.showLoginForm();
    }
    
    // Отображение формы регистрации
    showRegisterForm() {
        this.uiHandler.showRegisterForm();
    }
    
    // Отображение формы восстановления пароля
    showForgotPasswordForm() {
        this.uiHandler.showForgotPasswordForm();
    }
    
    // Обновление приветственной информации
    updateUserInfo() {
        const user = this.authHandler.getCurrentUser();
        this.uiHandler.updateUserInfo(user);
    }
    
    // Отображение уведомления
    showMessage(message, type = 'info') {
        this.uiHandler.showMessage(message, type);
    }
    
    // Получение текущего пользователя
    getCurrentUser() {
        return this.authHandler.getCurrentUser();
    }

    // Отображение профиля пользователя
    showUserProfile() {
        this.uiHandler.showUserProfile();
    }

    // Отображение формы добавления координатора
    showAddCoordinator() {
        this.uiHandler.showAddCoordinator();
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.PageHandler = new PageHandler();
});