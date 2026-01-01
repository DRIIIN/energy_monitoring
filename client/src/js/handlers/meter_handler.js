/* Класс с методами обработки действий со счётчиками */
class MeterHandler {
    constructor(pageHandler) {
        this.pageHandler = pageHandler;
    }

    // Отправляет на сервер запрос о параметрах счётчика с id meterId
    async getMeterDetails(meterId) {
        try {
            const response = await MeterAPI.getMeterDetails(meterId);
            if (response.success) {
                // console.log(response.data, meterId, `meter-voltage${meterId}`);
                document.getElementById(`meter-voltage${meterId}`).textContent         = response.data.voltage;
                document.getElementById(`meter-current${meterId}`).textContent         = response.data.current;
                document.getElementById(`meter-active-power${meterId}`).textContent    = response.data.active_power;
                document.getElementById(`meter-reactive-power${meterId}`).textContent  = response.data.reactive_power;
                document.getElementById(`meter-apparent-power${meterId}`).textContent  = response.data.apparent_power;
                document.getElementById(`meter-power-factor${meterId}`).textContent    = response.data.power_factor
                document.getElementById(`meter-frequency${meterId}`).textContent       = response.data.frequency;
                document.getElementById(`meter-neutral-current${meterId}`).textContent = response.data.neutral_current;

                return true;
            } else {
                this.pageHandler.showMessage(response.message || 'Не удалось получить информацию о приборе', 'error');
            
                return false
            }
        }catch (error) {
            console.error('Meter deleted error:', error);
            this.pageHandler.showMessage('Ошибка при опросе прибора', 'error');
        
            return false;
        } 
    }
}

window.MeterHandler = MeterHandler;