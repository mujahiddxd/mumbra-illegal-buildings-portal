// admin-dashboard.js
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('adminToken');
    
    if (!token) {
        window.location.href = '/admin-login.html';
        return;
    }
    
    // Add token to all fetch requests
    const fetchWithAuth = async (url, options = {}) => {
        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        const response = await fetch(url, { ...options, headers });
        
        if (response.status === 401) {
            // Token expired or invalid
            localStorage.removeItem('adminToken');
            window.location.href = '/admin-login.html?session=expired';
            return;
        }
        
        return response;
    };
    
    // Use fetchWithAuth for all your API calls
});