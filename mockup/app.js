const API_BASE = window.location.origin.startsWith('http')
  ? (window.location.port === '8080' ? '/api/products' : 'http://localhost:8080/api/products')
  : 'http://localhost:8080/api/products';

const grid = document.querySelector('#product-grid');
const search = document.querySelector('#search');
const categoryFilter = document.querySelector('#category-filter');
const formatFilter = document.querySelector('#format-filter');
const resultCount = document.querySelector('#result-count');
const dialog = document.querySelector('#product-dialog');
const prevPageBtn = document.querySelector('#prev-page');
const nextPageBtn = document.querySelector('#next-page');
const paginationPages = document.querySelector('#pagination-pages');

let currentPage = 1;
const pageSize = 8;
let totalPages = 1;
let totalElements = 0;
let estadoForzado = null;
let searchDebounceTimeout = null;

const PLANTILLAS = {
  cargando: `
    <div class="inline-state">
      <span class="state-icon spinner" aria-hidden="true"></span>
      <strong>Cargando productos</strong>
      <p>Espera mientras consultamos el catálogo.</p>
    </div>`,
  vacio: `
    <div class="inline-state empty">
      <span class="state-icon" aria-hidden="true">⌕</span>
      <strong>Sin resultados</strong>
      <p>Prueba con otra búsqueda o elimina algunos filtros.</p>
      <button type="button" id="btn-clear-filters">Limpiar filtros</button>
    </div>`,
  error: `
    <div class="inline-state error">
      <span class="state-icon" aria-hidden="true">!</span>
      <strong>No pudimos cargar el catálogo</strong>
      <p>Inténtalo nuevamente en unos momentos.</p>
      <button type="button" id="btn-retry">Reintentar</button>
    </div>`,
};

function formatPrice(value, currency) {
  if (value === null || value === undefined) return '';
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: currency || 'CLP',
    maximumFractionDigits: 0,
  }).format(value);
}

function productCard(product) {
  const displayImage = product.imageUrl || `assets/${product.id}.jpg`;
  return `
    <article class="product-card">
      <button class="card-button" type="button" data-product-id="${product.id}" aria-label="Ver detalle de ${product.name}">
        <span class="image-wrap">
          <img src="${displayImage}" alt="${product.name}" loading="lazy" />
          <span class="card-index">#${product.id}</span>
        </span>
        <span class="card-copy">
          <span class="card-type">${product.categoryGroup || product.category || ''}</span>
          <strong>${product.name}</strong>
          <span class="card-meta">${product.format || ''} <i>•</i> ${formatPrice(product.price, product.currency)}</span>
          <span class="view-link">Ver detalle <span aria-hidden="true">→</span></span>
        </span>
      </button>
    </article>`;
}

async function loadFilters() {
  try {
    const response = await fetch(`${API_BASE}/filters`);
    if (!response.ok) return;
    const data = await response.json();

    if (Array.isArray(data.categories)) {
      data.categories.forEach((cat) => {
        const option = document.createElement('option');
        option.value = cat;
        option.textContent = cat;
        categoryFilter.appendChild(option);
      });
    }

    if (Array.isArray(data.formats)) {
      data.formats.forEach((fmt) => {
        const option = document.createElement('option');
        option.value = fmt;
        option.textContent = fmt;
        formatFilter.appendChild(option);
      });
    }
  } catch (err) {
    console.warn('No se pudieron cargar los filtros desde el backend:', err);
  }
}

async function fetchProducts(page = 1) {
  currentPage = page;

  if (estadoForzado) {
    grid.innerHTML = PLANTILLAS[estadoForzado];
    resultCount.textContent = 'Referencia de estado: ' + estadoForzado;
    renderPagination(1, 1, false, false);
    return;
  }

  // Estado obligatorio: cargando
  grid.innerHTML = PLANTILLAS.cargando;
  resultCount.textContent = 'Consultando catálogo...';
  renderPagination(page, totalPages, false, false);

  const params = new URLSearchParams({
    page: String(page),
    size: String(pageSize),
  });

  const query = search.value.trim();
  const selectedCategory = categoryFilter.value;
  const selectedFormat = formatFilter.value;

  if (query) params.append('search', query);
  if (selectedCategory) params.append('category', selectedCategory);
  if (selectedFormat) params.append('format', selectedFormat);

  try {
    const response = await fetch(`${API_BASE}?${params.toString()}`);
    if (!response.ok) {
      throw new Error(`Error en servidor: ${response.status}`);
    }

    const data = await response.json();
    totalPages = data.totalPages || 0;
    totalElements = data.totalElements || 0;
    currentPage = data.page || page;

    // Estado obligatorio: sin resultados
    if (!data.items || data.items.length === 0) {
      grid.innerHTML = PLANTILLAS.vacio;
      resultCount.textContent = 'Sin resultados';
      renderPagination(1, 0, false, false);

      const clearBtn = document.querySelector('#btn-clear-filters');
      if (clearBtn) {
        clearBtn.addEventListener('click', () => {
          search.value = '';
          categoryFilter.value = '';
          formatFilter.value = '';
          fetchProducts(1);
        });
      }
      return;
    }

    // Estado obligatorio: normal
    grid.innerHTML = data.items.map(productCard).join('');

    const start = (data.page - 1) * data.size + 1;
    const end = Math.min(start + data.items.length - 1, data.totalElements);
    resultCount.textContent = `Mostrando ${start}-${end} de ${data.totalElements.toLocaleString('es-CL')}`;

    renderPagination(data.page, data.totalPages, data.hasPrevious, data.hasNext);

  } catch (error) {
    // Estado obligatorio: error
    console.error('Error al cargar productos:', error);
    grid.innerHTML = PLANTILLAS.error;
    resultCount.textContent = 'Error al cargar';
    renderPagination(1, 0, false, false);

    const retryBtn = document.querySelector('#btn-retry');
    if (retryBtn) {
      retryBtn.addEventListener('click', () => fetchProducts(currentPage));
    }
  }
}

function renderPagination(current, total, hasPrev, hasNext) {
  if (!prevPageBtn || !nextPageBtn || !paginationPages) return;

  prevPageBtn.disabled = !hasPrev;
  nextPageBtn.disabled = !hasNext;
  paginationPages.innerHTML = '';

  if (total <= 1) {
    return;
  }

  const pages = calculatePageItems(current, total);
  pages.forEach((p) => {
    if (p === '…') {
      const span = document.createElement('span');
      span.textContent = '…';
      paginationPages.appendChild(span);
    } else {
      const btn = document.createElement('span');
      btn.textContent = p;
      btn.style.cursor = 'pointer';
      if (p === current) {
        btn.classList.add('active');
        btn.setAttribute('aria-current', 'page');
      } else {
        btn.addEventListener('click', () => fetchProducts(p));
      }
      paginationPages.appendChild(btn);
    }
  });
}

function calculatePageItems(current, total) {
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  if (current <= 4) {
    return [1, 2, 3, 4, 5, '…', total];
  }

  if (current >= total - 3) {
    return [1, '…', total - 4, total - 3, total - 2, total - 1, total];
  }

  return [1, '…', current - 1, current, current + 1, '…', total];
}

async function openDetail(productId) {
  try {
    const response = await fetch(`${API_BASE}/${encodeURIComponent(productId)}`);
    if (!response.ok) {
      throw new Error(`No se pudo obtener detalle: ${response.status}`);
    }
    const product = await response.json();

    document.querySelector('#detail-id').textContent = `PRODUCTO #${product.id}`;
    document.querySelector('#detail-title').textContent = product.name;
    document.querySelector('#detail-description').textContent = product.description || '';
    const image = document.querySelector('#detail-image');
    image.src = product.imageUrl || `assets/${product.id}.jpg`;
    image.alt = product.name;

    const values = [
      ['Categoría', product.category || ''],
      ['Formato', product.format || ''],
      ['Unidad de precio', product.priceUnit || ''],
      ['Identificador', product.id],
    ];
    document.querySelector('#detail-list').innerHTML = values
      .map(([label, value]) => `<div><dt>${label}</dt><dd>${value}</dd></div>`)
      .join('');

    document.querySelector('#detail-price').textContent = formatPrice(product.price, product.currency);
    document.querySelector('#detail-currency').textContent = `Moneda: ${product.currency || 'CLP'}`;
    document.querySelector('#detail-original-price').textContent = `Precio original: ${formatPrice(product.originalPrice, product.currency)}`;
    document.querySelector('#detail-extracted-at').textContent = `Datos extraídos: ${product.extractedAt || ''}`;
    document.querySelector('#detail-source').href = product.productUrl || '#';

    dialog.showModal();
  } catch (error) {
    console.error('Error al abrir detalle:', error);
    alert('No se pudo cargar el detalle del producto.');
  }
}

// Event Listeners
grid.addEventListener('click', (event) => {
  const button = event.target.closest('[data-product-id]');
  if (!button) return;
  const productId = button.dataset.productId;
  if (productId) openDetail(productId);
});

document.querySelector('#dialog-close').addEventListener('click', () => dialog.close());
dialog.addEventListener('click', (event) => {
  if (event.target === dialog) dialog.close();
});

prevPageBtn.addEventListener('click', () => {
  if (currentPage > 1) fetchProducts(currentPage - 1);
});

nextPageBtn.addEventListener('click', () => {
  if (currentPage < totalPages) fetchProducts(currentPage + 1);
});

search.addEventListener('input', () => {
  clearTimeout(searchDebounceTimeout);
  searchDebounceTimeout = setTimeout(() => {
    fetchProducts(1);
  }, 300);
});

categoryFilter.addEventListener('change', () => fetchProducts(1));
formatFilter.addEventListener('change', () => fetchProducts(1));

// Referencia de estados manuales
document.querySelectorAll('.state-switch-buttons button').forEach((boton) => {
  boton.addEventListener('click', () => {
    document.querySelectorAll('.state-switch-buttons button').forEach((b) => b.classList.remove('on'));
    boton.classList.add('on');
    estadoForzado = boton.dataset.state === 'normal' ? null : boton.dataset.state;
    fetchProducts(currentPage);
  });
});

// Inicialización
loadFilters();
fetchProducts(1);
