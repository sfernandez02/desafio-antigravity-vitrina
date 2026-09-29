# AGENTS.md — Arnés agéntico del proyecto “Vitrina de productos”

Este archivo define las reglas permanentes que debes respetar al modificar este repositorio.
Tu función es implementar cambios siguiendo estas instrucciones, sin inventar datos y sin romper la arquitectura.

---

## 1. Propósito del proyecto

Construir una vitrina de productos que:

- consulte un catálogo real de 4.032 productos,
- muestre cards con imagen, nombre, precio y atributos,
- permita buscar por nombre,
- permita filtrar por categoría y formato,
- permita paginar resultados,
- permita ver el detalle de un producto,
- muestre estados: cargando, sin resultados, error, normal,
- **sin inventar datos**,
- **sin mover lógica de negocio al frontend**.

El frontend es un mockup.  
La lógica real debe vivir en el backend.

---

## 2. Arquitectura obligatoria

Debes respetar esta arquitectura:

### Backend (Java + Spring Boot + H2)
- Cargar el archivo `catalog.csv` en una base de datos H2.
- Exponer tres endpoints obligatorios:
  - `GET /api/products` → búsqueda, filtros y paginación.
  - `GET /api/products/{id}` → detalle de producto.
  - `GET /api/products/filters` → categorías y formatos disponibles.
- Toda la lógica de negocio vive aquí:
  - búsqueda,
  - filtros,
  - paginación,
  - ordenamiento,
  - validación de datos.

### Frontend (mockup)
- No debe contener lógica de negocio.
- Solo debe:
  - llamar a las APIs del backend,
  - mostrar resultados,
  - mostrar estados según la respuesta del backend,
  - abrir el detalle con datos entregados por el backend.

### Prohibido en el frontend
- Filtrar productos.
- Buscar productos.
- Paginar productos.
- Cargar el CSV.
- Inventar valores.
- Descargar el catálogo completo.

---

## 3. Datos: reglas estrictas

- **No inventes datos.**
- **No completes campos con valores ficticios.**
- **No generes precios, descuentos ni valoraciones.**
- **Todo valor visible debe provenir del catálogo real (`catalog.csv`).**
- **No modifiques el CSV.**

---

## 4. Límites de implementación

### Está permitido:
- Crear archivos nuevos en backend.
- Modificar controladores, servicios y repositorios.
- Modificar el frontend solo para conectarlo al backend.
- Crear pruebas o verificaciones.
- Crear documentación.

### Está prohibido:
- Mover lógica de negocio al frontend.
- Crear endpoints que devuelvan datos inventados.
- Cambiar la estructura del CSV.
- Eliminar archivos del mockup.
- Cambiar estilos o diseño sin autorización explícita.
- Crear microservicios adicionales (este proyecto es monolítico).
- Modificar configuraciones críticas sin autorización.

---

## 5. Estados obligatorios en el frontend

El frontend debe mostrar estos estados según la respuesta del backend:

- **cargando** → mientras se espera la API.
- **sin resultados** → cuando la API devuelve 0 productos.
- **error** → cuando la API falla.
- **normal** → cuando hay resultados.

No deben ser estados simulados: deben depender de la API real.

---

## 6. Flujo de trabajo obligatorio

Debes seguir este flujo:

1. Transformar la consigna y el mockup en una especificación escrita.
2. Esperar revisión humana.
3. Proponer una arquitectura basada en Spring Boot + H2.
4. Esperar revisión humana.
5. Implementar en pasos pequeños.
6. Esperar revisión humana después de cada cambio.
7. Corregir cualquier desviación.
8. Verificar que la aplicación cumple la especificación.

---

## 7. Verificación mínima

Debes incluir al menos una verificación repetible que detecte:

- si el frontend intenta descargar el catálogo completo,
- si se inventan datos,
- si los filtros se ejecutan en el frontend,
- si la paginación no proviene del backend.

Puede ser una prueba automatizada o un script simple.

---

## 8. Responsabilidad

El agente implementa.  
El equipo revisa, corrige y verifica.

El arnés no reemplaza la responsabilidad humana.

