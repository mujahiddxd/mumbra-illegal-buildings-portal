document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.querySelector('.mobile-menu-toggle');
    const mainNav = document.querySelector('.main-nav');
    const body = document.body;
    const navLinks = document.querySelectorAll('.nav-menu a, .dropdown-menu a');

    // Toggle mobile menu
    function toggleMenu() {
        menuToggle.classList.toggle('active');
        mainNav.classList.toggle('active');
        body.classList.toggle('menu-open');
    }

    // Close menu when clicking outside
    function closeMenuOnClickOutside(event) {
        if (!mainNav.contains(event.target) && !menuToggle.contains(event.target)) {
            if (mainNav.classList.contains('active')) {
                toggleMenu();
            }
        }
    }

    // Close menu when clicking on a link
    function closeMenuOnLinkClick() {
        if (window.innerWidth <= 992) { // Match this with your mobile breakpoint
            toggleMenu();
        }
    }

    // Event listeners
    if (menuToggle) {
        menuToggle.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleMenu();
        });
    }

    // Close menu when clicking outside
    document.addEventListener('click', closeMenuOnClickOutside);

    // Close menu when clicking on a navigation link
    navLinks.forEach(link => {
        link.addEventListener('click', closeMenuOnLinkClick);
    });

    // Close menu when window is resized to desktop view
    let resizeTimer;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(function() {
            if (window.innerWidth > 992) {
                menuToggle.classList.remove('active');
                mainNav.classList.remove('active');
                body.classList.remove('menu-open');
            }
        }, 250);
    });
});
