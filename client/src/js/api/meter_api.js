/* Инструменты для работы с приборах учёта */
class MeterAPI {
    // Получение информации о конкретном счётчике по его id
    static async getMeterDetails(meterId) {
        const response = await RequestAPI.get(`/api/meters/${meterId}`);

        return response.data;
    }
}

window.MeterAPI = MeterAPI;