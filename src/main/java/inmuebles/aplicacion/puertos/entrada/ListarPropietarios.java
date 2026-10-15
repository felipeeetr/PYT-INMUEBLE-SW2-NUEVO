package inmuebles.aplicacion.puertos.entrada;

import inmuebles.dominio.modelo.Propietario;

import java.util.List;

/** Puerto de entrada para cargar propietarios disponibles en los formularios. */
public interface ListarPropietarios {
    /** Consulta los propietarios disponibles para asociarlos a un inmueble.
     * @return propietarios registrados y seleccionables
     */
    List<Propietario> ejecutar();
}