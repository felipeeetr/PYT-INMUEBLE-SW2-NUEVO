package inmuebles.aplicacion.puertos.salida;

import inmuebles.dominio.modelo.Inmueble;

import java.util.List;
import java.util.Optional;

/** Contrato de persistencia para consultar y modificar inmuebles sin acoplar la aplicación a JDBC. */
public interface RepositorioInmuebles {
    /** Persiste un inmueble nuevo con sus datos específicos.
     * @param inmueble inmueble que se guardará
     */
    void guardar(Inmueble inmueble);
    /** @return todos los inmuebles con su subtipo y propietario */
    List<Inmueble> buscarTodos();
    /** Busca un inmueble por su CHIP.
     * @param chip identificador del inmueble
     * @return el inmueble que coincide con el CHIP, si existe
     */
    Optional<Inmueble> buscarPorChip(String chip);
    /** Reemplaza los datos comunes y específicos de forma atómica.
     * @param inmueble versión actualizada del inmueble
     */
    void actualizar(Inmueble inmueble);
    /** Elimina un inmueble por CHIP y sus datos dependientes.
     * @param chip identificador del inmueble que se eliminará
     */
    void eliminar(String chip);
}