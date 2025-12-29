class AuthHandler {
    constructor(pageHandler) {
        this.pageHandler = pageHandler;
        this.currentUser = null;
    }

    // Проверка авторизации
    async checkAuth() {
        const token = RequestAPI.getToken();
        
        if (!token) {
            this.pageHandler.showLoginScreen();
            return false;
        }
        
        try {
            const result = await UserAPI.validateToken();
            
            if (result.success && result.data.valid) {
                this.currentUser = result.data.user;
                this.pageHandler.showAppScreen();
                this.pageHandler.getUserCoordinators();
                this.pageHandler.updateUserInfo();
                this.pageHandler.showUserProfile();

                return true;
            } else {
                this.pageHandler.showLoginScreen();

                return false;
            }
        } catch (error) {
            console.error('Auth check failed:', error);
            this.pageHandler.showLoginScreen();

            return false;
        }
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
                this.pageHandler.showAppScreen();
                this.pageHandler.getUserCoordinators();
                this.pageHandler.updateUserInfo();
                this.pageHandler.showUserProfile();
                this.pageHandler.showMessage('Вход выполнен успешно', 'success');

                return true;
            } else {
                this.pageHandler.showMessage(result.message || 'Ошибка входа', 'error');
            
                return false;
            }
        } catch (error) {
            console.error('Login error:', error);
            this.pageHandler.showMessage('Ошибка соединения с сервером', 'error');
        
            return false;
        } finally {
            loginBtn.innerHTML = originalText;
            loginBtn.disabled  = false;
        }
    }
    
    // Процесс регистрации пользователя
    async handleRegister() {
        const formData = {
            username: document.getElementById('regUsername').value,
            password: document.getElementById('regPassword').value,
        };
        
        if (formData.password !== document.getElementById('regConfirmPassword').value) {
            this.pageHandler.showMessage('Пароли не совпадают', 'error');
            
            return false;
        }
        
        const registerBtn  = document.getElementById('registerSubmitButton');
        const originalText = registerBtn.innerHTML;
        
        try {
            registerBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Регистрация...';
            registerBtn.disabled  = true;
  
            const result = await UserAPI.register(formData);
            
            if (result.success) {
                this.pageHandler.showMessage('Регистрация успешна! Теперь выполните вход.', 'success');
                this.pageHandler.showLoginForm();
                document.getElementById('registerForm').reset();

                return true;
            } else {
                this.pageHandler.showMessage(result.message || 'Ошибка регистрации', 'error');
            
                return false
            }
        } catch (error) {
            console.error('Registration error:', error);
            this.pageHandler.showMessage('Ошибка соединения с сервером', 'error');
        
            return false;
        } finally {
            registerBtn.innerHTML = originalText;
            registerBtn.disabled  = false;
        }
    }
    
    // Процесс деавтаризации пользователя
    async handleLogout() {
        try {
            await UserAPI.logout();
            this.currentUser = null;
            this.pageHandler.showLoginScreen();
            this.pageHandler.showMessage('Выход выполнен успешно', 'info');
        
            return true;
        } catch (error) {
            console.error('Logout error:', error);
            this.pageHandler.showMessage('Ошибка при выходе', 'error');
        
            return false;
        }
    }

    // Проверка, авторизован ли пользователь
    isAuthenticated() {
        return this.currentUser !== null && RequestAPI.getToken() !== null;
    }
    
    // Получение текущего пользователя
    getCurrentUser() {
        return this.currentUser;
    }
}

window.AuthHandler = AuthHandler;