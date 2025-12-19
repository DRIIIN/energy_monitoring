const LOGIN_END_POINT           = '/api/auth/login';
const REGISTER_END_POINT        = '/api/auth/register';
const LOGOUT_END_POINT          = '/api/auth/logout';
const VALIDATE_END_POINT        = '/api/auth/validate';
const PROFILE_END_POINT         = '/api/auth/profile';
const CHANGE_PASSWORD_END_POINT = '/api/auth/change-password';

/* Инструменты для авторизации пользователя */
class UserAPI {

    // Формерование http-запроса на авторизацию пользователя
    static async login(username, password) {
        const response = await RequestAPI.post(LOGIN_END_POINT, {
            username,
            password
        });
        
        if (response.data.success && response.data.data.token) {
            RequestAPI.setToken(response.data.data.token);
        }
        
        return response.data;
    }
    
    // Формирование http-запроса на регистрацию нового пользователя 
    static async register(userData) {
        const response = await RequestAPI.post(REGISTER_END_POINT, userData);
        return response.data;
    }
    
    // Формирование http-запроса на деавторизацию пользователя
    static async logout() {
        try {
            await RequestAPI.post(LOGOUT_END_POINT);
        } finally {
            RequestAPI.setToken(null);
        }
    }
    
    // Формирование http-запроса на проверку валидности токена текущей сессии
    static async validateToken() {
        try {
            const response = await RequestAPI.get(VALIDATE_END_POINT);
            return response.data;
        } catch (error) {
            return { success: false, message: 'Invalid token' };
        }
    }
    
    // Формирование http-запроса на получение информации о текущем пользователе
    static async getProfile() {
        const response = await RequestAPI.get(PROFILE_END_POINT);
        return response.data;
    }
    
    // Формирование http-запроса на смену пороля текущего пользователя
    static async changePassword(oldPassword, newPassword) {
        const response = await RequestAPI.post(CHANGE_PASSWORD_END_POINT, {
            old_password: oldPassword,
            new_password: newPassword
        });
        return response.data;
    }
    
}

window.UserAPI = UserAPI;