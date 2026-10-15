package inmuebles.aplicacion.puertos.entrada;

import inmuebles.aplicacion.dto.InmuebleDto;

/** Puerto de entrada para actualizar los datos de un inmueble existente. */
public interface ActualizarInmueble {
    /** Valida y persiste los datos, incluidos los específicos del tipo nuevo.
     * @param inmueble datos completos del inmueble a actualizar
     */
    void ejecutar(InmuebleDto inmueble);
}