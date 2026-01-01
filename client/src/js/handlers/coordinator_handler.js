/* Класс с методами обработки действий с координаторами */
class CoordinatorHandler {
    constructor(pageHandler, routTableHandler, metetHandler) {
        this.pageHandler       = pageHandler;
        this.routTableHandler  = routTableHandler;
        this.metetHandler      = metetHandler;
        this.numberCoords      = null;
    }

    // Добавляет к строке html блок с разметкой об информации о координаторе coordinator и возвращает её
    addedCoordinatorToPage(html, coordinator) {
        return html + `
            <div class="coordinator-item-block" id="coordinator${coordinator.id}">
                <div class="coordinator"></div>
                <div class="coordinator-info">
                    <p><strong>Имя:    </strong><text class="coordinator-name ${coordinator.status}"   id="coordinator-name${coordinator.id}"> ${coordinator.name}                               </text></p>
                    <p><strong>MAC:    </strong><text class="coordinator-mac ${coordinator.status}"    id="coordinator-mac${coordinator.id}">  ${coordinator.mac}                                </text></p>
                    <p><strong>Адрес:  </strong><text class="coordinator-addr ${coordinator.status}"   id="coordinator-addr${coordinator.id}"> ${coordinator.ip}:${coordinator.port}             </text></p>
                    <p><strong>Статус: </strong><text class="coordinator-status ${coordinator.status}" id="coordinator-status${coordinator.id}">${coordinator.status=='online'?'online':'offline'}</text></p>
                </div>
                <div class="coordinator-actions">
                    <button class="btn menu-item coordinator-action button-connect" id="buttonConnect${coordinator.id}">
                        <i class="fas fa-plug"></i>
                    </button>
                    <button class="btn menu-item coordinator-action button-info"    id="buttonInfo${coordinator.id}" ${coordinator.status=='online'?'':'disabled'}>
                        <i class="fas fa-info-circle"></i>
                    </button>
                    <button class="btn menu-item coordinator-action button-delete"  id="buttonDelete${coordinator.id}">
                        <i class="fa fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }

    // Добавляет к строке html блок с разметкой об информации о приборе учёта meter и возвращает её
    addedMeterToPage(html, meter) {
        // console.log(meter);
        return html + `
            <div class="meter-item-block" id="meter${meter.id}">
                <div class="meter"></div>
                <div class="meter-info">   
                    <p><strong>Имя:                  </strong><text class="meter-name      ${meter.status}" id="meter-name${meter.id}">       ${meter.name}                               </text></p>
                    <p><strong>Long-адрес:           </strong><text class="meter-mac       ${meter.status}" id="meter-long-addr${meter.id}">  ${meter.zb_long_addr}                       </text></p>
                    <p><strong>Short-адрес:          </strong><text class="meter-addr      ${meter.status}" id="meter-short-addr${meter.id}"> 0x${meter.zb_short_addr.toString(16).toUpperCase().padStart(4, '0')}                    </text></p>
                    <p><strong>Статус:               </strong><text class="meter-status    ${meter.status}" id="meter-status${meter.id}">     ${meter.status=='online'?'online':'offline'}</text></p>
                    <p><strong>Создан:               </strong><text class="meter-create-at ${meter.status}" id="meter-create-at${meter.id}">  ${meter.created_at}                         </text></p>
                    <p><strong>Последняя активность: </strong><text class="meter-last-seen ${meter.status}" id="meter-last-seen${meter.id}">  ${meter.last_seen}                          </text></p>
                </div>
                <table class="meter-data">

                    <tr><td>Напряжение (В):             </td><td class="meter-voltage         ${meter.status}" id="meter-voltage${meter.id}">         ${meter.voltage}        </td></tr>
                    <tr><td>Ток (A):                    </td><td class="meter-current         ${meter.status}" id="meter-current${meter.id}">         ${meter.current}        </td></tr>
                    <tr><td>Мощность активная (кВт):    </td><td class="meter-active-power    ${meter.status}" id="meter-active-power${meter.id}">    ${meter.active_power}   </td></tr>
                    <tr><td>Мощность реактивная (кВар): </td><td class="meter-reactive-power  ${meter.status}" id="meter-reactive-power${meter.id}">  ${meter.reactive_power} </td></tr>
                    <tr><td>Мощность (кВ*А):            </td><td class="meter-apparent-power  ${meter.status}" id="meter-apparent-power${meter.id}">  ${meter.apparent_power} </td></tr>
                    <tr><td>Коэффициент мощности (-):   </td><td class="meter-power-factor    ${meter.status}" id="meter-power-factor${meter.id}">    ${meter.power_factor}   </td></tr>
                    <tr><td>Частота (Гц):               </td><td class="meter-frequency       ${meter.status}" id="meter-frequency${meter.id}">       ${meter.frequency}      </td></tr>
                    <tr><td>Ток нулевого провода (A):   </td><td class="meter-neutral-current ${meter.status}" id="meter-neutral-current${meter.id}"> ${meter.neutral_current}</td></tr>
                </table>
                <div class="meter-actions">
                    <button type="button" class="btn menu-item meter-action button-seen" id="buttonSeen${meter.id}">
                        <i class="fas fa-plug"></i>
                    </button>
                </div>
            </div>
        `;
    }

    // Добавляет мониторинг нажатий на кнопки координаторов
    setEventsToCoordinatorsButtons() {
        const deleteButtons = document.querySelectorAll('.button-delete');
        deleteButtons.forEach(button => {
            button.addEventListener('click', async () => {
                await this.deleteCoordinator(button.id.replace('buttonDelete', ''));
                this.pageHandler.showUserProfile();
            });
        });
        
        const connectButtons = document.querySelectorAll('.button-connect');
        connectButtons.forEach(button => {
            button.addEventListener('click', async () => {
                await this.connectToCoordinator(button.id.replace('buttonConnect', ''));
                this.pageHandler.showUserProfile();
            });
        });

        const infoButtons = document.querySelectorAll('.button-info');
        infoButtons.forEach(button => {
            button.addEventListener('click', async () => {
                await this.getCordinatorDetails(button.id.replace('buttonInfo', ''));
                this.routTableHandler.clear();
                this.pageHandler.showCoordinatorInfo();
            });
        });
    }

    // Добавляет мониторинг нажатий на кнопки приборов учёта
    setEventsToMetersButtons() {
        const seenButtons = document.querySelectorAll('.button-seen');
        seenButtons.forEach(button => {
            button.addEventListener('click', async () => {
                await this.metetHandler.getMeterDetails(button.id.replace('buttonSeen', ''));
            });
        });
    }
    
    // Получает от сервера информацию о доступных пользователю координаторах и добавляет в html разметку блоки, описывающие их
    async getUserCoordinators() {
        try {
            const response = await CoordinatorAPI.getCoordinators();

            if (!response) {
                return false;
            } else {
                this.numberCoords = response.data.length;
                this.pageHandler.updateUserInfo();

                const listContainer = document.getElementById('coordinatorsList');
                if (!listContainer) {
                    return false;
                } else
                if (this.numberCoords == 0) {
                    listContainer.innerHTML = `<p>Нет добавленных координаторов</p>`;
                    
                    return true;
                } else {
                    let html = '';
                    for (let i = 0; i < this.numberCoords; i++) {
                        const coordinator = response.data[i];
                        html = this.addedCoordinatorToPage(html, coordinator);
                    }
                    listContainer.innerHTML = html;

                    this.setEventsToCoordinatorsButtons();

                    return true;
                }
            }
        } catch {
            console.error('Coordinators geted error:', error);
            this.pageHandler.showMessage('Ошибка при определении доступных сетей', 'error');
            
            return false;
        }
    }

    // Отправляет на сервер запрос на создание координатора с заданными параметра
    async createNewCoordinator() {
        const coordinatorData = {
            name: document.getElementById('coordinatorName').value,
            mac:  document.getElementById('coordinatorMac').value,
            ip:   document.getElementById('coordinatorIp').value,
            port: document.getElementById('coordinatorPort').value,
        };

        try {
            const response = await CoordinatorAPI.createCoordinator(coordinatorData.name, coordinatorData.mac, coordinatorData.ip, coordinatorData.port);
            if (response.success) {
                this.pageHandler.showMessage('Сеть добавлена успешно.', 'success');
                await this.getUserCoordinators();

                return true;
            } else {
                this.pageHandler.showMessage(response.message || 'Ошибка доавления сети', 'error');
            
                return false
            }
        }catch (error) {
            console.error('Coordinator added error:', error);
            this.pageHandler.showMessage('Ошибка при добавлении сети', 'error');

            return false;
        } 
    }

    // Отправляет на сервер запрос на удоление координатора с id coordinatorId
    async deleteCoordinator(coordinatorId) {
        try {
            const response = await CoordinatorAPI.deleteCoordinator(coordinatorId);
            if (response.success) {
                this.pageHandler.showMessage('Сеть удалена успешно.', 'success');
                await this.getUserCoordinators();

                return true;
            } else {
                this.pageHandler.showMessage(response.message || 'Ошибка удаления сети', 'error');
            
                return false
            }
        }catch (error) {
            console.error('Coordinator deleted error:', error);
            this.pageHandler.showMessage('Ошибка при удвалении сети', 'error');
        
            return false;
        } 
    }

    // Отправляет на сервер запрос о доступности координатора с id coordinatorId
    async connectToCoordinator(coordinatorId) {
        try {
            const response = await CoordinatorAPI.connectToCoordinator(coordinatorId);
            if (response.success) {
                this.pageHandler.showMessage('Сеть доступна.', 'success');
                await this.getUserCoordinators();

                return true;
            } else {
                this.pageHandler.showMessage('Сеть не доступна', 'error');
                await this.getUserCoordinators();
            
                return false
            }
        }catch (error) {
            console.error('Coordinator deleted error:', error);
            this.pageHandler.showMessage('Ошибка при подключении к сети', 'error');
        
            return false;
        } 
    }

    // Отправляет на сервер запрос о параметрах координатора с id coordinatorId
    async getCordinatorDetails(coordinatorId) {
        try {
            const response = await CoordinatorAPI.getCoordinatorDetails(coordinatorId);
            if (response.success) {
                document.getElementById('coordinatorInfoId').textContent        = response.data.coordinator.id;
                document.getElementById('coordinatorInfoName').textContent      = response.data.coordinator.name;
                document.getElementById('coordinatorInfoMac').textContent       = response.data.coordinator.mac;
                document.getElementById('coordinatorInfoIp').textContent        = response.data.coordinator.ip + ":" + response.data.coordinator.port;
                document.getElementById('coordinatorInfoStatus').textContent    = response.data.coordinator.status;
                document.getElementById('coordinatorInfoCreatedAt').textContent = response.data.coordinator.created_at;
                document.getElementById('coordinatorInfoLastSeen').textContent  = response.data.coordinator.last_seen;

                const numberMeters = response.data.meters.length;
                if (numberMeters > 0) {
                    let html = '';  
                    for (let i = 0; i < numberMeters; i++) {
                        const meter = response.data.meters[i];
                        html = this.addedMeterToPage(html, meter);
                    }
                    const listContainer = document.getElementById('metersList');
                    listContainer.innerHTML = html;

                    this.setEventsToMetersButtons();
                }

                return true;
            } else {
                this.pageHandler.showMessage(response.message || 'Не удалось получить информацию о сети', 'error');
            
                return false
            }
        }catch (error) {
            console.error('Coordinator deleted error:', error);
            this.pageHandler.showMessage('Ошибка при подключении к сети', 'error');
        
            return false;
        } 
    }

    async sendCommandToCoordinator() {
        try {
            const commandCode   = document.getElementById("commantToCoord").value;
            const parameters    = document.getElementById("commandParams").value;
            const coordinatorId = document.getElementById("coordinatorInfoId").textContent;
        
            const response = await CoordinatorAPI.sendCommand(coordinatorId, commandCode, parameters);
            this.pageHandler.showMessage('Ответ получен', 'success');
            if (response.success) {
                // console.log(response);
                document.getElementById("coordinatorResponse").innerHTML = `<textarea>${response.data.command_parameters}</textarea>`;
                this.pageHandler.showCoordinatorInfo();

                if (response.data.command_code == "A2") {
                    var routeData = [];
                    const numberOfNodes = parseInt(response.data.command_parameters.slice(0, 2), 16);
                    for (let i = 0; i < numberOfNodes; i++) {
                        const nodeAddress    = "0x" + response.data.command_parameters.slice(2 * (2 + i * 8), 2 * (3 + i * 8)) + response.data.command_parameters.slice(2 * (1 + i * 8), 2 * (2 + i * 8));
                        const nextHopAddress = "0x" + response.data.command_parameters.slice(2 * (4 + i * 8), 2 * (5 + i * 8)) + response.data.command_parameters.slice(2 * (3 + i * 8), 2 * (4 + i * 8));
                        routeData.push([nodeAddress, nextHopAddress]);
                    }
                    
                    this.routTableHandler.setRoutesData(routeData);
                }

                return true;
            } else {
                this.pageHandler.showMessage(response.message || 'Не удолось отправить запрос', 'error');
            
                return false
            }
        }catch (error) {
            console.error('Send command error:', error);
            this.pageHandler.showMessage('Не удолось отправить запрос', 'error');
        
            return false;
        } 
    }

    // Возвращает количесво координаторов, доступных пользователю
    getNumberOfCoordinators() {
        return this.numberCoords;
    }
}

window.CoordinatorHandler = CoordinatorHandler;