package inmuebles.aplicacion.puertos.entrada;

/** Puerto de entrada para eliminar un inmueble por su CHIP. */
public interface EliminarInmueble {
    /** Elimina el inmueble asociado al CHIP indicado.
     * @param chip identificador del inmueble que se eliminará
     */
    void ejecutar(String chip);
}