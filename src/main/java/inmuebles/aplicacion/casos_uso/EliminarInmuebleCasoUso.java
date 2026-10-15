package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.puertos.entrada.EliminarInmueble;
import inmuebles.aplicacion.puertos.salida.RepositorioInmuebles;
import inmuebles.dominio.excepciones.RecursoNoEncontradoException;

/** Verifica que el inmueble exista antes de solicitar su eliminación. */
public class EliminarInmuebleCasoUso implements EliminarInmueble {
    private final RepositorioInmuebles inmuebles;

    public EliminarInmuebleCasoUso(RepositorioInmuebles inmuebles) { this.inmuebles = inmuebles; }

    @Override
    public void ejecutar(String chip) {
        if (inmuebles.buscarPorChip(chip).isEmpty())
            throw new RecursoNoEncontradoException("No existe el inmueble con CHIP " + chip);
        inmuebles.eliminar(chip);
    }
}