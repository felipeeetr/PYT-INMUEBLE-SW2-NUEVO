package inmuebles.adaptadores.salida.persistencia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/** Crea el esquema si hace falta y carga propietarios predeterminados sin duplicarlos. */
public final class InicializadorBaseDatos {
    private static final List<String[]> PROPIETARIOS_PREDETERMINADOS = List.of(
            new String[]{"10000001", "Camila Torres", "camila.torres@example.com"},
            new String[]{"10000002", "Andres Gomez", "andres.gomez@example.com"},
            new String[]{"10000003", "Valentina Rojas", "valentina.rojas@example.com"});

    private final ProveedorConexion conexiones;

    public InicializadorBaseDatos(ProveedorConexion conexiones) { this.conexiones = conexiones; }

    public void inicializar() {
        try (var conexion = conexiones.obtenerConexion()) {
            var recurso = getClass().getResourceAsStream("/esquema.sql");
            if (recurso == null) throw new IllegalStateException("No se encontró esquema.sql");
            try (var lector = new BufferedReader(new InputStreamReader(recurso, StandardCharsets.UTF_8));
                 var sentencia = conexion.createStatement()) {
                StringBuilder sql = new StringBuilder();
                for (String linea; (linea = lector.readLine()) != null;) {
                    sql.append(linea).append('\n');
                    if (linea.trim().endsWith(";")) {
                        sentencia.execute(sql.toString());
                        sql.setLength(0);
                    }
                }
                if (!sql.toString().isBlank()) sentencia.execute(sql.toString());
            }
            try (var sentencia = conexion.prepareStatement(
                    "MERGE INTO PROPIETARIO (identificacion, nombre, correo) KEY(identificacion) VALUES (?, ?, ?)")) {
                for (String[] propietario : PROPIETARIOS_PREDETERMINADOS) {
                    sentencia.setString(1, propietario[0]);
                    sentencia.setString(2, propietario[1]);
                    sentencia.setString(3, propietario[2]);
                    sentencia.addBatch();
                }
                sentencia.executeBatch();
            }
        } catch (SQLException e) {
            throw new ErrorPersistenciaException("No se pudo inicializar la base de datos", e);
        } catch (java.io.IOException e) {
            throw new ErrorPersistenciaException("No se pudo leer el esquema de la base de datos", e);
        }
    }
}