class RouteTableHandler {
    constructor() {
        this.containerId        = "routeMap";
        this.container          = document.getElementById(this.containerId);
        this.canvas             = null;
        this.nodes              = new Map();
        this.edges              = [];
        this.coordinatorAddress = '0x0000';
    }


    setRoutesData(routesData) {
        if (routesData == null || routesData.length == 0) {
            return;
        }
        this.clear();
        this.addNode(this.coordinatorAddress);
        routesData.forEach(node => {
            this.addNode(node[0]);
            this.addEdge(node[0], node[1]);
        });
        // console.log(this.nodes, this.edges)

        this.render();
    }

    addNode(address) {
        if (!this.nodes.has(address)) {
            this.nodes.set(address, {
                x: 0,
                y: 0,
                radius: 25
            });
        }
    }

    addEdge(source, target) {
        if (source != target) {
            this.edges.push({
                source: source,
                target: target
            });
        } else {
            this.edges.push({
                source: source,
                target: this.coordinatorAddress
            });
        }
    }

    calculatePositions() {
        const canvasWidth  = this.canvas.clientWidth || 600;
        
        const coordinator = this.nodes.get(this.coordinatorAddress);
        if (coordinator) {
            coordinator.x      = canvasWidth / 2;
            coordinator.y      = 50;
            coordinator.radius = 30; 
        }

        const levels = this.calculateLevels();
        
        // console.log(levels);
        Object.keys(levels).forEach((level, levelIndex) => {
            const levelNodes = levels[level];
            levelNodes.forEach((nodeAddress, nodeIndex) => {
                const node = this.nodes.get(nodeAddress);
                if (node) {
                    node.x = 100 + nodeIndex * ((canvasWidth - 50) / Math.max(1, levelNodes.length - 1));
                    node.y = 100 + levelIndex * 80;;
                }
            });
        });
    }

    calculateLevels() {
        const levels = { '1': [] };
        this.edges.forEach(edge => {
            if (edge.target == this.coordinatorAddress) {
                if (!levels['1'].includes(edge.source)) {
                    levels['1'].push(edge.source);
                }
            }
        });

        let currentLevel = 2;
        let foundNewNodes = true;
        
        while (foundNewNodes) {
            foundNewNodes = false;
            const currentLevelNodes = [];
            
            const allPreviousNodes = [];
            for (let i = 1; i < currentLevel; i++) {
                if (levels[i.toString()]) {
                    allPreviousNodes.push(...levels[i.toString()]);
                }
            }
            
            this.edges.forEach(edge => {
                if (this.isInAnyLevel(edge.source, levels) || edge.source == this.coordinatorAddress || edge.target == this.coordinatorAddress) {
                    return;
                } else
                if (allPreviousNodes.includes(edge.target) && !currentLevelNodes.includes(edge.source)) {
                    currentLevelNodes.push(edge.source);
                    foundNewNodes = true;
                }
            });
            
            if (currentLevelNodes.length > 0) {
                levels[currentLevel.toString()] = [...new Set(currentLevelNodes)];
                currentLevel++;
            }
        }

        return levels;
    }

    isInAnyLevel(nodeAddress, levels) {
        if (nodeAddress == this.coordinatorAddress) {
            return true;
        } else {
            for (const level in levels) {
                if (levels[level].includes(nodeAddress)) {
                    return true;
                }
            }
            return false;
        }
    }

    render() {
        if (!this.canvas) {
            this.container.innerHTML = `<div class="graph-canvas" id="${this.containerId}_canvas"></div>`;
            this.canvas = document.getElementById(`${this.containerId}_canvas`);
        }

        this.calculatePositions();
        
        const svgNS = "http://www.w3.org/2000/svg";
        const svg   = document.createElementNS(svgNS, "svg");
        svg.setAttribute("width",   "100%");
        svg.setAttribute("height",  "100%");
        svg.setAttribute("viewBox", "0 0 800 500");
        
        this.canvas.innerHTML = '';
        
        const defs   = document.createElementNS(svgNS, "defs");
        const marker = document.createElementNS(svgNS, "marker");
        marker.setAttribute("id", "arrowhead");
        marker.setAttribute("markerWidth", "10");
        marker.setAttribute("markerHeight", "7");
        marker.setAttribute("refX", "9");
        marker.setAttribute("refY", "3.5");
        marker.setAttribute("orient", "auto");
        
        const polygon = document.createElementNS(svgNS, "polygon");
        polygon.setAttribute("points", "0 0, 10 3.5, 0 7");
        polygon.setAttribute("class", "arrowhead");
        
        marker.appendChild(polygon);
        defs.appendChild(marker);
        svg.appendChild(defs);
        
        this.edges.forEach(edge => {
            const sourceNode = this.nodes.get(edge.source);
            const targetNode = this.nodes.get(edge.target);
            
            if (sourceNode && targetNode) {
                const line = document.createElementNS(svgNS, "line");
                line.setAttribute("x1", sourceNode.x);
                line.setAttribute("y1", sourceNode.y);
                line.setAttribute("x2", targetNode.x);
                line.setAttribute("y2", targetNode.y);
                line.setAttribute("class", "graph-edge");
                line.setAttribute("marker-end", "url(#arrowhead)");
                line.setAttribute("stroke", "#888");
                line.setAttribute("stroke-width", "2");
                svg.appendChild(line);
            }
        });

        this.nodes.forEach((node, address) => {
            let fillColor = address == this.coordinatorAddress ? "#4d96ff" : "#ff6b6b";
            
            const circle = document.createElementNS(svgNS, "circle");
            circle.setAttribute("cx", node.x);
            circle.setAttribute("cy", node.y - 5);
            circle.setAttribute("r",  node.radius);
            circle.setAttribute("fill", fillColor);
            circle.setAttribute("stroke", fillColor == "#ff6b6b" ? "#ff3333" : "#1a73e8");
            circle.setAttribute("stroke-width", "2");
            svg.appendChild(circle);

            const text = document.createElementNS(svgNS, "text");
            text.setAttribute("x", node.x);
            text.setAttribute("y", node.y);
            text.setAttribute("text-anchor", "middle");
            text.setAttribute("font-family", "Roboto, sans-serif");
            text.setAttribute("font-size", "14px");
            text.setAttribute("font-weight", "500");
            text.setAttribute("fill", "#333");
            text.textContent = address;
            svg.appendChild(text);
        });


        this.canvas.appendChild(svg);
    }

    clear() {
        this.canvas             = null;
        this.nodes              = new Map();
        this.edges              = [];
        this.container.innerHTML = `<p>Данные о маршрутах ещё не были получены</p>`;
    }
}

window.RouteTableHandler = RouteTableHandler;