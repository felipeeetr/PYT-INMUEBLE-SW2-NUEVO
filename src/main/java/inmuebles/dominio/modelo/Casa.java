package inmuebles.dominio.modelo;

import java.time.LocalDate;

/** Inmueble residencial con uno o más pisos y un sótano opcional. */
public final class Casa extends Inmueble {
    private final int cantidadPisos;
    private final Sotano sotano;

    public Casa(String chip, LocalDate fechaCompra, String direccion, EstadoInmueble estado,
                Propietario propietario, int cantidadPisos, Sotano sotano) {
        super(chip, fechaCompra, direccion, estado, propietario);
        if (cantidadPisos <= 0) throw new IllegalArgumentException("La cantidad de pisos debe ser mayor que cero");
        this.cantidadPisos = cantidadPisos;
        this.sotano = sotano;
    }

    public int getCantidadPisos() { return cantidadPisos; }
    public Sotano getSotano() { return sotano; }
}