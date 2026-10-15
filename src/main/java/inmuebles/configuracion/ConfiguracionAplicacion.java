package inmuebles.configuracion;

import inmuebles.adaptadores.salida.persistencia.InicializadorBaseDatos;
import inmuebles.adaptadores.salida.persistencia.ProveedorConexion;
import inmuebles.adaptadores.salida.persistencia.RepositorioInmueblesJdbc;
import inmuebles.adaptadores.salida.persistencia.RepositorioPropietariosJdbc;
import inmuebles.aplicacion.casos_uso.ActualizarInmuebleCasoUso;
import inmuebles.aplicacion.casos_uso.ConsultarInmueblesCasoUso;
import inmuebles.aplicacion.casos_uso.CrearInmuebleCasoUso;
import inmuebles.aplicacion.casos_uso.EliminarInmuebleCasoUso;
import inmuebles.aplicacion.casos_uso.ListarPropietariosCasoUso;
import inmuebles.aplicacion.puertos.entrada.ActualizarInmueble;
import inmuebles.aplicacion.puertos.entrada.ConsultarInmuebles;
import inmuebles.aplicacion.puertos.entrada.CrearInmueble;
import inmuebles.aplicacion.puertos.entrada.EliminarInmueble;
import inmuebles.aplicacion.puertos.entrada.ListarPropietarios;

/** Inicializa la infraestructura y conecta manualmente puertos con adaptadores. */
public final class ConfiguracionAplicacion {
    private final CrearInmueble crearInmueble;
    private final ConsultarInmuebles consultarInmuebles;
    private final ActualizarInmueble actualizarInmueble;
    private final EliminarInmueble eliminarInmueble;
    private final ListarPropietarios listarPropietarios;

    public ConfiguracionAplicacion() {
        this(ProveedorConexion.obtenerInstancia());
    }

    public ConfiguracionAplicacion(ProveedorConexion conexiones) {
        new InicializadorBaseDatos(conexiones).inicializar();
        var inmuebles = new RepositorioInmueblesJdbc(conexiones);
        var propietarios = new RepositorioPropietariosJdbc(conexiones);
        crearInmueble = new CrearInmuebleCasoUso(inmuebles, propietarios);
        consultarInmuebles = new ConsultarInmueblesCasoUso(inmuebles);
        actualizarInmueble = new ActualizarInmuebleCasoUso(inmuebles, propietarios);
        eliminarInmueble = new EliminarInmuebleCasoUso(inmuebles);
        listarPropietarios = new ListarPropietariosCasoUso(propietarios);
    }

    public CrearInmueble crearInmueble() { return crearInmueble; }
    public ConsultarInmuebles consultarInmuebles() { return consultarInmuebles; }
    public ActualizarInmueble actualizarInmueble() { return actualizarInmueble; }
    public EliminarInmueble eliminarInmueble() { return eliminarInmueble; }
    public ListarPropietarios listarPropietarios() { return listarPropietarios; }
}