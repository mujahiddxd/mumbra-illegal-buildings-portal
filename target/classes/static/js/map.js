document.addEventListener('DOMContentLoaded', function() {
    const mapContainer = document.getElementById('riskMap');
    if (!mapContainer) return;

    console.log('Initializing map...');
    const map = L.map('riskMap').setView([19.1750, 72.9982], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

    function getIcon(severity) {
        return L.divIcon({
            html: `<div class="map-marker ${severity}"></div>`,
            className: '',
            iconSize: [30, 30],
            iconAnchor: [15, 30]
        });
    }

    function updateStatistics(buildings) {
        console.log('Updating statistics...', buildings);
        const stats = {
            total: buildings.length,
            highRisk: buildings.filter(b => b.severity === 'high').length,
            mediumRisk: buildings.filter(b => b.severity === 'medium').length,
            lowRisk: buildings.filter(b => b.severity === 'low').length
        };
        console.log('Stats calculated:', stats);

        if (document.getElementById('total-buildings')) {
            document.getElementById('total-buildings').textContent = stats.total;
            document.getElementById('high-risk').textContent = stats.highRisk;
            document.getElementById('medium-risk').textContent = stats.mediumRisk;
            document.getElementById('low-risk').textContent = stats.lowRisk;
        }
    }

    function addMarkers(buildings) {
        console.log('Adding markers for buildings:', buildings);
        buildings.forEach(building => {
            console.log('Adding marker for:', building.name, building.lat, building.lng);
            const marker = L.marker([building.lat, building.lng], {
                icon: getIcon(building.severity)
            }).addTo(map);

            marker.bindPopup(`
                <h3>${building.name || 'Building'}</h3>
                <p><strong>Type:</strong> ${building.type}</p>
                <p><strong>Risk Level:</strong> ${building.severity}</p>
                <p><strong>Ward:</strong> ${building.ward}</p>
                <p>${building.description || 'No description available'}</p>
            `);
        });

        if (buildings.length > 0) {
            const group = new L.featureGroup(buildings.map(b => L.marker([b.lat, b.lng])));
            map.fitBounds(group.getBounds().pad(0.1));
            console.log('Map view updated to fit all markers.');
        }
    }

    console.log('Fetching CSV data...');
    fetch('/buildings.csv')
        .then(response => {
            console.log('Fetch response:', response);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.text();
        })
        .then(csvData => {
            console.log('CSV data loaded:', csvData);
            const lines = csvData.split('\n');
            const headers = lines[0].split(',').map(h => h.trim());
            console.log('CSV headers:', headers);

            const buildings = [];

            for (let i = 1; i < lines.length; i++) {
                if (!lines[i].trim()) continue;
                const values = lines[i].split(',');
                const building = {};
                headers.forEach((header, index) => {
                    building[header] = values[index] ? values[index].trim() : '';
                });
                console.log('Parsed building row:', building);

                if (building.Latitude && building.Longitude) {
                    buildings.push({
                        name: building['Building Name'] || 'Unnamed Building',
                        lat: parseFloat(building.Latitude),
                        lng: parseFloat(building.Longitude),
                        type: building.Category || 'Unknown',
                        severity: 'medium', // default for now
                        description: `Location: ${building.Location || 'N/A'}<br>Floors: ${building.Floors || 'N/A'}`,
                        ward: building['Ward No.'] || 'N/A'
                    });
                }
            }

            console.log('Buildings array after parsing:', buildings);

            if (buildings.length > 0) {
                updateStatistics(buildings);
                addMarkers(buildings);
            } else {
                console.warn('No buildings found, using sample data.');
                const sampleBuildings = [{
                    name: 'Sample Building',
                    lat: 19.1750,
                    lng: 72.9982,
                    type: 'Residential',
                    severity: 'medium',
                    description: 'Sample building description',
                    ward: '1'
                }];
                updateStatistics(sampleBuildings);
                addMarkers(sampleBuildings);
            }
        })
        .catch(error => {
            console.error('Error loading building data:', error);
        });
});
