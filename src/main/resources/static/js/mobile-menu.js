document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.querySelector('.mobile-menu-toggle');
    const mainNav = document.querySelector('.main-nav');
    const body = document.body;
    const navLinks = document.querySelectorAll('.nav-menu a, .dropdown-menu a');

    function toggleMenu() {
        menuToggle.classList.toggle('active');
        mainNav.classList.toggle('active');
        body.classList.toggle('menu-open');
    }

    function closeMenuOnClickOutside(event) {
        if (!mainNav.contains(event.target) && !menuToggle.contains(event.target)) {
            if (mainNav.classList.contains('active')) {
                toggleMenu();
            }
        }
    }

    function closeMenuOnLinkClick() {
        if (window.innerWidth <= 992) {
            toggleMenu();
        }
    }

    if (menuToggle) {
        menuToggle.addEventListener('click', function(e) {
            e.stopPropagation();
            toggleMenu();
        });
    }

    document.addEventListener('click', closeMenuOnClickOutside);

    navLinks.forEach(link => {
        link.addEventListener('click', closeMenuOnLinkClick);
    });

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


