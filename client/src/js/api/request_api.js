const SERVER_URL = 'http://localhost:8081'; // Адрес сервера

/* API клиента для работы с сервером energy_monitoring */
class RequestAPI {
    static baseUrl = SERVER_URL;                    // Адрес сервера
    static token   = localStorage.getItem('token'); // JWT-токен сессии
    
    // Установка JWT-токена
    static setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem('token', token);
        } else {
            localStorage.removeItem('token');
        }
    }
    
    // Получение JWT-токена 
    static getToken() {
        return localStorage.getItem('token');
    }
    
    // Формирование и отправка HTTP-запросов
    static async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `${token}`;
        }
        
        const config = {
            ...options,
            headers
        };
        
        const startTime = Date.now();
        
        try {
            const response     = await fetch(url, config);
            const responseTime = Date.now() - startTime;

            let data;
            const contentType = response.headers.get('content-type');
            
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = await response.text();
            }
            
            const responseData = {
                data,
                status:     response.status,
                statusText: response.statusText,
                headers:    response.headers,
                responseTime
            };
            if (!response.ok) {
                const error  = new Error(data.error || data.message || `HTTP ${response.status}`);
                error.status = response.status;
                error.data   = data;
                throw error;
            }
            
            return responseData;
            
        } catch (error) {
            console.error(`API request failed: ${endpoint}`, error);
            
            if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
                throw new Error('Network error: Could not connect to server');
            }
            
            throw error;
        }
    }
    
    // Метод GET
    static async get(endpoint, params = {}) {
        const queryString = Object.keys(params).length 
            ? `?${new URLSearchParams(params).toString()}`
            : '';
        
        return this.request(`${endpoint}${queryString}`, {
            method: 'GET'
        });
    }
    
    // Метод POST
    static async post(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    // Метод PUT
    static async put(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }
    
    // Метод DELETE
    static async delete(endpoint, data = {}) {
        const options = {
            method: 'DELETE'
        };
        
        if (data && Object.keys(data).length > 0) {
            options.body = JSON.stringify(data);
        }
        
        return this.request(endpoint, options);
    }
}

window.RequestAPI = RequestAPI;
