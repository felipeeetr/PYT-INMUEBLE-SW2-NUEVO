package inmuebles.dominio.excepciones;

/** Indica que una operación incumple una regla funcional del dominio. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }
    public ReglaNegocioException(String mensaje, Throwable causa) { super(mensaje, causa); }
}