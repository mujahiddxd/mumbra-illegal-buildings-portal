// Statistics counter animation
document.addEventListener('DOMContentLoaded', function() {
    // Initialize counters
    const initCounters = () => {
        const counters = document.querySelectorAll('.stat-number');
        const speed = 200; // The lower the slower
        
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

    // Intersection Observer for scroll-triggered animation
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                // Start counter animation
                initCounters();
                // Animate progress bars
                document.querySelectorAll('.progress-fill').forEach(bar => {
                    bar.style.transition = 'width 2s ease-out';
                    // Force reflow to trigger the animation
                    void bar.offsetWidth;
                    bar.style.width = window.getComputedStyle(bar).width;
                });
                // Unobserve after animation starts
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.3 });

    // Observe the stats section
    const statsSection = document.querySelector('.stats-section');
    if (statsSection) {
        observer.observe(statsSection);
    }

    // Card hover effect
    const cards = document.querySelectorAll('.stat-card');
    cards.forEach(card => {
        card.addEventListener('mousemove', function(e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            this.style.setProperty('--mouse-x', `${x}px`);
            this.style.setProperty('--mouse-y', `${y}px`);
        });
    });
});
