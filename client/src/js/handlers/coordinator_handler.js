class CoordinatorHandler {
    constructor(pageHandler) {
        this.pageHandler  = pageHandler;
        this.numberCoords = null;
    }

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
                        html += `
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
                    listContainer.innerHTML = html;
                    
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
                            this.pageHandler.showCoordinatorInfo();
                        });
                    });

                    return true;
                }
            }
        } catch {
            console.error('Coordinators geted error:', error);
            this.pageHandler.showMessage('Ошибка при определении доступных сетей', 'error');
            
            return false;
        }
    }

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

    async connectToCoordinator(coordinatorId) {
        try {
            const response = await CoordinatorAPI.connectToCoordinator(coordinatorId);
            if (response.success) {
                this.pageHandler.showMessage('Сеть доступна.', 'success');
                await this.getUserCoordinators();

                return true;
            } else {
                this.pageHandler.showMessage(response.message || 'Сеть не доступна', 'error');
            
                return false
            }
        }catch (error) {
            console.error('Coordinator deleted error:', error);
            this.pageHandler.showMessage('Ошибка при подключении к сети', 'error');
        
            return false;
        } 
    }

    getNumberOfCoordinators() {
        return this.numberCoords;
    }
}

window.CoordinatorHandler = CoordinatorHandler;