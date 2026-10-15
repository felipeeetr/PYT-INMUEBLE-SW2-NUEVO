package inmuebles.dominio.modelo;

import java.util.Objects;

/** Persona propietaria referenciada por los inmuebles; no administra su ciclo de vida. */
public record Propietario(String identificacion, String nombre, String correo) {
    public Propietario {
        identificacion = requerido(identificacion, "La identificación del propietario es obligatoria");
        nombre = requerido(nombre, "El nombre del propietario es obligatorio");
        correo = requerido(correo, "El correo del propietario es obligatorio");
    }

    private static String requerido(String valor, String mensaje) {
        Objects.requireNonNull(valor, mensaje);
        if (valor.isBlank()) throw new IllegalArgumentException(mensaje);
        return valor.trim();
    }
}