# Vitrina de Productos — Supermercado PASO

Proyecto fullstack desarrollado en **Java + Spring Boot** y **Frontend Web (HTML/CSS/JS)** que implementa una vitrina de productos de supermercado conectada a un catálogo real de 4.032 registros, cumpliendo estrictamente con el arnés agéntico y las reglas de diseño de [`AGENTS.md`](./AGENTS.md).

---

## 1. Propósito del Sistema

Construir una vitrina de productos que:
- Consulte un catálogo real de **4.032 productos** a partir de [`data/catalog.csv`](./data/catalog.csv).
- Muestre tarjetas de productos con imagen, identificador, categoría, nombre, formato y precio.
- Permita buscar por nombre (insensible a mayúsculas y minúsculas).
- Permita filtrar por categoría y formato.
- Permita paginar resultados de forma determinista (8 productos por página, 504 páginas en total).
- Permita consultar el detalle completo de un producto en un cuadro de diálogo modal.
- Gestione cuatro estados obligatorios reales según las respuestas de la API:
  - **Cargando**: mientras se espera la resolución de la petición al servidor.
  - **Sin resultados**: cuando la búsqueda o filtros devuelven 0 productos.
  - **Error**: cuando ocurre un problema de red o fallo de servidor, con botón de reintento.
  - **Normal**: cuando hay productos que mostrar en la grilla.
- **Sin inventar datos**: todos los valores provienen fielmente de `data/catalog.csv`.
- **Sin lógica de negocio en el frontend**: búsqueda, filtrado, paginación y ordenamiento residen exclusivamente en el backend.

---

## 2. Arquitectura del Proyecto

El sistema está estructurado como un monolito desacoplado:

```text
desafio/
├── AGENTS.md                  # Reglas permanentes y arnés de desarrollo
├── verify.ps1                 # Script repetible de verificación de cumplimiento
├── data/
│   ├── catalog.csv            # Catálogo oficial (4.032 registros reales)
│   └── README.md              # Documentación del origen de datos
├── mockup/                    # Frontend (Mockup adaptado como cliente REST)
│   ├── index.html             # Estructura semántica de la vitrina
│   ├── styles.css             # Estilos y diseño visual
│   ├── app.js                 # Cliente asíncrono consumidor de la API REST
│   └── assets/                # Imágenes locales de muestra
└── backend/                   # Backend Java + Spring Boot
    ├── pom.xml                # Configuración Maven (Spring Boot 3.4.0, H2, JPA)
    └── src/
        ├── main/
        │   ├── java/com/nttdata/vitrina/
        │   │   ├── VitrinaApplication.java        # Punto de entrada de la aplicación
        │   │   ├── config/WebConfig.java          # Configuración de CORS y recursos estáticos
        │   │   ├── controller/ProductController.java # Endpoints REST obligatorios
        │   │   ├── dto/                           # Objetos de transferencia de datos
        │   │   │   ├── FiltersResponse.java
        │   │   │   ├── PageResponse.java
        │   │   │   ├── ProductDetailDto.java
        │   │   │   └── ProductSummaryDto.java
        │   │   ├── model/Product.java             # Entidad JPA para la tabla H2
        │   │   ├── repository/ProductRepository.java # Consultas JPA y Specifications
        │   │   └── service/
        │   │       ├── CatalogLoaderService.java  # Parser e ingesta de CSV al arrancar
        │   │       ├── ProductService.java        # Interfaz de lógica de negocio
        │   │       └── impl/ProductServiceImpl.java # Implementación de negocio
        │   └── resources/application.properties   # Parámetros de H2 y ruta del CSV
        └── test/java/com/nttdata/vitrina/
            ├── controller/ProductControllerTest.java # Pruebas de integración MockMvc
            └── service/
                ├── CatalogLoaderTest.java        # Verificación de carga en H2
                └── ProductServiceTest.java        # Pruebas de filtros y paginación
```

### Componentes Principales:
- **Base de Datos H2 en memoria**: Instancia relacional en memoria (`jdbc:h2:mem:catalogdb`).
- **Ingesta de Datos (`CatalogLoaderService`)**: Parser RFC 4180 que lee `data/catalog.csv` al inicializar Spring Boot (`ApplicationRunner`), preservando integridad de comillas, comas internas y persistiendo los 4.032 productos mediante `saveAll`.
- **Capa de Persistencia y Consultas (`ProductRepository`)**: Extiende `JpaRepository` y `JpaSpecificationExecutor` para la creación dinámica de predicados SQL y obtención de filtros con `DISTINCT`.
- **Capa de Negocio (`ProductServiceImpl`)**: Centraliza la paginación 1-based, normalización de límites, búsquedas por nombre, filtrado combinado y mapeo a DTOs.
- **Servicio Estático y CORS (`WebConfig`)**: Mapea el directorio `mockup/` para servir el frontend directamente desde Spring Boot y permite peticiones CORS para herramientas locales de desarrollo.

---

## 3. Endpoints REST

La API expone tres endpoints obligatorios bajo la ruta base `/api/products`:

### 3.1. `GET /api/products`
Obtiene productos paginados según criterios de búsqueda y filtros.

- **Parámetros de consulta (Query Params)**:
  - `page` (entero opcional, valor por defecto: `1`): Número de página (1-based).
  - `size` (entero opcional, valor por defecto: `8`): Cantidad de productos por página.
  - `search` o `q` (cadena opcional): Término de búsqueda en el nombre del producto.
  - `category` (cadena opcional): Filtro por categoría o grupo de categoría.
  - `format` (cadena opcional): Filtro por presentación o formato.
- **Respuesta (`200 OK`)**:
  ```json
  {
    "items": [
      {
        "id": "10043",
        "name": "Batido de chocolate 90% leche Puleva",
        "category": "Huevos, leche y mantequilla > Leche y bebidas vegetales",
        "categoryGroup": "Huevos, leche y mantequilla",
        "format": "6 mini bricks x 200 ml",
        "price": 2605,
        "priceUnit": "/pack",
        "originalPrice": 3040,
        "currency": "CLP",
        "imageUrl": "https://prod-mercadona.imgix.net/images/...",
        "productUrl": "https://tienda.mercadona.es/product/10043/..."
      }
    ],
    "page": 1,
    "size": 8,
    "totalElements": 4032,
    "totalPages": 504,
    "hasNext": true,
    "hasPrevious": false
  }
  ```

### 3.2. `GET /api/products/{id}`
Obtiene la ficha detallada de un producto específico para el modal de detalle.

- **Parámetro de ruta**: `id` del producto (ej: `10005`).
- **Respuesta exitosa (`200 OK`)**: Objeto `ProductDetailDto` con descripción completa, precios, alérgenos, fecha de extracción y enlaces.
- **Respuesta no encontrada (`404 Not Found`)**: Retornado si el identificador no existe.

### 3.3. `GET /api/products/filters`
Devuelve las opciones disponibles de filtrado obtenidas dinámicamente desde el catálogo.

- **Respuesta (`200 OK`)**:
  ```json
  {
    "categories": [
      "Aceite, especias y salsas",
      "Agua y refrescos",
      "Aperitivos",
      "..."
    ],
    "formats": [
      "6 mini bricks x 200 ml",
      "Bandeja",
      "Bote",
      "..."
    ]
  }
  ```

---

## 4. Ejecución del Proyecto

### Requisitos previos:
- **Java**: JDK 21 o superior (compatible con Java 25).
- **Maven**: Apache Maven 3.9+ (o wrapper).
- Navegador web moderno.

### Pasos para iniciar el sistema:

1. Abrir una terminal en el directorio `backend`:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
2. Una vez que la consola indique que la aplicación ha iniciado (puerto `8080`), abrir en el navegador:
   ```text
   http://localhost:8080/index.html
   ```
   *(También se puede acceder directamente a `http://localhost:8080/` o abrir `mockup/index.html` con Live Server / servidor web estático gracias al soporte de CORS configurado).*

---

## 5. Pruebas Automatizadas

El backend incluye una suite exhaustiva de 16 pruebas automatizadas:
- **`CatalogLoaderTest`**: Comprueba el arranque del contexto, la lectura del CSV y valida que existan exactamente 4.032 productos persistidos en H2.
- **`ProductServiceTest`**: Comprueba la paginación 1-based, búsqueda insensible a mayúsculas, filtros por categoría y formato, combinaciones múltiples y obtención de filtros.
- **`ProductControllerTest`**: Valida mediante `MockMvc` los códigos de estado HTTP (200, 404), la estructura JSON de salida y el comportamiento de los endpoints.

Para ejecutar las pruebas:
```bash
cd backend
mvn test
```

---

## 6. Verificador de Cumplimiento AGENTS.md

En cumplimiento de la Sección 7 de `AGENTS.md`, el repositorio incluye el script repetible [`verify.ps1`](./verify.ps1) en la raíz del proyecto. Este script verifica de forma automatizada:

1. **Anti-descarga masiva**: Que el frontend no cargue directamente `catalog.csv` ni realice peticiones con tamaños desbordados (`size=4032`, etc.).
2. **Anti-lógica en el frontend**: Que `mockup/app.js` no use `.filter()` ni `.slice()` para filtrar o paginar colecciones en memoria, delegando todo al backend.
3. **Integridad de datos**: Que no existan arrays estáticos de productos ni datos inventados, y que `catalog.csv` conserve sus 4.032 registros intactos.
4. **Paginación backend**: Que la paginación en la interfaz dependa estrictamente de los metadatos de respuesta de la API (`totalPages`, `totalElements`, `page`).
5. **Pruebas de backend**: Ejecuta la suite de pruebas Maven y valida 0 errores y 0 fallos.

### Para ejecutar el verificador:
```powershell
pwsh -File .\verify.ps1
```
