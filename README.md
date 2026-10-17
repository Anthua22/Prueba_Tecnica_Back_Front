# Previsión del tiempo (AEMET)

Aplicación web para consultar la previsión del día siguiente en cualquier municipio de España.

- **Backend**: Java 21 + Spring Boot 3 + Swagger/OpenAPI (springdoc) — `backend/`
- **Frontend**: Angular (última versión) + Angular Material — `frontend/` (se genera con `setup-frontend.sh`)
- **CI**: GitHub Actions — `.github/workflows/ci.yml`

## Requisitos

Java 21, Maven 3.9+, Node 20.19+ (o 22) y una API Key de AEMET.

## Ejecución en local

### 1. Backend

```bash
cd backend
export AEMET_API_KEY="tu_api_key"        # Windows (PowerShell): $env:AEMET_API_KEY="tu_api_key"
mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Al arrancar precarga el listado de municipios (tarda unos segundos la primera vez).

Endpoints:

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/municipios?nombre=Ag` | Municipios cuyo nombre empieza por el prefijo → `[{codigo, nombre}]` |
| GET | `/api/municipios/{codigo}/prediccion?unidadTemperatura=G_FAH` | Previsión de mañana. `unidadTemperatura` es opcional (`G_CEL` por defecto) |

### 2. Frontend

```bash
cd frontend
npm start        # http://localhost:4200  (proxy a http://localhost:8080)
```

### Tests

```bash
cd backend && mvn verify
cd frontend && npx ng test --watch=false
```

## Arquitectura

**Backend** (capas, cada una con una sola responsabilidad):

```
api/         Controladores REST, DTOs públicos y mapeo (contrato con el frontend)
service/     Lógica de negocio (filtrar municipios, calcular media, convertir unidades)
client/      AemetClient: único punto que conoce AEMET (API key, flujo en 2 pasos, errores, caché)
domain/      Modelo de dominio independiente de AEMET y de la API pública
config/      Propiedades, cachés, reloj, OpenAPI
exception/   Errores y su traducción a respuestas HTTP (ProblemDetail)
```

Si mañana se cambia de proveedor (o se añade otro), solo cambia `client/`. Si el contrato público evoluciona, solo cambia `api/`.

**Frontend**: `core/` (modelos y servicios: API y preferencias) y `features/weather/` (componente contenedor con el estado + componentes de presentación: autocompletado y tarjeta de previsión). Cualquier cambio de municipio o unidad refresca la UI vía signals + RxJS (`switchMap` cancela peticiones obsoletas).

Decisiones:
- **Temperatura media** = (máxima + mínima) / 2 del día siguiente, convertida a la unidad pedida (AEMET siempre da Celsius) y redondeada a 1 decimal.
- **Probabilidad de precipitación**: se devuelven las franjas de 6 horas (`00-06`, `06-12`, `12-18`, `18-24`); si AEMET no las ofrece, las que haya.
- **Día siguiente** se calcula con la zona horaria `Europe/Madrid` y se busca por fecha, no por posición en el array.
- La API key va por variable de entorno y la app no arranca sin ella.

---

## Preguntas

### 1. Las llamadas al listado de municipios son lentas. ¿Cómo mejorar el tiempo de respuesta?

Como el listado casi no cambia, lo guardamos en **caché**: la primera petición paga la llamada a AEMET y el resto se sirven desde memoria en milisegundos. En el proyecto:
- Caché en memoria (Caffeine) con TTL de 24 h en `AemetClient.getMunicipios()` (`@Cacheable`).
- **Precarga al arrancar** (`CacheWarmup`), de modo que ningún usuario sufre la primera llamada lenta.
- `sync = true`: si llegan varias peticiones a la vez con la caché vacía, solo una llama a AEMET.
- En el frontend, `debounceTime` + `switchMap` para no lanzar una búsqueda por cada tecla.

Mejoras posibles: refresco programado en segundo plano (`@Scheduled`) para que nunca caduque en caliente, caché compartida (Redis) si hay varias instancias, persistir el listado en fichero/BD para sobrevivir a reinicios, un índice (trie / lista ordenada con búsqueda binaria) si el filtrado llegara a ser un problema, y cabeceras `Cache-Control` en nuestra API para que el navegador/CDN también cacheen.

### 2. ¿Por qué AEMET devuelve una URL en vez de los datos directamente?

Es un patrón de respuesta en dos pasos (similar a "claim check"). Motivos probables:
- **Separar el control del acceso de la entrega de datos**: la API autentica, aplica cuotas y limita peticiones con una respuesta pequeña y barata; la descarga pesada se sirve aparte.
- **Escalabilidad**: los ficheros de predicción se generan en procesos por lotes y se pueden servir como contenido estático/cacheado (CDN, almacenamiento), sin pasar por la lógica de la API.
- **Desacoplar y poder cambiar** dónde y cómo se almacenan los datos sin romper a los clientes, y ofrecer los metadatos (descripción de campos) por separado.
- **Controlar el consumo**: URLs temporales evitan que se difundan enlaces permanentes y simplifican la caducidad.

### 3. Recordar el último municipio y cargar la previsión automáticamente

Implementado en el frontend: `PreferencesStorage` guarda en `localStorage` el último municipio y la unidad cada vez que cambian (`effect` en `WeatherPageComponent`). Al abrir la app se leen, se rellenan los campos y, como el estado ya contiene un municipio, se dispara la petición de previsión sin intervención del usuario. Se versiona la clave (`v1`) y se tolera contenido corrupto o `localStorage` no disponible.

Si se quisiera que persistiera entre dispositivos, habría que asociarlo a un usuario (cuenta o cookie de identificación) y guardar la preferencia en el backend. Como extensión, se podría ofrecer geolocalización para proponer el municipio la primera vez.

### 4. Carga excesiva sobre AEMET: ¿qué hacer y cómo implementarlo?

La clave es **dejar de pedir a AEMET lo que ya sabemos**: la predicción cambia pocas veces al día, pero nuestros usuarios la piden muchas veces.

Implementado:
- **Caché de predicciones** por municipio en `AemetClient.getDailyForecast()` con TTL de 1 h. Cachea los datos de AEMET (sin convertir), así que sirve para ambas unidades. N usuarios consultando el mismo municipio = 1 llamada por hora, no N.
- **Coalescing** (`sync = true`): peticiones concurrentes al mismo municipio con la caché vacía generan una sola llamada a AEMET.
- Municipios cacheados 24 h y precargados (pregunta 1).
- Gestión del `429` de AEMET como error controlado para el usuario.

Siguientes pasos, según crezca:
- **Caché compartida (Redis)** para que todas las instancias del backend compartan resultados.
- **Serve-stale-on-error + circuit breaker** (Resilience4j): si AEMET limita o falla, devolver el último dato cacheado en lugar de error.
- **Limitar nuestras propias llamadas** (rate limiter/bulkhead) y reintentos con *backoff* respetando `429`/`Retry-After`.
- **Precalentar** en segundo plano los municipios más consultados antes de que caduque su entrada.
- `Cache-Control` en nuestra API y/o un CDN delante, y *rate limiting* por cliente.
- Contactar con AEMET para explicar el caso de uso y pedir una cuota adecuada.

El impacto para el usuario es mínimo: los datos tienen como mucho 1 h de antigüedad, y la previsión de AEMET no se actualiza con más frecuencia.

## CI

`.github/workflows/ci.yml` ejecuta en cada push a `main` y en cada pull request: tests y build del backend (`mvn verify`, Java 21) y tests y build del frontend (Node 22). Los tests no llaman a AEMET, así que no hace falta configurar la API key en GitHub.
