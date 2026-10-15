package inmuebles.adaptadores.salida.persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Centraliza la configuración y entrega conexiones JDBC nuevas por operación. */
public final class ProveedorConexion {
    private static final ProveedorConexion INSTANCIA = new ProveedorConexion(
            System.getenv().getOrDefault("INMUEBLES_DB_URL", "jdbc:h2:file:./data/inmuebles;DB_CLOSE_ON_EXIT=FALSE"),
            System.getenv().getOrDefault("INMUEBLES_DB_USER", "sa"),
            System.getenv().getOrDefault("INMUEBLES_DB_PASSWORD", ""));

    private final String url;
    private final String usuario;
    private final String contrasena;

    private ProveedorConexion(String url, String usuario, String contrasena) {
        this.url = url;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public static ProveedorConexion obtenerInstancia() { return INSTANCIA; }

    public static ProveedorConexion paraPruebas(String url) {
        return new ProveedorConexion(url, "sa", "");
    }

    public Connection obtenerConexion() throws SQLException {
        if (url.startsWith("jdbc:h2:file:")) {
            try {
                Files.createDirectories(Path.of("data"));
            } catch (IOException e) {
                throw new SQLException("No se pudo crear el directorio de datos", e);
            }
        }
        return DriverManager.getConnection(url, usuario, contrasena);
    }
}