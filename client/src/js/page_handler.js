/* Основные методы для обработки страницы */
class PageHandler {

    constructor() {
        this.currentUser = null;
        
        this.initialize();
    }
    
    async initialize() {
        console.log('Initializing Energy Monitoring System...');
        
        this.initUI();
        await this.checkAuth();
        
        console.log('Application initialized successfully');
    }
    
    initUI() {
        this.initEventListeners();
        
        setTimeout(() => {
            document.getElementById('preloader').classList.add('fade-out');
            setTimeout(() => {
                document.getElementById('preloader').style.display = 'none';
            }, 500);
        }, 1000);
    }
    
    // Обработчик событий
    initEventListeners() {
        document.getElementById('loginForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleLogin();
        });
        
        document.getElementById('registerButton')?.addEventListener('click', () => {
            this.showRegisterForm();
        });
        
        document.getElementById('forgotPassword')?.addEventListener('click', (e) => {
            this.showForgotPasswordForm();
        });

        document.getElementById('backToLoginFromRegister')?.addEventListener('click', () => {
            this.showLoginForm();
        });

        document.getElementById('backToLoginFromForgot')?.addEventListener('click', () => {
            this.showLoginForm();
        });

        document.getElementById('forgotSubmitButton')?.addEventListener('click', () => {
            this.showLoginForm();
        });
        
        document.getElementById('registerForm')?.addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleRegister();
        });
        
        document.getElementById('logoutButton')?.addEventListener('click', () => {
            this.handleLogout();
        });
        
        document.getElementById('userMenuButton')?.addEventListener('click', () => {
            this.toggleUserMenu();
        });
        
        document.addEventListener('click', (e) => {
            const userMenu       = document.getElementById('userMenuDropdown');
            const userMenuButton = document.getElementById('userMenuButton');
            
            if (userMenu?.classList.contains('show') && !userMenu.contains(e.target) && 
                !userMenuButton.contains(e.target)) {
                this.toggleUserMenu(false);
            }
        });
    }
    
    // Проверка авторизации
    async checkAuth() {
        const token = RequestAPI.getToken();
        
        if (!token) {
            this.showLoginScreen();
            return;
        }
        
        try {
            const result = await UserAPI.validateToken();
            
            if (result.success && result.data.valid) {
                this.currentUser = result.data.user;
                this.showAppScreen();
                this.updateWelcomeInfo();
            } else {
                this.showLoginScreen();
            }
        } catch (error) {
            console.error('Auth check failed:', error);
            this.showLoginScreen();
        }
    }
    
    // Отображение экрана авторизации
    showLoginScreen() {
        document.getElementById('loginScreen').classList.add('active');
        document.getElementById('appScreen').classList.remove('active');
        this.showLoginForm();
    }
    
    // Отображение экрана основного приложения
    showAppScreen() {
        document.getElementById('loginScreen').classList.remove('active');
        document.getElementById('appScreen').classList.add('active');
        
        if (this.currentUser) {
            this.updateUserInfo();
        }
    }
    
    // Отображение формы авторизации
    showLoginForm() {
        document.getElementById('loginForm').classList.add('active');
        document.getElementById('registerForm').classList.remove('active');
        document.getElementById('forgotPasswordForm').classList.remove('active');
    }
    
    // Отображение формы регистрации
    showRegisterForm() {
        document.getElementById('loginForm').classList.remove('active');
        document.getElementById('registerForm').classList.add('active');
        document.getElementById('forgotPasswordForm').classList.remove('active');
    }

    // Отображение формы востановления пороля
    showForgotPasswordForm() {
        document.getElementById('loginForm').classList.remove('active');
        document.getElementById('registerForm').classList.remove('active');
        document.getElementById('forgotPasswordForm').classList.add('active');
    }

    // Обновления информации о текущем пользователе
    updateWelcomeInfo() {
        if (!this.currentUser) return;
        
        document.getElementById('welcomeUsername').textContent = this.currentUser.username;
    }
    
    // Процесс авторизации пользователя
    async handleLogin() {
        const username     = document.getElementById('username').value;
        const password     = document.getElementById('password').value;
        
        const loginBtn     = document.getElementById('loginButton');
        const originalText = loginBtn.innerHTML;
        
        try {
            loginBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Вход...';
            loginBtn.disabled  = true;
            
            const result = await UserAPI.login(username, password);
            
            if (result.success) {
                this.currentUser = result.data.user;
                this.showAppScreen();
                this.updateWelcomeInfo();
                
                this.showMessage('Вход выполнен успешно', 'success');
            } else {
                this.showMessage(result.message || 'Ошибка входа', 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            this.showMessage('Ошибка соединения с сервером', 'error');
        } finally {
            loginBtn.innerHTML = originalText;
            loginBtn.disabled = false;
        }
    }
    
    // Процесс регистрации пользователя
    async handleRegister() {
        const formData = {
            username: document.getElementById('regUsername').value,
            password: document.getElementById('regPassword').value,
        };
        
        if (formData.password !== document.getElementById('regConfirmPassword').value) {
            this.showMessage('Пароли не совпадают', 'error');
            return;
        }
        
        const registerBtn  = document.getElementById('registerSubmitButton');
        const originalText = registerBtn.innerHTML;
        
        try {
            registerBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Регистрация...';
            registerBtn.disabled  = true;
            
            const result = await UserAPI.register(formData);
            
            if (result.success) {
                this.showMessage('Регистрация успешна! Теперь выполните вход.', 'success');
                this.showLoginForm();
                
                document.getElementById('registerForm').reset();
            } else {
                this.showMessage(result.message || 'Ошибка регистрации', 'error');
            }
        } catch (error) {
            console.error('Registration error:', error);
            this.showMessage('Ошибка соединения с сервером', 'error');
        } finally {
            registerBtn.innerHTML = originalText;
            registerBtn.disabled = false;
        }
    }
    
    // Процесс деавтаризации пользователя
    async handleLogout() {
        try {
            await UserAPI.logout();
            this.currentUser = null;
            this.showLoginScreen();
            this.showMessage('Выход выполнен успешно', 'info');
        } catch (error) {
            console.error('Logout error:', error);
            this.showMessage('Ошибка при выходе', 'error');
        }
    }
    
    // Обновление информации о пользователе
    updateUserInfo() {
        if (!this.currentUser) return;
        
        const elements = [
            { id: 'userName',         text: this.currentUser.username },
            { id: 'dropdownUserName', text: this.currentUser.username }
        ];
        
        elements.forEach(({ id, text }) => {
            const element = document.getElementById(id);
            if (element) element.textContent = text;
        });
    }
    
    // Открытие/закрытие меню пользователя
    toggleUserMenu(show = null) {
        const menu = document.getElementById('userMenuDropdown');
        
        if (show === null) {
            show = !menu.classList.contains('show');
        }
        
        if (show) {
            menu.classList.add('show');
        } else {
            menu.classList.remove('show');
        }
    }
    
    // Отображение уведомления
    showMessage(message, type = 'info') {
        const toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) return;
        
        const toastId = 'toast-' + Date.now();
        const toast = document.createElement('div');
        toast.id = toastId;
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <div class="toast-icon">
                <i class="fas ${this.getMessageIcon(type)}"></i>
            </div>
            <div class="toast-content">
                <div class="toast-title">${this.getMessageTitle(type)}</div>
                <div class="toast-message">${message}</div>
            </div>
            <button class="toast-close" onclick="document.getElementById('${toastId}').remove()">
                <i class="fas fa-times"></i>
            </button>
        `;
        
        toastContainer.appendChild(toast);
        
        setTimeout(() => toast.classList.add('show'), 10);
        
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 5000);
    }
    
    // Выбор иконки для уведомления
    getMessageIcon(type) {
        const icons = {
            'success': 'fa-check-circle',
            'error':   'fa-exclamation-circle',
            'warning': 'fa-exclamation-triangle',
            'info':    'fa-info-circle'
        };
        return icons[type] || 'fa-info-circle';
    }
    
    // Выбор статуса для уведомления
    getMessageTitle(type) {
        const titles = {
            'success': 'Успешно',
            'error':   'Ошибка',
            'warning': 'Предупреждение',
            'info':    'Информация'
        };
        return titles[type] || 'Информация';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.PageHandler = new PageHandler();
});