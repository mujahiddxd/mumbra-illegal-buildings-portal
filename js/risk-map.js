// Risk Assessment Map for Mumbra
document.addEventListener('DOMContentLoaded', function() {
    // Check if the risk map container exists
    const mapContainer = document.getElementById('riskMap');
    if (!mapContainer) return;

    // Define Mumbra boundaries to prevent map from going outside
    const mumbraBounds = L.latLngBounds(
        [19.1600, 72.9800], // Southwest corner
        [19.1900, 73.0200]  // Northeast corner
    );

    // Initialize the map centered on Mumbra, India
    const map = L.map('riskMap', {
        maxBounds: mumbraBounds,
        maxBoundsViscosity: 1.0,
        minZoom: 12,
        maxZoom: 18,
        zoomControl: true
    }).setView([19.1750, 72.9982], 13);

    // Add OpenStreetMap tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Restrict map view to Mumbra area - prevent zooming out beyond boundaries
    map.setMaxBounds(mumbraBounds);
    map.setMinZoom(12);
    map.setMaxZoom(18);

    // Add event listener to prevent zooming out of bounds
    map.on('zoomend', function() {
        const currentBounds = map.getBounds();
        const currentZoom = map.getZoom();
        
        // If zoom level is too low (zoomed out too much), reset to minimum zoom
        if (currentZoom < 12) {
            map.setZoom(12);
            showBoundaryMessage();
            return;
        }
        
        // Check if current view extends beyond Mumbra bounds
        if (!mumbraBounds.contains(currentBounds.getNorthEast()) || 
            !mumbraBounds.contains(currentBounds.getSouthWest())) {
            // If view goes outside bounds, reset to center of Mumbra
            map.setView([19.1750, 72.9982], Math.max(currentZoom, 12));
            showBoundaryMessage();
        }
    });

    // Prevent zoom out beyond minimum level
    map.on('zoomstart', function(e) {
        const currentZoom = map.getZoom();
        if (currentZoom <= 12 && e.sourceTarget._zoom < 12) {
            // Prevent zoom out if already at minimum zoom
            map.setZoom(12);
            showBoundaryMessage();
        }
    });

    // Function to show boundary restriction message
    function showBoundaryMessage() {
        // Remove existing message if any
        const existingMessage = document.querySelector('.boundary-message');
        if (existingMessage) {
            existingMessage.remove();
        }

        // Create boundary message
        const message = L.control({position: 'topright'});
        message.onAdd = function() {
            const div = L.DomUtil.create('div', 'boundary-message');
            div.innerHTML = `
                <div style="
                    background: rgba(220, 53, 69, 0.9);
                    color: white;
                    padding: 8px 12px;
                    border-radius: 6px;
                    font-size: 12px;
                    font-weight: 500;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.3);
                    border: 1px solid rgba(255,255,255,0.2);
                    backdrop-filter: blur(5px);
                ">
                    🚫 Map restricted to Mumbra area
                </div>
            `;
            
            // Auto-hide after 3 seconds
            setTimeout(() => {
                if (div.parentNode) {
                    div.parentNode.removeChild(div);
                }
            }, 3000);
            
            return div;
        };
        message.addTo(map);
    }

    // Sample data for illegal buildings with more realistic locations in Mumbra
    const illegalBuildings = [
        // High Risk Buildings (Red markers)
        {
            id: 1,
            name: 'Ref MB101 - Unauthorized Complex',
            lat: 19.1750,
            lng: 72.9982,
            type: 'unauthorized',
            severity: 'high',
            description: 'Five floors added beyond approved plan. Safety exits non-compliant.',
            address: 'Near Mumbra Station'
        },
        {
            id: 2,
            name: 'Ref MB102 - Structural Violation',
            lat: 19.1800,
            lng: 72.9900,
            type: 'structural',
            severity: 'high',
            description: 'Building constructed without proper foundation approval.',
            address: 'Kalwa Road'
        },
        {
            id: 3,
            name: 'Ref MB103 - FSI Violation',
            lat: 19.1700,
            lng: 73.0050,
            type: 'fsi',
            severity: 'high',
            description: 'Built-up area exceeds permissible FSI by 40%.',
            address: 'Thane Creek Area'
        },
        {
            id: 4,
            name: 'Ref MB104 - Safety Hazard',
            lat: 19.1780,
            lng: 72.9950,
            type: 'safety',
            severity: 'high',
            description: 'No fire safety equipment installed.',
            address: 'Mumbra Market Area'
        },
        {
            id: 5,
            name: 'Ref MB105 - Encroachment',
            lat: 19.1720,
            lng: 72.9920,
            type: 'encroachment',
            severity: 'high',
            description: 'Building extends into public road space.',
            address: 'Station Road'
        },
        {
            id: 6,
            name: 'Ref MB106 - Unauthorized Extension',
            lat: 19.1850,
            lng: 72.9880,
            type: 'extension',
            severity: 'high',
            description: 'Additional floors built without permission.',
            address: 'Kalwa Naka'
        },
        {
            id: 7,
            name: 'Ref MB107 - Commercial Violation',
            lat: 19.1680,
            lng: 73.0020,
            type: 'commercial',
            severity: 'high',
            description: 'Commercial activities in residential zone.',
            address: 'Thane Creek Road'
        },
        {
            id: 8,
            name: 'Ref MB108 - Height Violation',
            lat: 19.1820,
            lng: 72.9850,
            type: 'height',
            severity: 'high',
            description: 'Building exceeds approved height limit.',
            address: 'Kalwa Station Area'
        },
        {
            id: 9,
            name: 'Ref MB109 - Setback Violation',
            lat: 19.1750,
            lng: 72.9950,
            type: 'setback',
            severity: 'high',
            description: 'Insufficient setback from property boundary.',
            address: 'Mumbra Main Road'
        },
        {
            id: 10,
            name: 'Ref MB110 - Parking Violation',
            lat: 19.1780,
            lng: 72.9900,
            type: 'parking',
            severity: 'high',
            description: 'No parking space as per building norms.',
            address: 'Station Road'
        },
        {
            id: 11,
            name: 'Ref MB111 - Drainage Issue',
            lat: 19.1700,
            lng: 72.9980,
            type: 'drainage',
            severity: 'high',
            description: 'Blocking public drainage system.',
            address: 'Thane Creek Area'
        },
        {
            id: 12,
            name: 'Ref MB112 - Electrical Hazard',
            lat: 19.1850,
            lng: 72.9950,
            type: 'electrical',
            severity: 'high',
            description: 'Unauthorized electrical connections.',
            address: 'Kalwa Road'
        },

        // Medium Risk Buildings (Orange markers)
        {
            id: 13,
            name: 'Ref MB113 - Minor Encroachment',
            lat: 19.1770,
            lng: 72.9920,
            type: 'encroachment',
            severity: 'medium',
            description: 'Small extension into common area.',
            address: 'Mumbra Market'
        },
        {
            id: 14,
            name: 'Ref MB114 - Temporary Structure',
            lat: 19.1800,
            lng: 72.9980,
            type: 'temporary',
            severity: 'medium',
            description: 'Temporary shed without permission.',
            address: 'Station Road'
        },
        {
            id: 15,
            name: 'Ref MB115 - Signage Violation',
            lat: 19.1720,
            lng: 72.9950,
            type: 'signage',
            severity: 'medium',
            description: 'Oversized commercial signage.',
            address: 'Main Road'
        },
        {
            id: 16,
            name: 'Ref MB116 - Boundary Issue',
            lat: 19.1830,
            lng: 72.9900,
            type: 'boundary',
            severity: 'medium',
            description: 'Disputed property boundary.',
            address: 'Kalwa Area'
        },
        {
            id: 17,
            name: 'Ref MB117 - Water Connection',
            lat: 19.1750,
            lng: 72.9880,
            type: 'water',
            severity: 'medium',
            description: 'Unauthorized water connection.',
            address: 'Thane Creek Road'
        },
        {
            id: 18,
            name: 'Ref MB118 - Noise Violation',
            lat: 19.1780,
            lng: 73.0000,
            type: 'noise',
            severity: 'medium',
            description: 'Commercial activities causing noise.',
            address: 'Station Area'
        },
        {
            id: 19,
            name: 'Ref MB119 - Waste Disposal',
            lat: 19.1700,
            lng: 72.9900,
            type: 'waste',
            severity: 'medium',
            description: 'Improper waste disposal system.',
            address: 'Thane Creek'
        },
        {
            id: 20,
            name: 'Ref MB120 - Parking Issue',
            lat: 19.1850,
            lng: 72.9920,
            type: 'parking',
            severity: 'medium',
            description: 'Insufficient parking space.',
            address: 'Kalwa Naka'
        },

        // Low Risk Buildings (Green markers)
        {
            id: 21,
            name: 'Ref MB121 - Minor Modification',
            lat: 19.1770,
            lng: 72.9980,
            type: 'modification',
            severity: 'low',
            description: 'Minor internal modifications.',
            address: 'Mumbra Station'
        },
        {
            id: 22,
            name: 'Ref MB122 - Cosmetic Changes',
            lat: 19.1800,
            lng: 72.9950,
            type: 'cosmetic',
            severity: 'low',
            description: 'External paint without permission.',
            address: 'Main Road'
        },
        {
            id: 23,
            name: 'Ref MB123 - Garden Extension',
            lat: 19.1720,
            lng: 72.9980,
            type: 'garden',
            severity: 'low',
            description: 'Small garden extension.',
            address: 'Thane Creek'
        },
        {
            id: 24,
            name: 'Ref MB124 - Minor Repair',
            lat: 19.1830,
            lng: 72.9950,
            type: 'repair',
            severity: 'low',
            description: 'Repair work without notice.',
            address: 'Kalwa Road'
        },
        {
            id: 25,
            name: 'Ref MB125 - Temporary Fix',
            lat: 19.1750,
            lng: 72.9920,
            type: 'temporary',
            severity: 'low',
            description: 'Temporary structure for repair.',
            address: 'Station Road'
        }
    ];

    // Create feature groups for different risk levels
    const highRiskGroup = L.featureGroup();
    const mediumRiskGroup = L.featureGroup();
    const lowRiskGroup = L.featureGroup();
    const allMarkersGroup = L.featureGroup();

    // Function to get custom icon based on severity
    function getCustomIcon(severity) {
        const colors = {
            high: '#dc3545',
            medium: '#ffc107',
            low: '#28a745'
        };

        const color = colors[severity] || '#6c757d';
        
        return L.divIcon({
            className: 'custom-marker',
            html: `<div style="
                background-color: ${color};
                width: 20px;
                height: 20px;
                border-radius: 50%;
                border: 3px solid white;
                box-shadow: 0 2px 6px rgba(0,0,0,0.3);
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 12px;
                font-weight: bold;
                color: white;
            ">${severity.charAt(0).toUpperCase()}</div>`,
            iconSize: [20, 20],
            iconAnchor: [10, 10],
            popupAnchor: [0, -10]
        });
    }

    // Function to create popup content
    function createPopupContent(building) {
        const severityColors = {
            high: '#dc3545',
            medium: '#ffc107',
            low: '#28a745'
        };

        const severityColor = severityColors[building.severity];
        
        return `
            <div class="map-popup" style="min-width: 250px;">
                <div style="border-left: 4px solid ${severityColor}; padding-left: 12px;">
                    <h4 style="margin: 0 0 8px 0; color: #1f3b73; font-size: 1.1rem;">${building.name}</h4>
                    <p style="margin: 4px 0; color: #4a5d79; font-size: 0.9rem;"><strong>Address:</strong> ${building.address}</p>
                    <p style="margin: 4px 0; color: #4a5d79; font-size: 0.9rem;"><strong>Type:</strong> ${building.type.replace(/^\w/, c => c.toUpperCase())}</p>
                    <p style="margin: 4px 0; color: #4a5d79; font-size: 0.9rem;"><strong>Severity:</strong> 
                        <span style="color: ${severityColor}; font-weight: 600;">${building.severity.charAt(0).toUpperCase() + building.severity.slice(1)} Risk</span>
                    </p>
                    <p style="margin: 8px 0 0 0; color: #2c3e50; font-size: 0.85rem; line-height: 1.4;">${building.description}</p>
                </div>
            </div>
        `;
    }

    // Add markers to the map
    function addMarkers() {
        illegalBuildings.forEach(building => {
            const marker = L.marker([building.lat, building.lng], {
                icon: getCustomIcon(building.severity)
            });

            marker.bindPopup(createPopupContent(building));
            
            // Add to appropriate group
            allMarkersGroup.addLayer(marker);
            
            if (building.severity === 'high') {
                highRiskGroup.addLayer(marker);
            } else if (building.severity === 'medium') {
                mediumRiskGroup.addLayer(marker);
            } else if (building.severity === 'low') {
                lowRiskGroup.addLayer(marker);
            }
        });

        // Add all markers to map initially
        map.addLayer(allMarkersGroup);
    }

    // Initialize markers
    addMarkers();

    // Filter functionality
    const showHighRisk = document.getElementById('showHighRisk');
    const showMediumRisk = document.getElementById('showMediumRisk');
    const showLowRisk = document.getElementById('showLowRisk');

    function updateMapDisplay() {
        // Remove all groups
        map.removeLayer(allMarkersGroup);
        map.removeLayer(highRiskGroup);
        map.removeLayer(mediumRiskGroup);
        map.removeLayer(lowRiskGroup);

        // Add selected groups
        if (showHighRisk.checked) {
            map.addLayer(highRiskGroup);
        }
        if (showMediumRisk.checked) {
            map.addLayer(mediumRiskGroup);
        }
        if (showLowRisk.checked) {
            map.addLayer(lowRiskGroup);
        }
    }

    // Event listeners for checkboxes
    showHighRisk.addEventListener('change', updateMapDisplay);
    showMediumRisk.addEventListener('change', updateMapDisplay);
    showLowRisk.addEventListener('change', updateMapDisplay);

    // Quick action buttons
    const showAllMarkers = document.getElementById('showAllMarkers');
    const centerMap = document.getElementById('centerMap');
    const locateUser = document.getElementById('locateUser');

    showAllMarkers.addEventListener('click', function() {
        showHighRisk.checked = true;
        showMediumRisk.checked = true;
        showLowRisk.checked = true;
        updateMapDisplay();
        
        // Fit map to show all markers
        if (allMarkersGroup.getLayers().length > 0) {
            map.fitBounds(allMarkersGroup.getBounds().pad(0.1));
        }
    });

    centerMap.addEventListener('click', function() {
        map.setView([19.1750, 72.9982], 13);
    });

    locateUser.addEventListener('click', function() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    const { latitude, longitude } = position.coords;
                    
                    // Check if location is within Mumbra bounds
                    if (mumbraBounds.contains([latitude, longitude])) {
                        map.setView([latitude, longitude], 15);
                        
                        // Add user location marker
                        const userMarker = L.marker([latitude, longitude], {
                            icon: L.divIcon({
                                className: 'user-location-marker',
                                html: `<div style="
                                    background-color: #007bff;
                                    width: 16px;
                                    height: 16px;
                                    border-radius: 50%;
                                    border: 3px solid white;
                                    box-shadow: 0 2px 6px rgba(0,0,0,0.3);
                                "></div>`,
                                iconSize: [16, 16],
                                iconAnchor: [8, 8]
                            })
                        }).bindPopup('Your Location').addTo(map);
                        
                        // Remove user marker after 5 seconds
                        setTimeout(() => {
                            map.removeLayer(userMarker);
                        }, 5000);
                    } else {
                        alert('Your location is outside Mumbra area. Centering on Mumbra instead.');
                        map.setView([19.1750, 72.9982], 13);
                    }
                },
                function(error) {
                    alert('Unable to retrieve your location. Centering on Mumbra instead.');
                    map.setView([19.1750, 72.9982], 13);
                },
                {
                    enableHighAccuracy: true,
                    timeout: 5000,
                    maximumAge: 0
                }
            );
        } else {
            alert('Geolocation is not supported by your browser.');
        }
    });

    // Update statistics
    function updateStatistics() {
        const highCount = illegalBuildings.filter(b => b.severity === 'high').length;
        const mediumCount = illegalBuildings.filter(b => b.severity === 'medium').length;
        const lowCount = illegalBuildings.filter(b => b.severity === 'low').length;
        const totalCount = illegalBuildings.length;

        document.getElementById('highCount').textContent = highCount;
        document.getElementById('mediumCount').textContent = mediumCount;
        document.getElementById('lowCount').textContent = lowCount;
        document.getElementById('totalCases').textContent = totalCount;
    }

    // Initialize statistics
    updateStatistics();

    // Add click event to map to show coordinates (for debugging)
    map.on('click', function(e) {
        console.log('Clicked at:', e.latlng.lat, e.latlng.lng);
    });
});
