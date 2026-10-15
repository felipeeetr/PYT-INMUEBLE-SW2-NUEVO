package inmuebles.adaptadores.salida.persistencia;

import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.dominio.excepciones.RecursoNoEncontradoException;
import inmuebles.dominio.excepciones.ReglaNegocioException;
import inmuebles.dominio.modelo.Apartamento;
import inmuebles.dominio.modelo.Casa;
import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.Inmueble;
import inmuebles.dominio.modelo.Propietario;
import inmuebles.dominio.modelo.Sotano;
import inmuebles.dominio.modelo.TipoUso;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Implementa el repositorio de inmuebles con JDBC y transacciones H2. */
public final class RepositorioInmueblesJdbc implements RepositorioInmuebles {
    private static final String CONSULTA_BASE = "SELECT i.chip, i.fecha_compra, i.direccion, i.estado, i.tipo, "
            + "p.identificacion AS propietario_id, p.nombre AS propietario_nombre, p.correo AS propietario_correo, "
            + "a.numero_piso, c.cantidad_pisos, s.area_m2, s.tipo_uso "
            + "FROM INMUEBLE i JOIN PROPIETARIO p ON p.identificacion = i.propietario_id "
            + "LEFT JOIN APARTAMENTO a ON a.chip = i.chip LEFT JOIN CASA c ON c.chip = i.chip "
            + "LEFT JOIN SOTANO s ON s.chip_casa = c.chip ";

    private final ProveedorConexion conexiones;

    public RepositorioInmueblesJdbc(ProveedorConexion conexiones) { this.conexiones = conexiones; }

    @Override
    public void guardar(Inmueble inmueble) {
        transaccion(conexion -> {
            insertarComun(conexion, inmueble);
            insertarEspecifico(conexion, inmueble);
            return null;
        });
    }

    @Override
    public List<Inmueble> buscarTodos() {
        try (var conexion = conexiones.obtenerConexion();
             var consulta = conexion.prepareStatement(CONSULTA_BASE + "ORDER BY i.chip");
             var resultados = consulta.executeQuery()) {
            List<Inmueble> inmuebles = new ArrayList<>();
            while (resultados.next()) inmuebles.add(mapear(resultados));
            return inmuebles;
        } catch (SQLException e) {
            throw new ErrorPersistenciaException("No se pudieron consultar los inmuebles", e);
        }
    }

    @Override
    public Optional<Inmueble> buscarPorChip(String chip) {
        try (var conexion = conexiones.obtenerConexion();
             var consulta = conexion.prepareStatement(CONSULTA_BASE + "WHERE i.chip = ?")) {
            consulta.setString(1, chip);
            try (var resultados = consulta.executeQuery()) {
                return resultados.next() ? Optional.of(mapear(resultados)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErrorPersistenciaException("No se pudo consultar el inmueble", e);
        }
    }

    @Override
    public void actualizar(Inmueble inmueble) {
        transaccion(conexion -> {
            String tipo = tipo(inmueble);
            try (var consulta = conexion.prepareStatement("UPDATE INMUEBLE SET fecha_compra=?, direccion=?, estado=?, tipo=?, propietario_id=? WHERE chip=?")) {
                consulta.setObject(1, inmueble.getFechaCompra());
                consulta.setString(2, inmueble.getDireccion());
                consulta.setString(3, inmueble.getEstado().name());
                consulta.setString(4, tipo);
                consulta.setString(5, inmueble.getPropietario().identificacion());
                consulta.setString(6, inmueble.getChip());
                if (consulta.executeUpdate() == 0) throw new RecursoNoEncontradoException("No existe el inmueble con CHIP " + inmueble.getChip());
            }
            try (var eliminarApartamento = conexion.prepareStatement("DELETE FROM APARTAMENTO WHERE chip=?");
                 var eliminarCasa = conexion.prepareStatement("DELETE FROM CASA WHERE chip=?")) {
                eliminarApartamento.setString(1, inmueble.getChip());
                eliminarApartamento.executeUpdate();
                eliminarCasa.setString(1, inmueble.getChip());
                eliminarCasa.executeUpdate();
            }
            insertarEspecifico(conexion, inmueble);
            return null;
        });
    }

    @Override
    public void eliminar(String chip) {
        transaccion(conexion -> {
            try (var consulta = conexion.prepareStatement("DELETE FROM INMUEBLE WHERE chip=?")) {
                consulta.setString(1, chip);
                if (consulta.executeUpdate() == 0) throw new RecursoNoEncontradoException("No existe el inmueble con CHIP " + chip);
            }
            return null;
        });
    }

    private void insertarComun(Connection conexion, Inmueble inmueble) throws SQLException {
        try (var consulta = conexion.prepareStatement("INSERT INTO INMUEBLE(chip, fecha_compra, direccion, estado, tipo, propietario_id) VALUES(?,?,?,?,?,?)")) {
            consulta.setString(1, inmueble.getChip());
            consulta.setObject(2, inmueble.getFechaCompra());
            consulta.setString(3, inmueble.getDireccion());
            consulta.setString(4, inmueble.getEstado().name());
            consulta.setString(5, tipo(inmueble));
            consulta.setString(6, inmueble.getPropietario().identificacion());
            consulta.executeUpdate();
        }
    }

    private void insertarEspecifico(Connection conexion, Inmueble inmueble) throws SQLException {
        if (inmueble instanceof Apartamento apartamento) {
            try (var consulta = conexion.prepareStatement("INSERT INTO APARTAMENTO(chip, numero_piso) VALUES(?,?)")) {
                consulta.setString(1, apartamento.getChip());
                consulta.setInt(2, apartamento.getNumeroPiso());
                consulta.executeUpdate();
            }
        } else if (inmueble instanceof Casa casa) {
            try (var consulta = conexion.prepareStatement("INSERT INTO CASA(chip, cantidad_pisos) VALUES(?,?)")) {
                consulta.setString(1, casa.getChip());
                consulta.setInt(2, casa.getCantidadPisos());
                consulta.executeUpdate();
            }
            if (casa.getSotano() != null) {
                try (var consulta = conexion.prepareStatement("INSERT INTO SOTANO(chip_casa, area_m2, tipo_uso) VALUES(?,?,?)")) {
                    consulta.setString(1, casa.getChip());
                    consulta.setBigDecimal(2, casa.getSotano().areaM2());
                    consulta.setString(3, casa.getSotano().tipoUso().name());
                    consulta.executeUpdate();
                }
            }
        }
    }

    private Inmueble mapear(java.sql.ResultSet fila) throws SQLException {
        var propietario = new Propietario(fila.getString("propietario_id"), fila.getString("propietario_nombre"), fila.getString("propietario_correo"));
        String chip = fila.getString("chip");
        var fecha = fila.getDate("fecha_compra").toLocalDate();
        var estado = EstadoInmueble.valueOf(fila.getString("estado"));
        String tipo = fila.getString("tipo");
        if ("APARTAMENTO".equals(tipo))
            return new Apartamento(chip, fecha, fila.getString("direccion"), estado, propietario, fila.getInt("numero_piso"));
        Sotano sotano = fila.getBigDecimal("area_m2") == null ? null
                : new Sotano(fila.getBigDecimal("area_m2"), TipoUso.valueOf(fila.getString("tipo_uso")));
        return new Casa(chip, fecha, fila.getString("direccion"), estado, propietario, fila.getInt("cantidad_pisos"), sotano);
    }

    private String tipo(Inmueble inmueble) {
        if (inmueble instanceof Apartamento) return "APARTAMENTO";
        if (inmueble instanceof Casa) return "CASA";
        throw new IllegalArgumentException("Tipo de inmueble no soportado");
    }

    private <T> T transaccion(Operacion<T> operacion) {
        try (var conexion = conexiones.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try { conexion.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                if (e instanceof SQLException sql) {
                    if ("23505".equals(sql.getSQLState())) throw new ReglaNegocioException("Ya existe un inmueble con ese CHIP", sql);
                    throw new ErrorPersistenciaException("Falló la operación transaccional de inmuebles", sql);
                }
                throw (RuntimeException) e;
            }
        } catch (SQLException e) {
            throw new ErrorPersistenciaException("No se pudo completar la operación de inmuebles", e);
        }
    }

    @FunctionalInterface
    private interface Operacion<T> { T ejecutar(Connection conexion) throws SQLException; }
}