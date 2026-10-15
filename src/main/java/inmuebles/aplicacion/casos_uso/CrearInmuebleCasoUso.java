package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.puertos.entrada.CrearInmueble;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import inmuebles.dominio.excepciones.ReglaNegocioException;

/** Valida el propietario y registra un inmueble creado desde los datos de entrada. */
public class CrearInmuebleCasoUso implements CrearInmueble {
    private final RepositorioInmuebles inmuebles;
    private final RepositorioPropietarios propietarios;

    public CrearInmuebleCasoUso(RepositorioInmuebles inmuebles, RepositorioPropietarios propietarios) {
        this.inmuebles = inmuebles;
        this.propietarios = propietarios;
    }

    @Override
    public void ejecutar(InmuebleDto dto) {
        var inmueble = dto.aDominio();
        if (!propietarios.existe(inmueble.getPropietario().identificacion()))
            throw new ReglaNegocioException("El propietario seleccionado no existe");
        inmuebles.guardar(inmueble);
    }
}