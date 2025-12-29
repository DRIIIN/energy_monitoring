/* Инструменты для работы с координаторами */
class CoordinatorAPI {
    
    // Получение списка координаторов пользователя
    static async getCoordinators() {
        const response = await RequestAPI.get('/api/coordinators');

        return response.data;
    }
    
    // Получение информации о конкретном координаторе
    static async getCoordinatorDetails(coordinatorId) {
        const response = await RequestAPI.get(`/api/coordinators/${coordinatorId}`);

        return response.data;
    }
    
    // Создание нового координатора
    static async createCoordinator(name, mac, ip, port) {
        const response = await RequestAPI.post('/api/coordinators', {
            name: name,
            mac:  mac,
            ip:   ip,
            port: port
        });

        return response.data;
    }
    
    // Подключение к нужному координатору
    static async connectToCoordinator(coordinatorId) {
        const response = await RequestAPI.post(`/api/coordinators/${coordinatorId}/connect`);

        return response.data;
    }
    
    // Обновление информации о координаторе
    static async updateCoordinator(coordinatorId, data) {
        const response = await RequestAPI.put(`/api/coordinators/${coordinatorId}`, data);

        return response.data;
    }
    
    // Удаление координатора
    static async deleteCoordinator(coordinatorId) {
        const response = await RequestAPI.delete(`/api/coordinators/${coordinatorId}`);
        return response.data;
    }
    
    // Отправка команды на координатор
    static async sendCommand(coordinatorId, commandCode, parameters = {}) {
        const response = await RequestAPI.post(`/api/coordinators/${coordinatorId}/command`, {
            command: commandCode,
            parameters: parameters
        });
        return response.data;
    }
    
    // Получение данных с приборов учёта
    static async getMeterData(coordinatorId, meterId = null) {
        let endpoint = `/api/coordinators/${coordinatorId}/meter-data`;
        if (meterId) {
            endpoint += `?meterId=${meterId}`;
        }
        const response = await RequestAPI.get(endpoint);
        return response.data;
    }
}

window.CoordinatorAPI = CoordinatorAPI;