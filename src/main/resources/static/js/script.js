function searchBuildings() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const buildingItems = document.querySelectorAll('.building-item');

    buildingItems.forEach(item => {
        const text = item.textContent.toLowerCase();
        if (text.includes(searchTerm)) {
            item.style.display = 'block';
        } else {
            item.style.display = 'none';
        }
    });

    if (searchTerm === '') {
        buildingItems.forEach(item => {
            item.style.display = 'block';
        });
    }
}

function openReportForm() {
    alert('Report Form: This would open a form for submitting new violation reports with fields for location, description, photos, and contact information.');
}

function checkStatus() {
    const referenceNumber = prompt('Enter case reference number (e.g., MB001):');
    if (referenceNumber) {
        alert(`Status Check: This would display the current status of case ${referenceNumber}`);
    }
}

// Smooth scrolling for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Search on Enter key press
document.getElementById('searchInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        searchBuildings();
    }
});

// Image preview
const fileInput = document.getElementById('buildingImage');
const previewImg = document.getElementById('preview');
const placeholder = document.getElementById('placeholder');
if (fileInput) {
    fileInput.addEventListener('change', function () {
        const file = this.files && this.files[0];
        if (!file) {
            previewImg.style.display = 'none';
            placeholder.style.display = 'flex';
            previewImg.src = '';
            return;
        }
        const reader = new FileReader();
        reader.onload = e => {
            previewImg.src = e.target.result;
            previewImg.style.display = 'block';
            placeholder.style.display = 'none';
        };
        reader.readAsDataURL(file);
    });
}

// Illegal cards image upload previews
document.querySelectorAll('#illegal-cards .card').forEach(function(card) {
    const input = card.querySelector('input[type="file"]');
    const img = card.querySelector('img');
    const hint = card.querySelector('.translate-middle');
    if (!input || !img) return;
    input.addEventListener('change', function() {
        const file = this.files && this.files[0];
        if (!file) {
            img.src = '';
            img.classList.add('d-none');
            if (hint) hint.style.display = 'block';
            return;
        }
        const reader = new FileReader();
        reader.onload = function(e) {
            img.src = e.target.result;
            img.classList.remove('d-none');
            if (hint) hint.style.display = 'none';
        };
        reader.readAsDataURL(file);
    });
});

// Submit + toast
console.log('Script.js loaded successfully');

// Wait for DOM to be ready
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing form handlers');
    
    const submitBtn = document.getElementById('submitReport');
    const resetBtn = document.getElementById('resetForm');
    console.log('Submit button found:', submitBtn);
    
    if (submitBtn) {
    submitBtn.addEventListener('click', async function () {
        console.log('Submit button clicked');
        
        // Check if form elements exist
        const locationEl = document.getElementById('location');
        const categoryEl = document.getElementById('category');
        const severityEl = document.getElementById('severity');
        const notesEl = document.getElementById('notes');
        
        console.log('Form elements:', {
            location: locationEl,
            category: categoryEl,
            severity: severityEl,
            notes: notesEl
        });
        
        // Collect form data
        const formData = {
            name: (locationEl?.value || 'Unknown Location') + ' Building', // Use location as name for now
            address: locationEl?.value || '',
            location: locationEl?.value || '',
            wardNumber: '16', // Default ward number
            floors: '0', // Default floors
            category: categoryEl?.value || 'Unauthorized construction',
            violationType: 'unauthorized', // Default violation type
            latitude: 19.1750, // Default coordinates for Mumbra
            longitude: 73.0100,
            severity: (severityEl?.value || 'medium').toUpperCase(),
            status: 'PENDING', // All new reports start as PENDING
            description: notesEl?.value || 'User reported building',
            imageUrl: '/images/buildings/default-building.jpg' // Default image for now
        };

        console.log('Form data collected:', formData);
        
        // Validate required fields
        if (!formData.location) {
            alert('Please provide a location for the building.');
            return;
        }

        try {
            // Submit to backend
            const response = await fetch('/api/buildings', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                // Show success message
                const toastEl = document.getElementById('reportToast');
                if (window.bootstrap && toastEl) {
                    const toast = new bootstrap.Toast(toastEl, { delay: 2500 });
                    toast.show();
                } else {
                    alert('Report submitted successfully! It will be reviewed by administrators.');
                }
                
                // Reset form
                resetForm();
            } else {
                throw new Error('Failed to submit report');
            }
        } catch (error) {
            console.error('Error submitting report:', error);
            alert('Error submitting report. Please try again later.');
        }
    });
    }
    
    // Reset form function
    function resetForm() {
        document.getElementById('isIllegal').checked = true;
        document.getElementById('location').value = '';
        document.getElementById('severity').value = 'medium';
        document.getElementById('category').selectedIndex = 0;
        document.getElementById('notes').value = '';
        if (fileInput) fileInput.value = '';
        if (previewImg) previewImg.style.display = 'none';
        if (placeholder) placeholder.style.display = 'flex';
    }

    if (resetBtn) {
        resetBtn.addEventListener('click', resetForm);
    }
}); // End of DOMContentLoaded

// View Map toggles using Leaflet
document.querySelectorAll('#illegal-cards .card .view-map').forEach(function(btn){
    btn.addEventListener('click', function(){
        const card = this.closest('.card');
        const wrap = card.querySelector('.map-wrap');
        const mapContainer = card.querySelector('.leaflet-map');
        if (!wrap || !mapContainer) return;

        const isOpen = wrap.classList.contains('show');
        wrap.classList.toggle('show');

        if (!isOpen && !mapContainer.dataset.inited) {
            const lat = parseFloat(this.dataset.mapLat || '19.186');
            const lng = parseFloat(this.dataset.mapLng || '72.978');
            const zoom = parseInt(this.dataset.mapZoom || '13', 10);
            const map = L.map(mapContainer).setView([lat, lng], zoom);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(map);
            L.marker([lat, lng]).addTo(map);
            mapContainer.dataset.inited = '1';
            setTimeout(() => map.invalidateSize(), 200);
        }
    });
});

// Search functionality for navigation search bar
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('navSearch');
    const searchButton = document.getElementById('navSearchButton');

    function performSearch() {
        const searchTerm = searchInput.value.trim().toLowerCase();
        
        // If search term is empty, reset the view
        if (!searchTerm) {
            resetSearch();
            return;
        }

        // Search through building cards or relevant content
        const searchableElements = document.querySelectorAll('.building-card, .card, .map-marker');
        let foundResults = false;

        searchableElements.forEach(element => {
            const text = element.textContent.toLowerCase();
            if (text.includes(searchTerm)) {
                element.style.display = '';
                element.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                element.classList.add('search-highlight');
                foundResults = true;
            } else {
                element.style.display = 'none';
                element.classList.remove('search-highlight');
            }
        });

        // Show no results message if needed
        showNoResultsMessage(foundResults);
    }

    function resetSearch() {
        const elements = document.querySelectorAll('.building-card, .card, .map-marker');
        elements.forEach(element => {
            element.style.display = '';
            element.classList.remove('search-highlight');
        });
        
        const noResults = document.getElementById('noResultsMessage');
        if (noResults) noResults.remove();
    }

    function showNoResultsMessage(hasResults) {
        // Remove existing message if any
        const existingMessage = document.getElementById('noResultsMessage');
        if (existingMessage) existingMessage.remove();

        if (!hasResults) {
            const message = document.createElement('div');
            message.id = 'noResultsMessage';
            message.textContent = 'No results found. Try different keywords.';
            message.style.textAlign = 'center';
            message.style.padding = '2rem';
            message.style.color = '#666';
            
            // Insert after the search bar or in a suitable container
            const searchContainer = document.querySelector('.nav-search');
            if (searchContainer) {
                searchContainer.parentNode.insertBefore(message, searchContainer.nextSibling);
            }
        }
    }

    // Event listeners
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }

    if (searchButton) {
        searchButton.addEventListener('click', performSearch);
    }
});

// In your mobile menu toggle function
// Mobile menu functionality using Bootstrap's collapse
document.addEventListener('DOMContentLoaded', function() {
    const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
    const mainNav = document.querySelector('.main-nav');
    const menuOverlay = document.querySelector('.menu-overlay');
    const body = document.body;

    // Initialize Bootstrap collapse
    const collapse = new bootstrap.Collapse(mainNav, {
        toggle: false
    });

    // Toggle menu function
    function toggleMenu(show) {
        if (show) {
            menuOverlay.classList.add('show');
            mainNav.classList.add('show');
            body.classList.add('menu-open');
            menuOverlay.style.display = 'block';
        } else {
            menuOverlay.classList.remove('show');
            mainNav.classList.remove('show');
            body.classList.remove('menu-open');
            
            // Remove display: block after transition
            setTimeout(() => {
                if (!menuOverlay.classList.contains('show')) {
                    menuOverlay.style.display = 'none';
                }
            }, 300);
        }
    }

    // Toggle menu when clicking the button
    if (mobileMenuToggle) {
        mobileMenuToggle.addEventListener('click', function(e) {
            e.stopPropagation();
            const isExpanded = this.getAttribute('aria-expanded') === 'true';
            toggleMenu(!isExpanded);
        });
    }

    // Close menu when clicking on overlay
    if (menuOverlay) {
        menuOverlay.addEventListener('click', function() {
            toggleMenu(false);
            mobileMenuToggle.setAttribute('aria-expanded', 'false');
        });
    }

    // Close menu when clicking on nav links (for mobile)
    const navLinks = document.querySelectorAll('.main-nav .nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', function() {
            if (window.innerWidth <= 991.98) {
                toggleMenu(false);
                mobileMenuToggle.setAttribute('aria-expanded', 'false');
            }
        });
    });

    // Close menu when pressing Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && mainNav.classList.contains('show')) {
            toggleMenu(false);
            mobileMenuToggle.setAttribute('aria-expanded', 'false');
        }
    });

    // Handle window resize
    function handleResize() {
        if (window.innerWidth > 991.98) {
            // On desktop, ensure menu is closed and reset styles
            menuOverlay.style.display = 'none';
            menuOverlay.classList.remove('show');
            mainNav.classList.remove('show');
            body.classList.remove('menu-open');
            if (mobileMenuToggle) {
                mobileMenuToggle.setAttribute('aria-expanded', 'false');
            }
        }
    }

    // Add resize event listener
    window.addEventListener('resize', handleResize);

    // Clean up event listeners when the page is unloaded
    window.addEventListener('beforeunload', function() {
        window.removeEventListener('resize', handleResize);
    });
});

// Add smooth scrolling to all anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const targetId = this.getAttribute('href');
        if (targetId === '#') return;
        
        const targetElement = document.querySelector(targetId);
        if (targetElement) {
            window.scrollTo({
                top: targetElement.offsetTop - 70,
                behavior: 'smooth'
            });
            
            // Update URL without jumping
            if (history.pushState) {
                history.pushState(null, null, targetId);
            } else {
                location.hash = targetId;
            }
        }
    });
});

// Add active class to current section in navigation
window.addEventListener('scroll', function() {
    const scrollPosition = window.scrollY;
    
    // Add/remove active class based on scroll position
    document.querySelectorAll('section').forEach(section => {
        const sectionTop = section.offsetTop - 100;
        const sectionHeight = section.offsetHeight;
        const sectionId = section.getAttribute('id');
        
        if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
            document.querySelectorAll('.nav-menu a').forEach(link => {
                link.classList.remove('active');
                if (link.getAttribute('href') === `#${sectionId}`) {
                    link.classList.add('active');
                }
            });
        }
    });
});

// Initialize tooltips
var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl);
});
