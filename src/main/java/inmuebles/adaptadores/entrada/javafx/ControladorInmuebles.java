package inmuebles.adaptadores.entrada.javafx;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.puertos.entrada.ActualizarInmueble;
import inmuebles.aplicacion.puertos.entrada.ConsultarInmuebles;
import inmuebles.aplicacion.puertos.entrada.CrearInmueble;
import inmuebles.aplicacion.puertos.entrada.EliminarInmueble;
import inmuebles.aplicacion.puertos.entrada.ListarPropietarios;
import inmuebles.dominio.modelo.Propietario;

import java.util.List;

/** Adapta las acciones de la vista JavaFX a los puertos de entrada de la aplicación. */
public final class ControladorInmuebles {
    private final CrearInmueble crear;
    private final ConsultarInmuebles consultar;
    private final ActualizarInmueble actualizar;
    private final EliminarInmueble eliminar;
    private final ListarPropietarios listarPropietarios;

    public ControladorInmuebles(CrearInmueble crear, ConsultarInmuebles consultar,
                                ActualizarInmueble actualizar, EliminarInmueble eliminar,
                                ListarPropietarios listarPropietarios) {
        this.crear = crear;
        this.consultar = consultar;
        this.actualizar = actualizar;
        this.eliminar = eliminar;
        this.listarPropietarios = listarPropietarios;
    }

    public List<InmuebleDto> consultarInmuebles() { return consultar.ejecutar(); }
    public List<Propietario> listarPropietarios() { return listarPropietarios.ejecutar(); }
    public void crearInmueble(InmuebleDto inmueble) { crear.ejecutar(inmueble); }
    public void actualizarInmueble(InmuebleDto inmueble) { actualizar.ejecutar(inmueble); }
    public void eliminarInmueble(String chip) { eliminar.ejecutar(chip); }
}