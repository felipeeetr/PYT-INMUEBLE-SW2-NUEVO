package inmuebles.adaptadores.entrada.javafx;

import inmuebles.aplicacion.dto.InmuebleDto;
import inmuebles.dominio.modelo.EstadoInmueble;
import inmuebles.dominio.modelo.Propietario;
import inmuebles.dominio.modelo.TipoUso;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

/** Construye y valida el diálogo JavaFX para crear o editar cualquier tipo de inmueble. */
public final class FormularioInmuebleDialogo {
    private final ControladorInmuebles controlador;
    private final InmuebleDto original;

    private final TextField chip = new TextField();
    private final DatePicker fecha = new DatePicker();
    private final TextField direccion = new TextField();
    private final ComboBox<EstadoInmueble> estado = new ComboBox<>();
    private final ComboBox<Propietario> propietario = new ComboBox<>();
    private final ComboBox<String> tipo = new ComboBox<>();
    private final TextField numeroPiso = new TextField();
    private final TextField cantidadPisos = new TextField();
    private final CheckBox conSotano = new CheckBox("La casa tiene sótano");
    private final TextField areaSotano = new TextField();
    private final ComboBox<TipoUso> tipoUso = new ComboBox<>();
    private final GridPane formulario = new GridPane();
    private final GridPane camposApartamento = new GridPane();
    private final GridPane camposCasa = new GridPane();
    private final GridPane camposSotano = new GridPane();
    private final Label mensaje = new Label();

    public FormularioInmuebleDialogo(ControladorInmuebles controlador, InmuebleDto original) {
        this.controlador = controlador;
        this.original = original;
    }

    public InmuebleDto mostrar() {
        Dialog<InmuebleDto> dialogo = new Dialog<>();
        dialogo.setTitle(original == null ? "Nuevo inmueble" : "Actualizar inmueble");
        dialogo.setHeaderText("Información general y características del inmueble");
        ButtonType guardar = new ButtonType(original == null ? "Crear" : "Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        construirFormulario();
        dialogo.getDialogPane().setContent(formulario);
        dialogo.getDialogPane().setMinWidth(510);
        AtomicReference<InmuebleDto> resultado = new AtomicReference<>();
        Node botonGuardar = dialogo.getDialogPane().lookupButton(guardar);
        botonGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                resultado.set(construirDto());
                mensaje.setText("");
            } catch (RuntimeException error) {
                mensaje.setText(error.getMessage());
                evento.consume();
            }
        });
        dialogo.setResultConverter(boton -> boton == guardar ? resultado.get() : null);
        return dialogo.showAndWait().orElse(null);
    }

    private void construirFormulario() {
        formulario.setHgap(12);
        formulario.setVgap(10);
        formulario.setPadding(new Insets(16));
        int fila = 0;
        agregar(formulario, fila++, "CHIP", chip);
        agregar(formulario, fila++, "Fecha de compra", fecha);
        agregar(formulario, fila++, "Dirección", direccion);
        agregar(formulario, fila++, "Estado", estado);
        agregar(formulario, fila++, "Propietario", propietario);
        agregar(formulario, fila++, "Tipo", tipo);

        camposApartamento.setHgap(12);
        camposApartamento.setVgap(8);
        agregar(camposApartamento, 0, "Número de piso", numeroPiso);
        camposCasa.setHgap(12);
        camposCasa.setVgap(8);
        agregar(camposCasa, 0, "Cantidad de pisos", cantidadPisos);
        camposCasa.add(conSotano, 1, 1);
        camposSotano.setHgap(12);
        camposSotano.setVgap(8);
        agregar(camposSotano, 0, "Área del sótano (m²)", areaSotano);
        agregar(camposSotano, 1, "Uso del sótano", tipoUso);
        GridPane.setColumnSpan(camposApartamento, 2);
        GridPane.setColumnSpan(camposCasa, 2);
        GridPane.setColumnSpan(camposSotano, 2);
        formulario.add(camposApartamento, 0, fila++);
        formulario.add(camposCasa, 0, fila++);
        formulario.add(camposSotano, 0, fila++);
        GridPane.setColumnSpan(mensaje, 2);
        mensaje.setStyle("-fx-text-fill: #a12d2d;");
        formulario.add(mensaje, 0, fila);

        estado.getItems().setAll(EstadoInmueble.values());
        tipo.getItems().setAll("APARTAMENTO", "CASA");
        tipoUso.getItems().setAll(TipoUso.values());
        propietario.getItems().setAll(controlador.listarPropietarios());
        propietario.setConverter(new StringConverter<>() {
            @Override public String toString(Propietario valor) {
                return valor == null ? "" : valor.nombre() + " (" + valor.identificacion() + ")";
            }
            @Override public Propietario fromString(String valor) { return null; }
        });
        numeroPiso.setPromptText("Ej. 3");
        cantidadPisos.setPromptText("Ej. 2");
        areaSotano.setPromptText("Ej. 45.5");
        tipo.valueProperty().addListener((observable, anterior, nuevo) -> actualizarCampos(nuevo));
        conSotano.selectedProperty().addListener((observable, anterior, nuevo) -> mostrarSotano(nuevo));
        if (original == null) {
            fecha.setValue(LocalDate.now());
            estado.setValue(EstadoInmueble.DISPONIBLE);
            tipo.setValue("APARTAMENTO");
            numeroPiso.setText("1");
            cantidadPisos.setText("1");
        } else {
            chip.setText(original.chip());
            chip.setDisable(true);
            fecha.setValue(original.fechaCompra());
            direccion.setText(original.direccion());
            estado.setValue(original.estado());
            propietario.setValue(original.propietario());
            tipo.setValue(original.tipo());
            if (original.numeroPiso() != null) numeroPiso.setText(original.numeroPiso().toString());
            if (original.cantidadPisos() != null) cantidadPisos.setText(original.cantidadPisos().toString());
            if (original.areaSotano() != null) {
                conSotano.setSelected(true);
                areaSotano.setText(original.areaSotano().toPlainString());
                tipoUso.setValue(original.tipoUso());
            }
        }
        actualizarCampos(tipo.getValue());
        mostrarSotano(conSotano.isSelected());
    }

    private void actualizarCampos(String valor) {
        boolean apartamento = "APARTAMENTO".equals(valor);
        camposApartamento.setVisible(apartamento);
        camposApartamento.setManaged(apartamento);
        camposCasa.setVisible(!apartamento);
        camposCasa.setManaged(!apartamento);
        boolean sotanoVisible = !apartamento && conSotano.isSelected();
        camposSotano.setVisible(sotanoVisible);
        camposSotano.setManaged(sotanoVisible);
    }

    private void mostrarSotano(boolean visible) {
        boolean mostrar = visible && "CASA".equals(tipo.getValue());
        camposSotano.setVisible(mostrar);
        camposSotano.setManaged(mostrar);
    }

    private InmuebleDto construirDto() {
        if (chip.getText() == null || chip.getText().isBlank()) throw new IllegalArgumentException("El CHIP es obligatorio");
        if (fecha.getValue() == null) throw new IllegalArgumentException("Seleccione la fecha de compra");
        if (direccion.getText() == null || direccion.getText().isBlank()) throw new IllegalArgumentException("La dirección es obligatoria");
        if (estado.getValue() == null) throw new IllegalArgumentException("Seleccione el estado del inmueble");
        if (propietario.getValue() == null) throw new IllegalArgumentException("Seleccione un propietario");
        String clase = tipo.getValue();
        if (clase == null) throw new IllegalArgumentException("Seleccione el tipo de inmueble");
        Integer piso = null;
        Integer pisosCasa = null;
        BigDecimal area = null;
        TipoUso uso = null;
        if ("APARTAMENTO".equals(clase)) {
            piso = entero(numeroPiso.getText(), "El número de piso debe ser un entero mayor que cero");
        } else {
            pisosCasa = entero(cantidadPisos.getText(), "La cantidad de pisos debe ser un entero mayor que cero");
            if (conSotano.isSelected()) {
                try { area = new BigDecimal(areaSotano.getText().trim()); }
                catch (NumberFormatException e) { throw new IllegalArgumentException("Ingrese un área de sótano válida"); }
                if (area.signum() <= 0) throw new IllegalArgumentException("El área del sótano debe ser mayor que cero");
                uso = tipoUso.getValue();
                if (uso == null) throw new IllegalArgumentException("Seleccione el uso del sótano");
            }
        }
        return new InmuebleDto(chip.getText(), fecha.getValue(), direccion.getText(), estado.getValue(),
                propietario.getValue(), clase, piso, pisosCasa, area, uso);
    }

    private int entero(String texto, String error) {
        try {
            int valor = Integer.parseInt(texto.trim());
            if (valor <= 0) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException e) { throw new IllegalArgumentException(error); }
    }

    private void agregar(GridPane contenedor, int fila, String etiqueta, Node campo) {
        contenedor.add(new Label(etiqueta), 0, fila);
        contenedor.add(campo, 1, fila);
        if (campo instanceof TextField texto) texto.setMaxWidth(Double.MAX_VALUE);
    }
}