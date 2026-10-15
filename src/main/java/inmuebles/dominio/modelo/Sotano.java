package inmuebles.dominio.modelo;

import java.math.BigDecimal;
import java.util.Objects;

/** Características opcionales del sótano asociado a una casa. */
public record Sotano(BigDecimal areaM2, TipoUso tipoUso) {
    public Sotano {
        Objects.requireNonNull(areaM2, "El área del sótano es obligatoria");
        if (areaM2.signum() <= 0) throw new IllegalArgumentException("El área del sótano debe ser mayor que cero");
        Objects.requireNonNull(tipoUso, "El tipo de uso del sótano es obligatorio");
    }
}