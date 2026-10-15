package inmuebles.aplicacion.puertos.salida;

import inmuebles.dominio.modelo.Propietario;

import java.util.List;

/** Contrato de lectura y validación de propietarios existentes. */
public interface RepositorioPropietarios {
    /** @return propietarios disponibles para asociarlos a un inmueble */
    List<Propietario> listar();
    /** Comprueba si existe un propietario con la identificación indicada.
     * @param identificacion identificación del propietario
     * @return true si existe
     */
    boolean existe(String identificacion);
}