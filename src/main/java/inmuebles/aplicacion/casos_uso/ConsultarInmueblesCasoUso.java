package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.aplicacion.puertos.entrada.ConsultarInmuebles;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;

import java.util.List;

/** Obtiene los inmuebles del repositorio y los convierte a DTO para la interfaz. */
public class ConsultarInmueblesCasoUso implements ConsultarInmuebles {
    private final RepositorioInmuebles inmuebles;

    public ConsultarInmueblesCasoUso(RepositorioInmuebles inmuebles) { this.inmuebles = inmuebles; }

    @Override
    public List<InmuebleDto> ejecutar() {
        return inmuebles.buscarTodos().stream().map(InmuebleDto::desdeDominio).toList();
    }
}