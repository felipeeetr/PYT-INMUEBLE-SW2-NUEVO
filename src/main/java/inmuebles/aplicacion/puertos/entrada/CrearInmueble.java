package inmuebles.aplicacion.puertos.entrada;

import inmuebles.aplicacion.dto.InmuebleDto;

/** Puerto de entrada para validar y registrar un inmueble nuevo. */
public interface CrearInmueble {
    /** Valida y registra el inmueble indicado.
     * @param inmueble datos completos del inmueble que se creará
     */
    void ejecutar(InmuebleDto inmueble);
}