package inmuebles.aplicacion.casos_uso;

import inmuebles.aplicacion.puertos.entrada.ListarPropietarios;
import inmuebles.aplicacion.puertos.salida.RepositorioPropietarios;
import inmuebles.dominio.modelo.Propietario;

import java.util.List;

/** Expone a la aplicación la lista de propietarios disponibles en persistencia. */
public class ListarPropietariosCasoUso implements ListarPropietarios {
    private final RepositorioPropietarios propietarios;

    public ListarPropietariosCasoUso(RepositorioPropietarios propietarios) { this.propietarios = propietarios; }

    @Override
    public List<Propietario> ejecutar() { return propietarios.listar(); }
}