package inmuebles.adaptadores.salida.persistencia;

/** Envuelve un fallo de infraestructura conservando su causa original. */
public class ErrorPersistenciaException extends RuntimeException {
    public ErrorPersistenciaException(String mensaje, Throwable causa) { super(mensaje, causa); }
}