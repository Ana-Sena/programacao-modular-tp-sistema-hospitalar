// A única interação da sprint é a navegação entre telas estáticas.
(function () {
  'use strict';

  const screens = document.querySelectorAll('.screen');
  const links = document.querySelectorAll('nav a');

  function navigate() {
    const requested = window.location.hash.slice(1);
    const active = Array.from(screens).find(screen => screen.id === requested)
      || document.getElementById('pacientes');

    screens.forEach(screen => {
      screen.hidden = screen !== active;
    });

    links.forEach(link => {
      if (link.hash === `#${active.id}`) {
        link.setAttribute('aria-current', 'page');
      } else {
        link.removeAttribute('aria-current');
      }
    });

    document.title = `${active.querySelector('h1').textContent} · MedCare`;
  }

  window.addEventListener('hashchange', navigate);
  navigate();
})();
