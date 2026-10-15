package inmuebles.adaptadores.entrada.javafx;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.configuracion.ConfiguracionAplicacion;
import inmuebles.dominio.modelo.Apartamento;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

/** Arranca la ventana JavaFX y presenta la tabla principal de inmuebles. */
public final class AplicacionInmuebles extends Application {
    private final TableView<InmuebleDto> tabla = new TableView<>();
    private final Label estado = new Label("Listo");
    private ControladorInmuebles controlador;

    @Override
    public void start(Stage escenario) {
        try {
            var configuracion = new ConfiguracionAplicacion();
            controlador = new ControladorInmuebles(configuracion.crearInmueble(), configuracion.consultarInmuebles(),
                    configuracion.actualizarInmueble(), configuracion.eliminarInmueble(), configuracion.listarPropietarios());
            escenario.setTitle("Gestión de inmuebles");
            escenario.setMinWidth(900);
            escenario.setMinHeight(560);
            escenario.setScene(new Scene(construirVista(), 1100, 680));
            recargar();
            escenario.show();
        } catch (RuntimeException error) {
            mostrarError("No se pudo iniciar la aplicación", error);
            throw error;
        }
    }

    private BorderPane construirVista() {
        BorderPane raiz = new BorderPane();
        raiz.setPadding(new Insets(20));
        raiz.setStyle("-fx-background-color: #f4f6f3;");
        Label titulo = new Label("Inmuebles registrados");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #183c35;");
        Button nuevo = new Button("Nuevo inmueble");
        Button editar = new Button("Editar");
        Button eliminar = new Button("Eliminar");
        Button recargar = new Button("Recargar tabla");
        nuevo.setOnAction(evento -> crear());
        editar.setOnAction(evento -> editar());
        eliminar.setOnAction(evento -> eliminar());
        recargar.setOnAction(evento -> recargar());
        editar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        eliminar.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        HBox acciones = new HBox(8, nuevo, editar, eliminar, recargar);
        HBox encabezado = new HBox(24, titulo, acciones);
        encabezado.setPadding(new Insets(0, 0, 16, 0));
        raiz.setTop(encabezado);
        configurarTabla();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        raiz.setCenter(tabla);
        estado.setStyle("-fx-text-fill: #52635e;");
        BorderPane.setMargin(estado, new Insets(12, 0, 0, 0));
        raiz.setBottom(estado);
        return raiz;
    }

    private void configurarTabla() {
        tabla.setPlaceholder(new Label("No hay inmuebles registrados"));
        tabla.getColumns().setAll(
                columna("CHIP", InmuebleDto::chip),
                columna("Tipo", InmuebleDto::tipo),
                columna("Dirección", InmuebleDto::direccion),
                columna("Fecha de compra", inmueble -> inmueble.fechaCompra().format(DateTimeFormatter.ISO_DATE)),
                columna("Estado", inmueble -> inmueble.estado().name()),
                columna("Propietario", inmueble -> inmueble.propietario().nombre()),
                columna("Detalle", inmueble -> inmueble.numeroPiso() == null
                        ? "" + inmueble.cantidadPisos() + " piso(s)" + (inmueble.areaSotano() == null ? "" : " + sótano")
                        : "Piso " + inmueble.numeroPiso()));
    }

    private TableColumn<InmuebleDto, String> columna(String titulo, java.util.function.Function<InmuebleDto, String> valor) {
        TableColumn<InmuebleDto, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(dato -> new javafx.beans.property.SimpleStringProperty(valor.apply(dato.getValue())));
        return columna;
    }

    private void recargar() {
        ejecutar("Tabla actualizada", () -> tabla.setItems(FXCollections.observableArrayList(controlador.consultarInmuebles())));
    }

    private void crear() {
        InmuebleDto datos = new FormularioInmuebleDialogo(controlador, null).mostrar();
        if (datos != null) ejecutar("Inmueble creado correctamente", () -> {
            controlador.crearInmueble(datos);
            recargar();
        });
    }

    private void editar() {
        InmuebleDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        InmuebleDto datos = new FormularioInmuebleDialogo(controlador, seleccionado).mostrar();
        if (datos != null) ejecutar("Inmueble actualizado correctamente", () -> {
            controlador.actualizarInmueble(datos);
            recargar();
        });
    }

    private void eliminar() {
        InmuebleDto seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar el inmueble " + seleccionado.chip() + "?", ButtonType.CANCEL, ButtonType.OK);
        confirmacion.setHeaderText("Confirmar eliminación");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK)
            ejecutar("Inmueble eliminado correctamente", () -> {
                controlador.eliminarInmueble(seleccionado.chip());
                recargar();
            });
    }

    private void ejecutar(String exito, Runnable accion) {
        try {
            accion.run();
            estado.setText(exito);
        } catch (RuntimeException error) {
            mostrarError("No se pudo completar la operación", error);
        }
    }

    private void mostrarError(String titulo, Throwable error) {
        String mensaje = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Gestión de inmuebles");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
        estado.setText("Error: " + mensaje);
    }

    public static void main(String[] args) { launch(args); }
}