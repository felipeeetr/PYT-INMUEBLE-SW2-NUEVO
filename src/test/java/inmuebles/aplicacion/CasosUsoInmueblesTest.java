package inmuebles.aplicacion;

import inmuebles.aplicacion.casos_uso.ActualizarInmuebleCasoUso;
import inmuebles.aplicacion.casos_uso.ConsultarInmueblesCasoUso;
import inmuebles.aplicacion.casos_uso.CrearInmuebleCasoUso;
import inmuebles.aplicacion.casos_uso.EliminarInmuebleCasoUso;
import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import inmuebles.dominio.excepciones.RecursoNoEncontradoException;
import inmuebles.dominio.excepciones.ReglaNegocioException;
import inmuebles.dominio.modelo.Apartamento;
import inmuebles.dominio.modelo.Casa;
import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.Inmueble;
import inmuebles.dominio.modelo.Propietario;
import inmuebles.dominio.modelo.TipoUso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CasosUsoInmueblesTest {
    private static final Propietario PROPIETARIO = new Propietario("P-1", "Ana Perez", "ana@example.com");
    private RepositorioFalso inmuebles;
    private PropietariosFalsos propietarios;
    private CrearInmuebleCasoUso crear;
    private ConsultarInmueblesCasoUso consultar;
    private ActualizarInmuebleCasoUso actualizar;
    private EliminarInmuebleCasoUso eliminar;

    @BeforeEach
    void preparar() {
        inmuebles = new RepositorioFalso();
        propietarios = new PropietariosFalsos();
        propietarios.existing.add(PROPIETARIO.identificacion());
        crear = new CrearInmuebleCasoUso(inmuebles, propietarios);
        consultar = new ConsultarInmueblesCasoUso(inmuebles);
        actualizar = new ActualizarInmuebleCasoUso(inmuebles, propietarios);
        eliminar = new EliminarInmuebleCasoUso(inmuebles);
    }

    @Test
    void ejecutaCrudYConvierteEntreCasaYApartamento() {
        crear.ejecutar(dto("CHIP-1", "CASA", null, 2, new BigDecimal("30"), TipoUso.BODEGA));
        assertInstanceOf(Casa.class, inmuebles.buscarPorChip("CHIP-1").orElseThrow());
        assertEquals(1, consultar.ejecutar().size());

        actualizar.ejecutar(dto("CHIP-1", "APARTAMENTO", 4, null, null, null));
        assertInstanceOf(Apartamento.class, inmuebles.buscarPorChip("CHIP-1").orElseThrow());
        assertEquals(4, ((Apartamento) consultar.ejecutar().getFirst().aDominio()).getNumeroPiso());

        actualizar.ejecutar(dto("CHIP-1", "CASA", null, 3, null, null));
        Casa casa = assertInstanceOf(Casa.class, inmuebles.buscarPorChip("CHIP-1").orElseThrow());
        assertNull(casa.getSotano());
        eliminar.ejecutar("CHIP-1");
        assertTrue(consultar.ejecutar().isEmpty());
    }

    @Test
    void rechazaChipDuplicadoYPropietarioInexistente() {
        InmuebleDto apartamento = dto("CHIP-2", "APARTAMENTO", 1, null, null, null);
        crear.ejecutar(apartamento);
        assertThrows(ReglaNegocioException.class, () -> crear.ejecutar(apartamento));
        assertThrows(ReglaNegocioException.class, () -> crear.ejecutar(new InmuebleDto("CHIP-3", LocalDate.now(),
                "Calle 1", EstadoInmueble.DISPONIBLE, new Propietario("OTRO", "Luis", "luis@example.com"),
                "CASA", null, 1, null, null)));
    }

    @Test
    void rechazaDatosInvalidosYOperacionesSobreInmueblesInexistentes() {
        assertThrows(IllegalArgumentException.class,
                () -> crear.ejecutar(dto("CHIP-4", "APARTAMENTO", 0, null, null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> crear.ejecutar(dto("CHIP-4", "CASA", null, 0, null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> crear.ejecutar(dto("CHIP-4", "CASA", null, 1, BigDecimal.ZERO, TipoUso.BODEGA)));
        assertThrows(RecursoNoEncontradoException.class, () -> eliminar.ejecutar("AUSENTE"));
        assertThrows(RecursoNoEncontradoException.class,
                () -> actualizar.ejecutar(dto("AUSENTE", "CASA", null, 1, null, null)));
    }

    @Test
    void propagaFallosDelPuertoDeSalida() {
        inmuebles.fallarGuardar = true;
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> crear.ejecutar(dto("CHIP-5", "CASA", null, 1, null, null)));
        assertEquals("fallo del repositorio", error.getMessage());
    }

    private InmuebleDto dto(String chip, String tipo, Integer piso, Integer pisos, BigDecimal area, TipoUso uso) {
        return new InmuebleDto(chip, LocalDate.of(2022, 1, 10), "Calle 10", EstadoInmueble.DISPONIBLE,
                PROPIETARIO, tipo, piso, pisos, area, uso);
    }

    private static final class RepositorioFalso implements RepositorioInmuebles {
        private final Map<String, Inmueble> datos = new HashMap<>();
        private boolean fallarGuardar;

        @Override public void guardar(Inmueble inmueble) {
            if (fallarGuardar) throw new IllegalStateException("fallo del repositorio");
            if (datos.putIfAbsent(inmueble.getChip(), inmueble) != null) throw new ReglaNegocioException("CHIP duplicado");
        }
        @Override public List<Inmueble> buscarTodos() { return new ArrayList<>(datos.values()); }
        @Override public Optional<Inmueble> buscarPorChip(String chip) { return Optional.ofNullable(datos.get(chip)); }
        @Override public void actualizar(Inmueble inmueble) {
            if (!datos.containsKey(inmueble.getChip())) throw new RecursoNoEncontradoException("No existe");
            datos.put(inmueble.getChip(), inmueble);
        }
        @Override public void eliminar(String chip) {
            if (datos.remove(chip) == null) throw new RecursoNoEncontradoException("No existe");
        }
    }

    private static final class PropietariosFalsos implements RepositorioPropietarios {
        private final List<String> existing = new ArrayList<>();
        @Override public List<Propietario> listar() { return List.of(PROPIETARIO); }
        @Override public boolean existe(String identificacion) { return existing.contains(identificacion); }
    }
}