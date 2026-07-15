document.addEventListener('DOMContentLoaded', () => {
  const horaInput = document.getElementById('horaReserva');
  document.querySelectorAll('.time-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.time-btn').forEach((b) => {
        b.classList.remove('selected', 'btn-danger');
        b.classList.add('btn-outline-secondary');
      });
      btn.classList.add('selected', 'btn-danger');
      btn.classList.remove('btn-outline-secondary');
      if (horaInput) horaInput.value = btn.textContent.trim();
    });
  });

  const personasInput = document.getElementById('personasReserva');
  const clamp = (value) => Math.max(1, Math.min(20, value));
  document.querySelector('.btn-minus')?.addEventListener('click', () => {
    personasInput.value = clamp(Number(personasInput.value || 1) - 1);
  });
  document.querySelector('.btn-plus')?.addEventListener('click', () => {
    personasInput.value = clamp(Number(personasInput.value || 1) + 1);
  });

});
