let carritoPaginaTotal = 0;
let metodoPagoPagina = 'TARJETA';

const valor = (id) => document.getElementById(id)?.value.trim() || '';

function mostrarToastPagina(mensaje, tipo = 'success') {
  const toast = document.getElementById('toastCarritoPagina');
  toast.classList.remove('bg-success', 'bg-danger', 'bg-warning');
  toast.classList.add(tipo === 'success' ? 'bg-success' : tipo === 'error' ? 'bg-danger' : 'bg-warning');
  document.getElementById('toast-carrito-pagina-msg').textContent = mensaje;
  new bootstrap.Toast(toast, { delay: 2500 }).show();
}

async function cargarCarritoPagina() {
  const res = await fetch('/carrito');
  if (res.status === 401) {
    window.location.href = '/login';
    return;
  }
  const data = await res.json();
  renderCarritoPagina(data.items, data.total);
}

function renderCarritoPagina(items, total) {
  const contenedor = document.getElementById('checkout-items');
  carritoPaginaTotal = total || 0;
  document.getElementById('checkout-total').textContent = `S/ ${carritoPaginaTotal.toFixed(2)}`;
  document.getElementById('checkout-total-form').textContent = `S/ ${carritoPaginaTotal.toFixed(2)}`;

  const cantidad = (items || []).reduce((suma, item) => suma + item.cantidad, 0);
  const badge = document.getElementById('cart-badge');
  if (badge) {
    badge.classList.toggle('d-none', cantidad <= 0);
    badge.textContent = cantidad;
  }

  if (!items || items.length === 0) {
    contenedor.innerHTML = `
      <div class="text-center py-5">
        <i class="bi bi-bag-x fs-1 text-muted"></i>
        <h5 class="fw-bold mt-3">Tu carrito está vacío</h5>
        <p class="text-muted">Agrega productos desde la carta para continuar.</p>
        <a href="/carta" class="btn btn-danger">Ver carta</a>
      </div>`;
    document.getElementById('pageBtnPagar').disabled = true;
    return;
  }

  document.getElementById('pageBtnPagar').disabled = false;
  contenedor.innerHTML = items.map(item => `
    <div class="cart-page-item">
      <img src="${item.imagenUrl}" alt="${item.nombre}">
      <div class="flex-grow-1">
        <strong>${item.nombre}</strong>
        <small>S/ ${item.precio.toFixed(2)} x ${item.cantidad}</small>
        <span>S/ ${item.subtotal.toFixed(2)}</span>
      </div>
      <button class="btn btn-sm btn-outline-danger rounded-circle" onclick="quitarCarritoPagina(${item.productoId})">
        <i class="bi bi-trash"></i>
      </button>
    </div>`).join('');
}

async function quitarCarritoPagina(productoId) {
  const res = await fetch(`/carrito/quitar/${productoId}`, { method: 'DELETE' });
  const data = await res.json();
  renderCarritoPagina(data.items, data.total);
}

async function vaciarCarritoPagina() {
  await fetch('/carrito/vaciar', { method: 'DELETE' });
  renderCarritoPagina([], 0);
}

function datosPagoPagina() {
  return {
    metodoEntrega: document.querySelector('input[name="metodoEntrega"]:checked').value,
    direccionEntrega: valor('pageDireccionEntrega'),
    metodoPago: metodoPagoPagina,
    numeroTarjeta: valor('pageNumeroTarjeta'),
    vencimiento: valor('pageVencimiento'),
    cvv: valor('pageCvv'),
    nombreTarjeta: valor('pageNombreTarjeta'),
    numeroOperacion: valor('pageNumeroOperacion'),
    numeroYapePlin: valor('pageNumeroYapePlin'),
    codigoAprobacion: valor('pageCodigoAprobacion')
  };
}

async function procesarPagoPagina() {
  const boton = document.getElementById('pageBtnPagar');
  boton.disabled = true;
  boton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Procesando...';

  const res = await fetch('/carrito/pagar', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(datosPagoPagina())
  });
  const data = await res.json();

  if (res.ok) {
    mostrarToastPagina(`Pedido #${data.ventaId} confirmado`, 'success');
    renderCarritoPagina([], 0);
    setTimeout(() => window.location.href = '/perfil', 1200);
    return;
  }

  boton.disabled = false;
  boton.innerHTML = '<i class="bi bi-lock-fill me-2"></i>Pagar pedido';
  mostrarToastPagina(data.error || 'Error al procesar el pago', 'error');
}

document.addEventListener('DOMContentLoaded', () => {
  cargarCarritoPagina();

  document.querySelectorAll('#pageTabsPago .nav-link').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('#pageTabsPago .nav-link').forEach(item => item.classList.remove('active'));
      tab.classList.add('active');
      metodoPagoPagina = tab.dataset.metodo;
      document.getElementById('pageCamposTarjeta').classList.toggle('d-none', metodoPagoPagina !== 'TARJETA');
      document.getElementById('pageCamposYape').classList.toggle('d-none', metodoPagoPagina !== 'YAPE_PLIN');
    });
  });

  document.querySelectorAll('input[name="metodoEntrega"]').forEach(input => {
    input.addEventListener('change', () => {
      document.getElementById('pageGrupoDireccion').classList.toggle('d-none', input.value === 'RECOJO' && input.checked);
    });
  });

  document.getElementById('pageNumeroTarjeta').addEventListener('input', event => {
    event.target.value = event.target.value.replace(/\D/g, '').replace(/(.{4})/g, '$1 ').trim();
  });

  document.getElementById('pageVencimiento').addEventListener('input', event => {
    event.target.value = event.target.value.replace(/\D/g, '').replace(/^(\d{2})(\d)/, '$1/$2').substring(0, 5);
  });
});
