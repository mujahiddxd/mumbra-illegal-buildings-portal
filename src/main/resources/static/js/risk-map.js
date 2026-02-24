// Make sure you include this in HTML before risk-map.js
// <script src="https://cdnjs.cloudflare.com/ajax/libs/PapaParse/5.4.1/papaparse.min.js"></script>

document.addEventListener('DOMContentLoaded', function() {
    const mapContainer = document.getElementById('riskMap');
    if (!mapContainer) return;

    // --- Map Initialization ---
    const mumbraBounds = L.latLngBounds([19.1600, 73.0000], [19.1900, 73.0400]);
        const map = L.map('riskMap', {
        maxBounds: mumbraBounds,
        maxBoundsViscosity: 1.0,
        minZoom: 12,
        maxZoom: 18,
        zoomControl: true
    }).setView([19.1750, 72.9982], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // --- Boundary Message ---
    function showBoundaryMessage() {
        const existingMessage = document.querySelector('.boundary-message');
        if (existingMessage) existingMessage.remove();

        const message = L.control({position: 'topright'});
        message.onAdd = function() {
            const div = L.DomUtil.create('div', 'boundary-message');
            div.innerHTML = '<div style="background: rgba(220, 53, 69, 0.9); color: white; padding: 8px 12px; border-radius: 6px; font-size: 12px; font-weight: 500; box-shadow: 0 2px 8px rgba(0,0,0,0.3); border: 1px solid rgba(255,255,255,0.2); backdrop-filter: blur(5px);">🚫 Map restricted to Mumbra area</div>';
            setTimeout(() => { if (div.parentNode) div.parentNode.removeChild(div); }, 3000);
            return div;
        };
        message.addTo(map);
    }

    map.on('zoomend', function() {
        const bounds = map.getBounds();
        const zoom = map.getZoom();
        if (zoom < 12 || !mumbraBounds.contains(bounds)) {
            map.setView([19.1750, 72.9982], Math.max(zoom, 12));
            showBoundaryMessage();
        }
    });

    map.on('zoomstart', function(e) {
        if (map.getZoom() <= 12 && e.sourceTarget._zoom < 12) {
            map.setZoom(12);
            showBoundaryMessage();
        }
    });

    // --- Feature Groups ---
    const highRiskGroup = L.featureGroup();
    const mediumRiskGroup = L.featureGroup();
    const lowRiskGroup = L.featureGroup();
    const allMarkersGroup = L.featureGroup();

    // --- Custom Marker Icon ---
    function getCustomIcon(severity) {
        const colors = { high: '#dc3545', medium: '#ffc107', low: '#28a745' };
        const color = colors[severity] || '#6c757d';
        return L.divIcon({
            className: 'custom-marker',
            html: `<div style="background-color: ${color}; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.3);"></div>`,
            iconSize: [20, 20],
            iconAnchor: [10, 10],
            popupAnchor: [0, -10]
        });
    }

    // --- Popup Content ---
    function createPopupContent(building) {
        const severityColors = { high: '#dc3545', medium: '#ffc107', low: '#28a745' };
        const severityColor = severityColors[building.severity] || '#6c757d';
        return `
            <div class="map-popup" style="min-width: 250px;">
                <div style="border-left: 4px solid ${severityColor}; padding-left: 12px;">
                    <h4 style="margin: 0 0 8px 0; color: #1f3b73; font-size: 1.1rem;">${building.name}</h4>
                    <p style="margin: 4px 0; color: #4a5d79; font-size: 0.9rem;"><strong>Address:</strong> ${building.address}</p>
                    <p style="margin: 4px 0; color: #4a5d79; font-size: 0.9rem;"><strong>Type:</strong> ${building.type}</p>
                    <p style="margin: 4px 0; color: ${severityColor}; font-size: 0.9rem; font-weight: bold;"><strong>Risk Level:</strong> ${building.severity.toUpperCase()}</p>
                    <p style="margin: 8px 0 0 0; color: #2c3e50; font-size: 0.85rem; line-height: 1.4;">${building.description}</p>
                </div>
            </div>
        `;
    }

    // --- Function to determine severity from CSV data ---
    function getSeverity(severityFromCSV, category) {
        // Use severity from CSV if available, otherwise fall back to category-based logic
        if (severityFromCSV) {
            return severityFromCSV.toLowerCase();
        }
        // Fallback logic based on category
        if (category === 'C2B') return 'medium';
        return 'low';
    }

    // --- Load CSV via PapaParse ---
    Papa.parse('buildings.csv', {
        download: true,
        header: true,
        skipEmptyLines: true,
        dynamicTyping: false,
        trimHeaders: true,
        complete: function(results) {
            console.log('CSV parsed:', results.data.length, 'rows');
            console.log('Sample row:', results.data[0]);

            let validCount = 0;
            let skippedCount = 0;

            results.data.forEach((b, idx) => {
                let lat, lng;
                
                // Priority 1: Check for separate Latitude/Longitude columns (most reliable)
                if (b.Latitude && b.Longitude) {
                    lat = parseFloat(b.Latitude);
                    lng = parseFloat(b.Longitude);
                }
                // Priority 2: Try parsing Area column if it contains "lat, lng" format
                else if (b.Area && typeof b.Area === 'string') {
                    // Skip if Area looks like base64 encoded image data
                    if (b.Area.startsWith('/9j/') || b.Area.length > 100) {
                        console.warn(`Skipping row ${idx + 1} - Area contains image data:`, b['Building Name']);
                        skippedCount++;
                        return;
                    }
                    
                    // Skip if Area is just a number (likely square footage)
                    if (/^\d+$/.test(b.Area.trim())) {
                        console.warn(`Skipping row ${idx + 1} - Area is just a number (no coordinates):`, b['Building Name']);
                        skippedCount++;
                        return;
                    }
                    
                    // Try to parse as "lat, lng"
                    const areaStr = b.Area.replace(/["'\s]/g, '');
                    const coords = areaStr.split(',');
                    
                    if (coords.length === 2) {
                        lat = parseFloat(coords[0]);
                        lng = parseFloat(coords[1]);
                    }
                }

                if (isNaN(lat) || isNaN(lng)) {
                    console.warn(`Skipping row ${idx + 1} - Invalid coordinates:`, {
                        name: b['Building Name'],
                        area: b.Area ? b.Area.substring(0, 50) + '...' : 'N/A',
                        parsed: { lat, lng }
                    });
                    skippedCount++;
                    return;
                }

                const severity = getSeverity(b.Severity, b.Category);
                const marker = L.marker([lat, lng], { icon: getCustomIcon(severity) });
                
                marker.bindPopup(createPopupContent({
                    name: b['Building Name'] || 'Unnamed Building',
                    address: b.Location || 'N/A',
                    type: b.Category || 'Unknown',
                    severity: severity,
                    description: `Floors: ${b.Floors || 'N/A'}<br>Ward: ${b['Ward No.'] || 'N/A'}`
                }));

                allMarkersGroup.addLayer(marker);
                if (severity === 'high') highRiskGroup.addLayer(marker);
                else if (severity === 'medium') mediumRiskGroup.addLayer(marker);
                else if (severity === 'low') lowRiskGroup.addLayer(marker);

                validCount++;
            });

            console.log(`✅ Loaded ${validCount} valid buildings`);
            console.log(`⚠️ Skipped ${skippedCount} invalid rows`);
            
            if (validCount > 0) {
                map.addLayer(allMarkersGroup);
                map.fitBounds(allMarkersGroup.getBounds().pad(0.1));
            } else {
                console.error('❌ No valid buildings loaded. Check CSV format.');
            }
        },
        error: function(err) {
            console.error('❌ CSV parse failed:', err);
        }
    });
});