document.addEventListener('DOMContentLoaded', () => {
  const dniInput = document.getElementById('dni');
  const nombreInput = document.getElementById('nombre');
  const dniMensaje = document.getElementById('dniMensaje');
  let ultimoDniConsultado = '';

  if (!dniInput || !nombreInput || !dniMensaje) {
    return;
  }

  const mostrarMensaje = (texto, clase) => {
    dniMensaje.textContent = texto;
    dniMensaje.className = `small mt-1 ${clase}`;
  };

  dniInput.addEventListener('input', async () => {
    dniInput.value = dniInput.value.replace(/\D/g, '').slice(0, 8);
    const dni = dniInput.value;

    if (dni.length < 8) {
      ultimoDniConsultado = '';
      mostrarMensaje('', '');
      return;
    }

    if (dni === ultimoDniConsultado) {
      return;
    }

    ultimoDniConsultado = dni;
    mostrarMensaje('Consultando RENIEC...', 'text-muted');

    try {
      const response = await fetch(`/api/reniec/dni?numero=${encodeURIComponent(dni)}`);

      if (!response.ok) {
        nombreInput.value = '';
        mostrarMensaje('No se encontraron datos para este DNI.', 'text-danger');
        return;
      }

      const persona = await response.json();
      const nombreCompleto = persona.full_name || persona.fullName || [
        persona.first_last_name || persona.firstLastName,
        persona.second_last_name || persona.secondLastName,
        persona.first_name || persona.firstName
      ].filter(Boolean).join(' ');

      if (!nombreCompleto) {
        nombreInput.value = '';
        mostrarMensaje('RENIEC respondio, pero no envio el nombre.', 'text-danger');
        return;
      }

      nombreInput.value = nombreCompleto;
      nombreInput.dispatchEvent(new Event('input', { bubbles: true }));
      mostrarMensaje('Nombre completado automaticamente.', 'text-success');
    } catch (error) {
      mostrarMensaje('No se pudo consultar RENIEC en este momento.', 'text-danger');
    }
  });
});
