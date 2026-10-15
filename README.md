# Gestión de inmuebles

Aplicación de escritorio para registrar y administrar apartamentos y casas. Permite crear, consultar, editar y eliminar inmuebles, asociarlos con propietarios existentes y cambiar el tipo de inmueble conservando el CHIP.

## Tecnologías

- Java 21.
- JavaFX para la interfaz de escritorio.
- Maven para dependencias, compilación y ejecución.
- H2 en modo archivo para conservar datos entre ejecuciones.
- JDBC puro para la persistencia; no se utiliza ORM.
- JUnit 5 para pruebas unitarias y de persistencia.

## Requisitos e instalación

Instala un JDK 21 o superior y Maven 3.9 o superior. Desde la raíz del proyecto, descarga dependencias y compila con:

```bash
mvn clean install
```

## Base de datos

Al iniciar la aplicación, se crea automáticamente el esquema definido en `src/main/resources/esquema.sql` y se insertan los propietarios predeterminados de forma idempotente. La base de datos se guarda en `./data/inmuebles.mv.db`; no se elimina al iniciar.

Valores predeterminados: URL `jdbc:h2:file:./data/inmuebles;DB_CLOSE_ON_EXIT=FALSE`, usuario `sa` y contraseña vacía. Se pueden cambiar mediante las variables de entorno `INMUEBLES_DB_URL`, `INMUEBLES_DB_USER` e `INMUEBLES_DB_PASSWORD`.

En PowerShell, por ejemplo:

```powershell
$env:INMUEBLES_DB_URL = "jdbc:h2:file:./data/inmuebles;DB_CLOSE_ON_EXIT=FALSE"
$env:INMUEBLES_DB_USER = "sa"
$env:INMUEBLES_DB_PASSWORD = ""
```

La inicialización crea las tablas `PROPIETARIO`, `INMUEBLE`, `APARTAMENTO`, `CASA` y `SOTANO`. Los propietarios iniciales son Camila Torres (`10000001`), Andres Gomez (`10000002`) y Valentina Rojas (`10000003`).

## Ejecución y pruebas

Inicia la aplicación desde la raíz del proyecto:

```bash
mvn javafx:run
```

Ejecuta las pruebas automatizadas con:

```bash
mvn test
```

Las pruebas usan una base H2 en memoria, separada de la base de desarrollo.

## Operaciones disponibles

- **Crear:** registra una casa o apartamento y valida CHIP, datos obligatorios y propietario.
- **Consultar:** carga la tabla general de inmuebles.
- **Actualizar:** modifica los datos comunes o específicos. Permite cambiar entre casa y apartamento en una transacción.
- **Eliminar:** elimina el inmueble seleccionado y sus filas dependientes, sin eliminar al propietario.
- **Listar propietarios:** consulta propietarios existentes para seleccionarlos en el formulario. No incluye administración CRUD de propietarios.

En las casas, el sótano es opcional y admite área y tipo de uso. Los cambios de subtipo conservan el CHIP; al convertir una casa en apartamento, se elimina también su sótano.

## Estructura del proyecto

```text
src/
├── main/
│   ├── java/inmuebles/
│   │   ├── dominio/
│   │   │   ├── modelo/              # Entidades y enumeraciones
│   │   │   └── excepciones/         # Errores de dominio
│   │   ├── aplicacion/
│   │   │   ├── puertos/entrada/     # Contratos para los casos de uso
│   │   │   ├── puertos/salida/      # Contratos de persistencia
│   │   │   ├── casos_uso/           # Lógica de aplicación
│   │   │   └── dto/                 # Datos de entrada y salida
│   │   ├── adaptadores/
│   │   │   ├── entrada/javafx/      # Vistas y controlador de interfaz
│   │   │   └── salida/persistencia/ # JDBC, conexiones e inicialización
│   │   └── configuracion/           # Inyección manual de dependencias
│   └── resources/esquema.sql        # Esquema H2
└── test/java/inmuebles/
    ├── aplicacion/                  # Pruebas de casos de uso
    └── adaptadores/salida/persistencia/ # Pruebas de integración H2
```
