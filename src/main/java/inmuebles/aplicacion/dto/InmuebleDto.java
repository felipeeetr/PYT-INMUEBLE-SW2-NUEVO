package inmuebles.aplicacion.dto;

import inmuebles.dominio.modelo.Apartamento;
import inmuebles.dominio.modelo.Casa;
import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.Inmueble;
import inmuebles.dominio.modelo.Propietario;
import inmuebles.dominio.modelo.Sotano;
import inmuebles.dominio.modelo.TipoUso;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de entrada y salida que desacoplan los formularios del modelo persistido. */
public record InmuebleDto(String chip, LocalDate fechaCompra, String direccion, EstadoInmueble estado,
                          Propietario propietario, String tipo, Integer numeroPiso, Integer cantidadPisos,
                          BigDecimal areaSotano, TipoUso tipoUso) {
    public Inmueble aDominio() {
        if (tipo == null) throw new IllegalArgumentException("El tipo de inmueble es obligatorio");
        return switch (tipo.trim().toUpperCase()) {
            case "APARTAMENTO" -> new Apartamento(chip, fechaCompra, direccion, estado, propietario,
                    requerido(numeroPiso, "El número de piso es obligatorio"));
            case "CASA" -> new Casa(chip, fechaCompra, direccion, estado, propietario,
                    requerido(cantidadPisos, "La cantidad de pisos es obligatoria"), crearSotano());
            default -> throw new IllegalArgumentException("Tipo de inmueble no válido: " + tipo);
        };
    }

    private Sotano crearSotano() {
        if (areaSotano == null && tipoUso == null) return null;
        if (areaSotano == null || tipoUso == null) throw new IllegalArgumentException("Complete el área y el uso del sótano");
        return new Sotano(areaSotano, tipoUso);
    }

    private static int requerido(Integer valor, String mensaje) {
        if (valor == null) throw new IllegalArgumentException(mensaje);
        return valor;
    }

    public static InmuebleDto desdeDominio(Inmueble inmueble) {
        if (inmueble instanceof Apartamento apartamento) {
            return new InmuebleDto(apartamento.getChip(), apartamento.getFechaCompra(), apartamento.getDireccion(),
                    apartamento.getEstado(), apartamento.getPropietario(), "APARTAMENTO", apartamento.getNumeroPiso(),
                    null, null, null);
        }
        Casa casa = (Casa) inmueble;
        Sotano sotano = casa.getSotano();
        return new InmuebleDto(casa.getChip(), casa.getFechaCompra(), casa.getDireccion(), casa.getEstado(),
                casa.getPropietario(), "CASA", null, casa.getCantidadPisos(),
                sotano == null ? null : sotano.areaM2(), sotano == null ? null : sotano.tipoUso());
    }
}