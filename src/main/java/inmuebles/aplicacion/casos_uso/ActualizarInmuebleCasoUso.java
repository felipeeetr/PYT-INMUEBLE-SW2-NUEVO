package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.puertos.entrada.ActualizarInmueble;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import inmuebles.dominio.excepciones.RecursoNoEncontradoException;
import inmuebles.dominio.excepciones.ReglaNegocioException;

/** Comprueba existencia y propietario antes de delegar la actualización al repositorio. */
public class ActualizarInmuebleCasoUso implements ActualizarInmueble {
    private final RepositorioInmuebles inmuebles;
    private final RepositorioPropietarios propietarios;

    public ActualizarInmuebleCasoUso(RepositorioInmuebles inmuebles, RepositorioPropietarios propietarios) {
        this.inmuebles = inmuebles;
        this.propietarios = propietarios;
    }

    @Override
    public void ejecutar(InmuebleDto dto) {
        if (inmuebles.buscarPorChip(dto.chip()).isEmpty())
            throw new RecursoNoEncontradoException("No existe el inmueble con CHIP " + dto.chip());
        var inmueble = dto.aDominio();
        if (!propietarios.existe(inmueble.getPropietario().identificacion()))
            throw new ReglaNegocioException("El propietario seleccionado no existe");
        inmuebles.actualizar(inmueble);
    }
}