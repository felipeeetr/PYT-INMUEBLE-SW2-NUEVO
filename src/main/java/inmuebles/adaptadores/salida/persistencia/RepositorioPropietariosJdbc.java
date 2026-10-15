package inmuebles.adaptadores.salida.persistencia;

import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import inmuebles.dominio.modelo.Propietario;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Implementa con JDBC la consulta y validación de propietarios. */
public final class RepositorioPropietariosJdbc implements RepositorioPropietarios {
    private final ProveedorConexion conexiones;

    public RepositorioPropietariosJdbc(ProveedorConexion conexiones) { this.conexiones = conexiones; }

    @Override
    public List<Propietario> listar() {
        String sql = "SELECT identificacion, nombre, correo FROM PROPIETARIO ORDER BY nombre";
        try (var conexion = conexiones.obtenerConexion();
             var consulta = conexion.prepareStatement(sql);
             var resultados = consulta.executeQuery()) {
            List<Propietario> propietarios = new ArrayList<>();
            while (resultados.next()) propietarios.add(new Propietario(
                    resultados.getString("identificacion"), resultados.getString("nombre"), resultados.getString("correo")));
            return propietarios;
        } catch (SQLException e) {
            throw new ErrorPersistenciaException("No se pudieron consultar los propietarios", e);
        }
    }

    @Override
    public boolean existe(String identificacion) {
        try (var conexion = conexiones.obtenerConexion();
             var consulta = conexion.prepareStatement("SELECT 1 FROM PROPIETARIO WHERE identificacion = ?")) {
            consulta.setString(1, identificacion);
            try (var resultados = consulta.executeQuery()) { return resultados.next(); }
        } catch (SQLException e) {
            throw new ErrorPersistenciaException("No se pudo validar el propietario", e);
        }
    }
}