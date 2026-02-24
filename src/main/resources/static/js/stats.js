document.addEventListener('DOMContentLoaded', function() {
    const initCounters = () => {
        const counters = document.querySelectorAll('.stat-number');
        const speed = 200;
        counters.forEach(counter => {
            const target = +counter.getAttribute('data-target');
            const count = +counter.innerText;
            const increment = target / speed;
            if (count < target) {
                counter.innerText = Math.ceil(count + increment);
                setTimeout(initCounters, 1);
            } else {
                counter.innerText = target.toLocaleString();
            }
        });
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                initCounters();
                document.querySelectorAll('.progress-fill').forEach(bar => {
                    bar.style.transition = 'width 2s ease-out';
                    void bar.offsetWidth;
                    bar.style.width = window.getComputedStyle(bar).width;
                });
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.3 });

    const statsSection = document.querySelector('.stats-section');
    if (statsSection) {
        observer.observe(statsSection);
    }
});


