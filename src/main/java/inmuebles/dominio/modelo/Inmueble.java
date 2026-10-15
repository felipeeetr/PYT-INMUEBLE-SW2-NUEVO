package inmuebles.dominio.modelo;

import java.time.LocalDate;
import java.util.Objects;

/** Datos y reglas comunes a los tipos de inmueble soportados por el dominio. */
public abstract class Inmueble {
    private final String chip;
    private final LocalDate fechaCompra;
    private final String direccion;
    private final EstadoInmueble estado;
    private final Propietario propietario;

    protected Inmueble(String chip, LocalDate fechaCompra, String direccion, EstadoInmueble estado, Propietario propietario) {
        this.chip = requerido(chip, "El CHIP es obligatorio");
        this.fechaCompra = Objects.requireNonNull(fechaCompra, "La fecha de compra es obligatoria");
        this.direccion = requerido(direccion, "La dirección es obligatoria");
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        this.propietario = Objects.requireNonNull(propietario, "El propietario es obligatorio");
    }

    private static String requerido(String valor, String mensaje) {
        Objects.requireNonNull(valor, mensaje);
        if (valor.isBlank()) throw new IllegalArgumentException(mensaje);
        return valor.trim();
    }

    public String getChip() { return chip; }
    public LocalDate getFechaCompra() { return fechaCompra; }
    public String getDireccion() { return direccion; }
    public EstadoInmueble getEstado() { return estado; }
    public Propietario getPropietario() { return propietario; }
}