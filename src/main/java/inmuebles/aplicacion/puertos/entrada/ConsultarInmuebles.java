package inmuebles.aplicacion.puertos.entrada;

import inmuebles.aplicacion.dto.InmuebleDto;

import java.util.List;

/** Puerto de entrada para consultar los inmuebles registrados. */
public interface ConsultarInmuebles {
    /** Consulta los inmuebles registrados y los devuelve como datos aptos para la vista.
     * @return inmuebles registrados
     */
    List<InmuebleDto> ejecutar();
}