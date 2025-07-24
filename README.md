# Bot React - Dockerizado

Este proyecto es un bot que utiliza NstBrowser para automatizar interacciones en redes sociales. Ha sido dockerizado para facilitar su uso y distribución.

## ¿Qué hace este proyecto?

- Automatiza interacciones en redes sociales usando NstBrowser
- Permite usar múltiples perfiles de navegador
- Está diseñado para ser fácilmente modificable y escalable

## Estructura del Proyecto

```
Bot_React/
├── src/                # Código fuente del proyecto
├── target/             # JAR y dependencias compiladas
├── docker/            # Archivos de Docker
├── pom.xml           # Dependencias del proyecto
├── docker-compose.yml # Configuración de Docker Compose
└── .env              # Variables de entorno
```

## Requisitos

- Docker y Docker Compose instalados
- NstBrowser instalado localmente
- Java 8 o superior

## Instalación

1. Clona el repositorio:
```bash
git clone [URL_DEL_REPO]
cd Bot_React
```

2. Configura tu API Key:
```bash
echo "API_KEY=tu_api_key" > .env
```

3. Inicia los servicios:
```bash
docker-compose up --build
```

## Uso del Bot

1. Una vez que los servicios estén corriendo:
   - NstBrowser estará disponible en `http://localhost:8888`
   - El bot se ejecutará automáticamente y mostrará los perfiles disponibles

2. Para ver los logs del bot:
```bash
docker-compose logs -f bot
```

## Modificando el Proyecto

### 1. Modificar el Código

1. Realiza tus cambios en los archivos Java en la carpeta `src/`
2. Para aplicar los cambios:
```bash
docker-compose down
docker-compose up --build
```

### 2. Agregar Nuevas Dependencias

1. Modifica el archivo `pom.xml` para agregar nuevas dependencias
2. Para aplicar los cambios:
```bash
docker-compose down
docker-compose up --build
```

## Variables de Entorno

- `API_KEY`: Tu clave de API para NstBrowser
- `NSTBROWSER_URL`: URL donde está corriendo NstBrowser (por defecto: http://localhost:8888)

## Estructura de Carpetas

- `profiles/`: Esta carpeta se monta como volumen para persistir los perfiles de NstBrowser
- `src/`: Código fuente del proyecto
- `target/`: Directorio donde se compila el código
- `build/`: Directorio de construcción temporal

## Mejores Prácticas

1. **Mantén limpio el código**: Usa comentarios y sigue convenciones de nombres
2. **Documenta los cambios**: Agrega comentarios en el código cuando hagas modificaciones importantes
3. **Prueba antes de subir**: Siempre prueba tus cambios localmente antes de hacer un commit
4. **Usa ramas**: Crea ramas para nuevas características o correcciones

## Comandos Útiles

- Construir y ejecutar:
```bash
docker-compose up --build
```

- Detener servicios:
```bash
docker-compose down
```

- Ver logs:
```bash
docker-compose logs -f
```

- Ver logs específicos del bot:
```bash
docker-compose logs -f bot
```

## Resolución de Problemas

1. Si hay problemas de conexión con NstBrowser:
   - Verifica que NstBrowser está corriendo
   - Verifica que el puerto 8888 está abierto
   - Verifica que la API Key es correcta

2. Si hay problemas de compilación:
   - Verifica que todas las dependencias están en el pom.xml
   - Limpia y reconstruye el proyecto:
```bash
docker-compose down
docker-compose up --build
```

## Contribución

1. Haz un fork del repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

[MIT](LICENSE)
