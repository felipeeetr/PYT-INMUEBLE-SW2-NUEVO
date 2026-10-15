package inmuebles.adaptadores.salida.persistencia;

import inmuebles.dominio.excepciones.ReglaNegocioException;
import inmuebles.dominio.modelo.Apartamento;
import inmuebles.dominio.modelo.Casa;
import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.Propietario;
import inmuebles.dominio.modelo.Sotano;
import inmuebles.dominio.modelo.TipoUso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RepositorioInmueblesJdbcTest {
    private static final AtomicInteger CONTADOR = new AtomicInteger();
    private static final Propietario PROPIETARIO = new Propietario("P-100", "Maria Ruiz", "maria@example.com");
    private ProveedorConexion conexiones;
    private RepositorioInmueblesJdbc inmuebles;
    private RepositorioPropietariosJdbc propietarios;

    @BeforeEach
    void preparar() {
        conexiones = ProveedorConexion.paraPruebas("jdbc:h2:mem:inmuebles" + CONTADOR.incrementAndGet() + ";DB_CLOSE_DELAY=-1");
        new InicializadorBaseDatos(conexiones).inicializar();
        inmuebles = new RepositorioInmueblesJdbc(conexiones);
        propietarios = new RepositorioPropietariosJdbc(conexiones);
        try (var conexion = conexiones.obtenerConexion();
             var sentencia = conexion.prepareStatement("INSERT INTO PROPIETARIO(identificacion,nombre,correo) VALUES(?,?,?)")) {
            sentencia.setString(1, PROPIETARIO.identificacion());
            sentencia.setString(2, PROPIETARIO.nombre());
            sentencia.setString(3, PROPIETARIO.correo());
            sentencia.executeUpdate();
        } catch (SQLException e) { throw new AssertionError(e); }
    }

    @Test
    void guardaYConsultaApartamentoCasaConYSinSotano() {
        inmuebles.guardar(new Apartamento("APT-1", LocalDate.of(2021, 2, 3), "Calle A", EstadoInmueble.DISPONIBLE, PROPIETARIO, 5));
        inmuebles.guardar(new Casa("CAS-1", LocalDate.of(2020, 4, 5), "Calle B", EstadoInmueble.ARRENDADO,
                PROPIETARIO, 2, new Sotano(new BigDecimal("42.5"), TipoUso.PARQUEADERO)));
        inmuebles.guardar(new Casa("CAS-2", LocalDate.of(2019, 6, 7), "Calle C", EstadoInmueble.VENDIDO, PROPIETARIO, 1, null));

        assertEquals(3, inmuebles.buscarTodos().size());
        assertEquals(5, assertInstanceOf(Apartamento.class, inmuebles.buscarPorChip("APT-1").orElseThrow()).getNumeroPiso());
        Casa casaConSotano = assertInstanceOf(Casa.class, inmuebles.buscarPorChip("CAS-1").orElseThrow());
        assertEquals(0, new BigDecimal("42.5").compareTo(casaConSotano.getSotano().areaM2()));
        assertNull(assertInstanceOf(Casa.class, inmuebles.buscarPorChip("CAS-2").orElseThrow()).getSotano());
    }

    @Test
    void inicializacionEsIdempotenteYNoDuplicaPropietariosPredeterminados() {
        new InicializadorBaseDatos(conexiones).inicializar();
        new InicializadorBaseDatos(conexiones).inicializar();
        assertEquals(4, propietarios.listar().size());
    }

    @Test
    void aplicaClavesForaneasChecksYRestriccionDeBorradoDePropietario() throws SQLException {
        inmuebles.guardar(new Apartamento("DUP", LocalDate.now(), "Calle A", EstadoInmueble.DISPONIBLE, PROPIETARIO, 1));
        assertThrows(ReglaNegocioException.class, () -> inmuebles.guardar(
                new Apartamento("DUP", LocalDate.now(), "Calle A", EstadoInmueble.DISPONIBLE, PROPIETARIO, 1)));
        inmuebles.guardar(new Apartamento("REF", LocalDate.now(), "Calle A", EstadoInmueble.DISPONIBLE, PROPIETARIO, 1));
        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.createStatement()) {
            assertThrows(SQLException.class, () -> sentencia.executeUpdate("DELETE FROM PROPIETARIO WHERE identificacion='P-100'"));
            assertThrows(SQLException.class, () -> sentencia.executeUpdate("UPDATE APARTAMENTO SET numero_piso=0 WHERE chip='REF'"));
            assertThrows(SQLException.class, () -> sentencia.executeUpdate("UPDATE INMUEBLE SET estado='INVALIDO' WHERE chip='REF'"));
            assertThrows(SQLException.class, () -> sentencia.executeUpdate("INSERT INTO SOTANO(chip_casa,area_m2,tipo_uso) VALUES('REF',-1,'BODEGA')"));
            assertThrows(SQLException.class, () -> sentencia.executeUpdate("INSERT INTO INMUEBLE(chip,fecha_compra,direccion,estado,tipo,propietario_id) VALUES('FK',CURRENT_DATE,'X','DISPONIBLE','CASA','NO-EXISTE')"));
        }
    }

    @Test
    void borradoEnCascadaEliminaLasFilasEspecificasPeroNoElPropietario() throws SQLException {
        inmuebles.guardar(new Casa("BORRAR", LocalDate.now(), "Calle X", EstadoInmueble.DISPONIBLE,
                PROPIETARIO, 2, new Sotano(new BigDecimal("20"), TipoUso.VIVIENDA)));
        inmuebles.eliminar("BORRAR");
        try (var conexion = conexiones.obtenerConexion(); var consulta = conexion.createStatement()) {
            assertEquals(0, contar(consulta, "SELECT COUNT(*) FROM CASA WHERE chip='BORRAR'"));
            assertEquals(0, contar(consulta, "SELECT COUNT(*) FROM SOTANO WHERE chip_casa='BORRAR'"));
            assertEquals(1, contar(consulta, "SELECT COUNT(*) FROM PROPIETARIO WHERE identificacion='P-100'"));
        }
    }

    @Test
    void rollbackRevierteCambioDeTipoSiFallaLaInsercionDelNuevoSubtipo() throws SQLException {
        inmuebles.guardar(new Casa("ROLLBACK", LocalDate.now(), "Calle X", EstadoInmueble.DISPONIBLE,
                PROPIETARIO, 2, new Sotano(new BigDecimal("20"), TipoUso.BODEGA)));
        try (var conexion = conexiones.obtenerConexion(); var sentencia = conexion.createStatement()) {
            sentencia.execute("ALTER TABLE APARTAMENTO ADD CONSTRAINT BLOQUEAR_ROLLBACK CHECK (chip <> 'ROLLBACK')");
        }

        assertThrows(ErrorPersistenciaException.class, () -> inmuebles.actualizar(new Apartamento(
                "ROLLBACK", LocalDate.now(), "Nueva dirección", EstadoInmueble.ARRENDADO, PROPIETARIO, 3)));
        Casa casaRestaurada = assertInstanceOf(Casa.class, inmuebles.buscarPorChip("ROLLBACK").orElseThrow());
        assertEquals("Calle X", casaRestaurada.getDireccion());
        assertNotNull(casaRestaurada.getSotano());
    }

    private int contar(java.sql.Statement sentencia, String sql) throws SQLException {
        try (var resultados = sentencia.executeQuery(sql)) { resultados.next(); return resultados.getInt(1); }
    }
}