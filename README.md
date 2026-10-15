# Gestión de inmuebles

Aplicación de escritorio JavaFX para registrar apartamentos y casas, con persistencia JDBC sobre H2 en archivo. Está implementada con arquitectura hexagonal y no utiliza ORM ni Spring.

## Requisitos y ejecución

- JDK 21 o superior y Maven 3.9 o superior.
- Ejecutar pruebas: `mvn test`.
- Compilar el proyecto: `mvn package`.
- Ejecutar la aplicación con JavaFX: `mvn javafx:run`.

La primera ejecución crea `./data/inmuebles.mv.db`, inicializa el esquema y agrega tres propietarios predeterminados. El archivo permanece entre ejecuciones y no se elimina ni recrea. La ruta puede cambiarse con `INMUEBLES_DB_URL`; usuario y contraseña se configuran con `INMUEBLES_DB_USER` e `INMUEBLES_DB_PASSWORD`. Ejemplo:

```bash
INMUEBLES_DB_URL='jdbc:h2:file:./data/inmuebles;DB_CLOSE_ON_EXIT=FALSE' mvn javafx:run
```

## Organización

```text
src/
├── main/
│   ├── java/inmuebles/
│   │   ├── dominio/modelo/                 # Entidades y enumeraciones
│   │   ├── dominio/excepciones/            # Errores de negocio y de existencia
│   │   ├── aplicacion/puertos/entrada/     # Una interfaz por caso de uso
│   │   ├── aplicacion/puertos/salida/      # Contratos de repositorio
│   │   ├── aplicacion/casos_uso/           # Orquestación de reglas y puertos
│   │   ├── aplicacion/dto/                 # Transporte de datos de inmuebles
│   │   ├── adaptadores/entrada/javafx/     # Vistas, diálogo y controlador UI
│   │   ├── adaptadores/salida/persistencia/# Proveedor, inicializador y JDBC
│   │   └── configuracion/                  # Inyección manual de dependencias
│   └── resources/esquema.sql               # Esquema H2 idempotente
└── test/java/inmuebles/
	├── aplicacion/                         # Pruebas unitarias de casos de uso
	└── adaptadores/salida/persistencia/    # Pruebas de integración H2
```

El dominio no conoce la infraestructura. Los casos de uso dependen de puertos de salida, los repositorios JDBC solo conocen el dominio y `ControladorInmuebles` conecta la vista con los puertos de entrada. `ProveedorConexion` centraliza las conexiones y abre una conexión nueva por operación. Las operaciones de escritura usan transacciones con rollback; al cambiar de tipo se reemplazan las filas específicas y el sótano en una misma transacción.

Los propietarios precargados son `10000001` (Camila Torres), `10000002` (Andres Gomez) y `10000003` (Valentina Rojas). La inicialización usa `CREATE TABLE IF NOT EXISTS` y `MERGE`, por lo que es repetible.

Las pruebas crean bases H2 en memoria independientes de los datos de desarrollo. Incluyen ambas clases de inmueble, sótanos opcionales, duplicados, validación de referencias y checks, cascadas, restricción de borrado de propietarios y rollback al fallar una conversión.
