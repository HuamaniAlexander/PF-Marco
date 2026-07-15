# Despliegue en Render

## 1. Subir cambios a GitHub

Desde la raíz del proyecto:

```powershell
git add .
git commit -m "Configurar despliegue en Render"
git push
```

## 2. Crear el servicio

1. Entra a Render.
2. Crea un nuevo Web Service desde tu repositorio de GitHub.
3. Render usará el `Dockerfile` de la raíz del proyecto.
4. Para pruebas puedes usar el plan Free.

## 3. Variables de entorno

Configura estas variables en Render:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://HOST:PUERTO/BASE_DE_DATOS
SPRING_DATASOURCE_USERNAME=TU_USUARIO
SPRING_DATASOURCE_PASSWORD=TU_PASSWORD
GEMINI_API_KEY=TU_API_KEY_DE_GEMINI
GEMINI_MODEL=gemini-3.5-flash
DECOLECTA_API_TOKEN=TU_TOKEN_DE_DECOLECTA
```

Render asigna automáticamente `PORT`, y la app ya lo usa con `server.port=${PORT:8080}`.

## 4. Notas

- No subas claves reales al repositorio.
- Tu base de datos MySQL debe aceptar conexiones externas desde Render.
- Si usas una BD externa, confirma host, puerto, usuario, contraseña y permisos.
- El primer deploy puede tardar porque Docker descarga Maven y compila el `.jar`.
