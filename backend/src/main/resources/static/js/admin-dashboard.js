const dashboardOps = (() => {
  const storageKey = 'lenas_operacion_dashboard';
  const estados = {
    LIBRE: { label: 'Disponible', badge: 'bg-success' },
    ABIERTA: { label: 'Abierta', badge: 'bg-primary' },
    PENDIENTE: { label: 'Pendiente cocina', badge: 'bg-danger' },
    PREPARACION: { label: 'En preparación', badge: 'bg-warning text-dark' },
    LISTO: { label: 'Listo', badge: 'bg-success' },
    ENTREGADO: { label: 'Entregado', badge: 'bg-info text-dark' },
    CUENTA: { label: 'Cuenta solicitada', badge: 'bg-dark' },
    PAGADO: { label: 'Pagado', badge: 'bg-secondary' }
  };

  let productos = [];
  let state = null;
  let mesaSeleccionada = 1;

  const formatoSoles = (valor) => `S/ ${(Number(valor) || 0).toFixed(2)}`;
  const get = (id) => document.getElementById(id);

  function estadoInicial() {
    return {
      cajaAbierta: false,
      ventasTurno: 0,
      alertas: [],
      mesas: Array.from({ length: 12 }, (_, index) => ({
        id: index + 1,
        estado: 'LIBRE',
        clientes: 0,
        observaciones: '',
        items: [],
        metodoPago: 'EFECTIVO',
        descuento: 0
      }))
    };
  }

  function cargarEstado() {
    try {
      state = JSON.parse(localStorage.getItem(storageKey)) || estadoInicial();
    } catch (error) {
      state = estadoInicial();
    }
  }

  function guardarEstado() {
    localStorage.setItem(storageKey, JSON.stringify(state));
  }

  function cargarProductos() {
    productos = [...document.querySelectorAll('.dashboard-product-source')].map(element => ({
      id: Number(element.dataset.id),
      nombre: element.dataset.nombre,
      categoria: element.dataset.categoria,
      precio: Number(element.dataset.precio),
      imagen: element.dataset.imagen
    }));
  }

  function mesaActual() {
    return state.mesas.find(mesa => mesa.id === mesaSeleccionada);
  }

  function totalMesa(mesa) {
    return mesa.items.reduce((suma, item) => suma + item.precio * item.cantidad, 0);
  }

  function totalConDescuento(mesa) {
    return Math.max(totalMesa(mesa) - (Number(mesa.descuento) || 0), 0);
  }

  function badgeEstado(estado) {
    const config = estados[estado] || estados.LIBRE;
    return `<span class="badge ${config.badge}">${config.label}</span>`;
  }

  function seleccionarMesa(id) {
    mesaSeleccionada = Number(id);
    const mesa = mesaActual();
    get('opsMesaSeleccionada').textContent = `Mesa ${String(mesaSeleccionada).padStart(2, '0')}`;
    get('opsClientes').value = mesa.clientes || 2;
    get('opsObservaciones').value = mesa.observaciones || '';
    render();
  }

  function abrirMesa() {
    const mesa = mesaActual();
    if (mesa.estado === 'PAGADO') {
      mesa.items = [];
      mesa.descuento = 0;
    }
    if (mesa.estado === 'LIBRE' || mesa.estado === 'PAGADO') {
      mesa.estado = 'ABIERTA';
    }
    mesa.clientes = Math.max(Number(get('opsClientes').value) || 1, 1);
    guardarEstado();
    render();
  }

  function agregarProducto(id) {
    const mesa = mesaActual();
    if (mesa.estado === 'LIBRE' || mesa.estado === 'PAGADO') {
      abrirMesa();
    }
    if (['PENDIENTE', 'PREPARACION', 'LISTO', 'CUENTA'].includes(mesa.estado)) {
      mesa.estado = 'ABIERTA';
    }
    const producto = productos.find(item => item.id === Number(id));
    const existente = mesa.items.find(item => item.id === producto.id);
    if (existente) {
      existente.cantidad += 1;
    } else {
      mesa.items.push({ ...producto, cantidad: 1 });
    }
    guardarEstado();
    render();
  }

  function quitarProducto(id) {
    const mesa = mesaActual();
    mesa.items = mesa.items
      .map(item => item.id === Number(id) ? { ...item, cantidad: item.cantidad - 1 } : item)
      .filter(item => item.cantidad > 0);
    guardarEstado();
    render();
  }

  function enviarCocina() {
    const mesa = mesaActual();
    if (!mesa.items.length) {
      alert('Agrega al menos un producto antes de enviar a cocina.');
      return;
    }
    mesa.estado = 'PENDIENTE';
    mesa.observaciones = get('opsObservaciones').value.trim();
    guardarEstado();
    render();
  }

  function cambiarEstadoMesa(id, estado) {
    const mesa = state.mesas.find(item => item.id === Number(id));
    mesa.estado = estado;
    guardarEstado();
    render();
  }

  function solicitarCuenta(id = mesaSeleccionada) {
    const mesa = state.mesas.find(item => item.id === Number(id));
    if (!mesa.items.length) {
      alert('La mesa no tiene consumo.');
      return;
    }
    mesa.estado = 'CUENTA';
    guardarEstado();
    render();
  }

  function cobrarMesa(id) {
    if (!state.cajaAbierta) {
      alert('Primero abre caja para registrar pagos.');
      return;
    }
    const mesa = state.mesas.find(item => item.id === Number(id));
    state.ventasTurno += totalConDescuento(mesa);
    mesa.estado = 'PAGADO';
    mesa.clientes = 0;
    mesa.items = [];
    mesa.observaciones = '';
    mesa.descuento = 0;
    guardarEstado();
    render();
  }

  function actualizarPagoMesa(id, campo, valor) {
    const mesa = state.mesas.find(item => item.id === Number(id));
    mesa[campo] = campo === 'descuento' ? Number(valor) || 0 : valor;
    guardarEstado();
    renderCaja();
    renderMesas();
  }

  function toggleCaja() {
    state.cajaAbierta = !state.cajaAbierta;
    guardarEstado();
    renderCaja();
  }

  function reportarAgotado() {
    const producto = get('opsAgotado').value.trim();
    if (!producto) return;
    state.alertas.unshift(`Producto agotado: ${producto}`);
    get('opsAgotado').value = '';
    guardarEstado();
    renderCocina();
  }

  function reportarDemora() {
    state.alertas.unshift('Cocina reportó demora de 10 a 15 minutos.');
    guardarEstado();
    renderCocina();
  }

  function resetDemo() {
    if (!confirm('¿Reiniciar mesas, comandas y caja del dashboard?')) return;
    state = estadoInicial();
    mesaSeleccionada = 1;
    guardarEstado();
    render();
  }

  function renderMesas() {
    const grid = get('opsMesas');
    if (!grid) return;
    grid.innerHTML = state.mesas.map(mesa => `
      <button type="button" class="ops-table-card ${mesa.id === mesaSeleccionada ? 'selected' : ''} ${mesa.estado.toLowerCase()}"
              onclick="dashboardOps.seleccionarMesa(${mesa.id})">
        <strong>${String(mesa.id).padStart(2, '0')}</strong>
        <small>${estados[mesa.estado].label}</small>
      </button>`).join('');

    const tbody = get('opsTablaMesas');
    tbody.innerHTML = state.mesas.map(mesa => `
      <tr>
        <td class="fw-bold">Mesa ${String(mesa.id).padStart(2, '0')}</td>
        <td>${badgeEstado(mesa.estado)}</td>
        <td>${mesa.clientes || '-'}</td>
        <td>${mesa.items.length ? mesa.items.map(item => `${item.cantidad} ${item.nombre}`).join('<br>') : '-'}</td>
        <td class="fw-bold text-danger">${formatoSoles(totalMesa(mesa))}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-danger" onclick="dashboardOps.seleccionarMesa(${mesa.id})">Atender</button>
          ${mesa.estado === 'LISTO' ? `<button class="btn btn-sm btn-success" onclick="dashboardOps.cambiarEstadoMesa(${mesa.id}, 'ENTREGADO')">Entregado</button>` : ''}
          ${mesa.items.length ? `<button class="btn btn-sm btn-dark" onclick="dashboardOps.solicitarCuenta(${mesa.id})">Cuenta</button>` : ''}
        </td>
      </tr>`).join('');
  }

  function renderProductos() {
    const query = (get('opsBuscarProducto')?.value || '').toLowerCase();
    const lista = get('opsProductos');
    if (!lista) return;
    const filtrados = productos.filter(producto =>
      producto.nombre.toLowerCase().includes(query) ||
      producto.categoria.toLowerCase().includes(query)
    );
    lista.innerHTML = filtrados.slice(0, 10).map(producto => `
      <button type="button" class="ops-product-item" onclick="dashboardOps.agregarProducto(${producto.id})">
        <img src="${producto.imagen || ''}" alt="${producto.nombre}">
        <span><strong>${producto.nombre}</strong><small>${producto.categoria}</small></span>
        <b>${formatoSoles(producto.precio)}</b>
      </button>`).join('') || '<p class="text-muted text-center py-3">No hay productos con ese nombre.</p>';
  }

  function renderOrden() {
    const mesa = mesaActual();
    get('opsOrdenTotal').textContent = formatoSoles(totalMesa(mesa));
    const contenedor = get('opsOrdenActual');
    if (!mesa.items.length) {
      contenedor.innerHTML = '<p class="text-muted text-center py-4">Selecciona productos para la mesa.</p>';
      return;
    }
    contenedor.innerHTML = mesa.items.map(item => `
      <div class="ops-order-item">
        <div><strong>${item.cantidad} ${item.nombre}</strong><small>${formatoSoles(item.precio * item.cantidad)}</small></div>
        <button class="btn btn-sm btn-outline-danger" onclick="dashboardOps.quitarProducto(${item.id})"><i class="bi bi-dash"></i></button>
      </div>`).join('');
  }

  function renderCocina() {
    const columnas = {
      PENDIENTE: get('kitchenPendiente'),
      PREPARACION: get('kitchenPreparacion'),
      LISTO: get('kitchenListo')
    };
    Object.values(columnas).forEach(columna => columna.innerHTML = '');
    ['PENDIENTE', 'PREPARACION', 'LISTO'].forEach(estado => {
      const mesas = state.mesas.filter(mesa => mesa.estado === estado);
      columnas[estado].innerHTML = mesas.map(mesa => `
        <div class="command-mini ${estado === 'PREPARACION' ? 'warning' : estado === 'LISTO' ? 'success' : ''}">
          <strong>Mesa ${String(mesa.id).padStart(2, '0')}</strong>
          <span>Clientes: ${mesa.clientes || '-'}</span>
          <p>${mesa.items.map(item => `${item.cantidad} ${item.nombre}`).join('<br>')}</p>
          ${mesa.observaciones ? `<small class="d-block text-muted mb-2">Obs: ${mesa.observaciones}</small>` : ''}
          ${estado === 'PENDIENTE' ? `<button class="btn btn-sm btn-danger" onclick="dashboardOps.cambiarEstadoMesa(${mesa.id}, 'PREPARACION')">Iniciar preparación</button>` : ''}
          ${estado === 'PREPARACION' ? `<button class="btn btn-sm btn-warning" onclick="dashboardOps.cambiarEstadoMesa(${mesa.id}, 'LISTO')">Marcar listo</button>` : ''}
          ${estado === 'LISTO' ? `<button class="btn btn-sm btn-outline-success" onclick="dashboardOps.cambiarEstadoMesa(${mesa.id}, 'ENTREGADO')">Entregado a mozo</button>` : ''}
        </div>`).join('') || '<p class="text-muted small">Sin comandas.</p>';
    });
    get('opsAlertasCocina').textContent = state.alertas.length ? state.alertas.slice(0, 3).join(' · ') : 'Sin alertas registradas.';
  }

  function renderCaja() {
    get('opsCajaEstado').textContent = state.cajaAbierta ? 'Abierta' : 'Cerrada';
    get('opsCajaBtn').textContent = state.cajaAbierta ? 'Cerrar caja' : 'Abrir caja';
    const pendientes = state.mesas.filter(mesa => mesa.estado === 'CUENTA');
    get('opsPendientesPago').textContent = pendientes.length;
    get('opsVentasTurno').textContent = formatoSoles(state.ventasTurno);
    get('cashierCuentas').innerHTML = pendientes.map(mesa => `
      <tr>
        <td class="fw-bold">Mesa ${String(mesa.id).padStart(2, '0')}</td>
        <td><strong>${formatoSoles(totalMesa(mesa))}</strong><small class="d-block text-muted">${mesa.items.length} productos</small></td>
        <td>
          <select class="form-select form-select-sm" onchange="dashboardOps.actualizarPagoMesa(${mesa.id}, 'metodoPago', this.value)">
            ${['EFECTIVO', 'TARJETA', 'YAPE', 'PLIN'].map(metodo => `<option ${mesa.metodoPago === metodo ? 'selected' : ''}>${metodo}</option>`).join('')}
          </select>
        </td>
        <td><input class="form-control form-control-sm" type="number" min="0" value="${mesa.descuento || 0}" onchange="dashboardOps.actualizarPagoMesa(${mesa.id}, 'descuento', this.value)"></td>
        <td class="text-end">
          <strong class="text-danger me-2">${formatoSoles(totalConDescuento(mesa))}</strong>
          <button class="btn btn-sm btn-danger" onclick="dashboardOps.cobrarMesa(${mesa.id})">Cobrar</button>
        </td>
      </tr>`).join('') || '<tr><td colspan="5" class="text-center text-muted py-4">No hay mesas pendientes de pago.</td></tr>';
  }

  function render() {
    renderMesas();
    renderProductos();
    renderOrden();
    renderCocina();
    renderCaja();
  }

  function init() {
    cargarProductos();
    cargarEstado();
    seleccionarMesa(1);
    get('opsBuscarProducto')?.addEventListener('input', renderProductos);
    activarPestanaInicial();
  }

  function activarPestanaInicial() {
    const section = window.dashboardInitialSection || '';
    const hash = window.location.hash.replace('#', '');
    const debeAbrirAdmin = window.dashboardEditingProduct || ['productos', 'reservas', 'ventas', 'admin-extra'].includes(section) || ['productos', 'reservas', 'ventas', 'admin-extra'].includes(hash);

    if (!debeAbrirAdmin || !window.bootstrap) {
      return;
    }

    const adminTab = document.querySelector('[data-bs-target="#rol-admin"]');
    if (adminTab) {
      bootstrap.Tab.getOrCreateInstance(adminTab).show();
    }

    const targetId = hash || section || 'productos';
    const target = document.getElementById(targetId);
    if (target) {
      setTimeout(() => target.scrollIntoView({ behavior: 'smooth', block: 'start' }), 150);
    }
  }

  document.addEventListener('DOMContentLoaded', init);

  return {
    seleccionarMesa,
    abrirMesa,
    agregarProducto,
    quitarProducto,
    enviarCocina,
    cambiarEstadoMesa,
    solicitarCuenta,
    cobrarMesa,
    actualizarPagoMesa,
    toggleCaja,
    reportarAgotado,
    reportarDemora,
    resetDemo
  };
})();
