package inmuebles.dominio.excepciones;

/** Indica que una operación solicitó un inmueble que no existe. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) { super(mensaje); }
}