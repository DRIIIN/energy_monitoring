class UIHandler {
    constructor(pageHandler) {
        this.pageHandler = pageHandler;
    }

    // Инициализация UI
    initUI() {
        this.hidePreloader();
    }
    
    // Скрытие прелоадера
    hidePreloader() {
        setTimeout(() => {
            document.getElementById('preloader').classList.add('fade-out');
            setTimeout(() => {
                document.getElementById('preloader').style.display = 'none';
            }, 500);
        }, 1000);
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

    // Отображение блока профиля
    showUserProfile() {
        document.getElementById('userProfileBlock').classList.add('show');
        document.getElementById('addCoordinatorBlock').classList.remove('show');
        document.getElementById('userMenuDropdown').classList.remove('show');
        document.getElementById('coordinatorInfoBlock').classList.remove('show');
    }

    // Отображение блока добавления координатора
    showAddCoordinator() {
        document.getElementById('userProfileBlock').classList.remove('show');
        document.getElementById('addCoordinatorBlock').classList.add('show');
        document.getElementById('userMenuDropdown').classList.remove('show');
        document.getElementById('coordinatorInfoBlock').classList.remove('show');
    }

    // Отображение блока информации о координаторе
    showCoordinatorInfo() {
        document.getElementById('userProfileBlock').classList.remove('show');
        document.getElementById('coordinatorInfoBlock').classList.add('show');
        document.getElementById('userMenuDropdown').classList.remove('show');
    }

    // Скрытие кнопки добавлени координатора в профиле пользователя
    hideBattonAdditionCoordinatorInProfile() {
        document.getElementById('openAddCoordinatorFormButton').classList.add('hide');
    }

    // Отображение кнопки добавлени координатора в профиле пользователя
    showBattonAdditionCoordinatorInProfile() {
        document.getElementById('openAddCoordinatorFormButton').classList.remove('hide');
    }

    // Обновления информации о текущем пользователе
    updateUserInfo(user, numberCoords) {
        if (!user) {
            return;
        }
        
        const elements = [
            { id: 'userName',                 text: user.username },
            { id: 'dropdownUserName',         text: user.username },
            { id: 'profilUsername',           text: user.username },
            { id: 'profilCoordinatorsNumber', text: numberCoords  }
        ];
        
        elements.forEach(({ id, text }) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = text;
            }
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
        if (!toastContainer) {
            return;
        }
        
        const toastId   = 'toast-' + Date.now();
        const toast     = document.createElement('div');
        toast.id        = toastId;
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

window.UIHandler = UIHandler;
