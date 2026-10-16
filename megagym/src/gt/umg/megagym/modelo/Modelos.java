package gt.umg.megagym.modelo;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;


public final class Modelos {

    private Modelos() {}

    // ESTADOS Y CATEGORIAS

    public enum Categoria {
        CANCHA, TIENDA, EXTRA
    }

    public enum EstadoCancha {
        DISPONIBLE,
        MANTENIMIENTO,
        INACTIVA,
        SUSPENDIDA
    }

    public enum EstadoReserva {
        PENDIENTE,
        CONFIRMADA,
        EN_CURSO,
        FINALIZADA,
        CANCELADA,
        SUSPENDIDA
    }

    // CLASE CLIENTE

    public record Cliente(
        String codigo,
        String nombre,
        String telefono,
        String correo,
        LocalDate alta
    ) {}

    // CLASE CANCHA

    public record Cancha(
        String codigo,
        String nombre,
        String tipo,
        int capacidad,
        EstadoCancha estado,
        LocalTime apertura,
        LocalTime cierre
    ) {}

    // CLASE SERVICIO

    public record Servicio(
        String codigo,
        String nombre,
        BigDecimal precio
    ) {}

    // CLASE PRODUCTO

    public static final class Producto {

        private final String codigo;
        private final String nombre;
        private final String categoria;

        private BigDecimal precio;
        private int existencia;
        private int minimo;
        private boolean activo;

        public Producto(
                String codigo,
                String nombre,
                String categoria,
                BigDecimal precio,
                int existencia,
                int minimo,
                boolean activo) {

            this.codigo = codigo;
            this.nombre = nombre;
            this.categoria = categoria;
            this.precio = precio;
            this.existencia = existencia;
            this.minimo = minimo;
            this.activo = activo;
        }

        public String codigo() {
            return codigo;
        }

        public String nombre() {
            return nombre;
        }

        public String categoria() {
            return categoria;
        }

        public BigDecimal precio() {
            return precio;
        }

        public int existencia() {
            return existencia;
        }

        public int minimo() {
            return minimo;
        }

        public boolean activo() {
            return activo;
        }

        public void descontar(int cantidad) {

            if (cantidad <= 0 ||
                !activo ||
                cantidad > existencia) {

                throw new IllegalArgumentException(
                    "Stock insuficiente para " + nombre
                );
            }

            existencia -= cantidad;
        }

        public void reponer(int cantidad) {

            if (cantidad <= 0) {

                throw new IllegalArgumentException(
                    "La cantidad debe ser positiva"
                );
            }

            existencia += cantidad;
        }
    }

    // TARIFAS

    public record Tarifa(
        String codigo,
        String dia,
        LocalTime inicio,
        LocalTime fin,
        BigDecimal precioHora
    ) {}

    public record TarifaCancha(
        String canchaId,
        BigDecimal normal,
        BigDecimal conLuz
    ) {}

    // CLASE RESERVA

    public static final class Reserva {

        private final String codigo;
        private final String clienteId;
        private final String canchaId;
        private final String cuentaId;

        private final LocalDate fecha;
        private final LocalTime inicio;
        private final int minutos;

        private EstadoReserva estado;

        private LocalDateTime canceladaEn;
        private String motivoCancelacion = "";
        private String operadorCancelacion = "";

        private LocalDateTime suspendidaEn;
        private String motivoSuspension = "";
        private String operadorSuspension = "";

        private String modalidad = "NORMAL";
        private boolean conLuz = false;

        public Reserva(
                String codigo,
                String clienteId,
                String canchaId,
                String cuentaId,
                LocalDate fecha,
                LocalTime inicio,
                int minutos,
                EstadoReserva estado,
                LocalDateTime canceladaEn,
                String motivo,
                String operador) {

            this.codigo = codigo;
            this.clienteId = clienteId;
            this.canchaId = canchaId;
            this.cuentaId = cuentaId;
            this.fecha = fecha;
            this.inicio = inicio;
            this.minutos = minutos;
            this.estado = estado;
            this.canceladaEn = canceladaEn;

            this.motivoCancelacion =
                motivo == null ? "" : motivo;

            this.operadorCancelacion =
                operador == null ? "" : operador;
        }

        public String codigo() {
            return codigo;
        }

        public String clienteId() {
            return clienteId;
        }

        public String canchaId() {
            return canchaId;
        }

        public String cuentaId() {
            return cuentaId;
        }

        public LocalDate fecha() {
            return fecha;
        }

        public LocalTime inicio() {
            return inicio;
        }

        public int minutos() {
            return minutos;
        }

        public LocalTime fin() {
            return inicio.plusMinutes(minutos);
        }

        public EstadoReserva estado() {
            return estado;
        }

        public LocalDateTime canceladaEn() {
            return canceladaEn;
        }

        public String motivoCancelacion() {
            return motivoCancelacion;
        }

        public String operadorCancelacion() {
            return operadorCancelacion;
        }

        public LocalDateTime suspendidaEn() {
            return suspendidaEn;
        }

        public String motivoSuspension() {
            return motivoSuspension;
        }

        public String operadorSuspension() {
            return operadorSuspension;
        }

        public String modalidad() {
            return modalidad;
        }

        public boolean conLuz() {
            return conLuz;
        }

        public void configurarAlquiler(
                String modalidad,
                boolean conLuz) {

            if (!"NORMAL".equals(modalidad)
                    && !"TORNEO".equals(modalidad)) {

                throw new IllegalArgumentException(
                    "Modalidad no reconocida"
                );
            }

            this.modalidad = modalidad;
            this.conLuz = conLuz;
        }

        public void restaurarSuspension(
                LocalDateTime cuando,
                String motivo,
                String operador) {

            suspendidaEn = cuando;

            motivoSuspension =
                motivo == null ? "" : motivo;

            operadorSuspension =
                operador == null ? "" : operador;
        }

        public void cambiarEstado(
                EstadoReserva siguiente,
                String motivo,
                String operador) {

            boolean valido = switch (estado) {

                case PENDIENTE ->
                    siguiente == EstadoReserva.CONFIRMADA
                    || siguiente == EstadoReserva.CANCELADA
                    || siguiente == EstadoReserva.SUSPENDIDA;

                case CONFIRMADA ->
                    siguiente == EstadoReserva.EN_CURSO
                    || siguiente == EstadoReserva.CANCELADA
                    || siguiente == EstadoReserva.SUSPENDIDA;

                case EN_CURSO ->
                    siguiente == EstadoReserva.FINALIZADA;

                case SUSPENDIDA ->
                    siguiente == EstadoReserva.CONFIRMADA;

                default -> false;
            };

            if (!valido) {

                throw new IllegalArgumentException(
                    "Transicion invalida: "
                    + estado + " -> " + siguiente
                );
            }

            if (siguiente == EstadoReserva.CANCELADA) {

                if (motivo == null || motivo.isBlank()
                    || operador == null
                    || operador.isBlank()) {

                    throw new IllegalArgumentException(
                        "La cancelacion requiere motivo y operador"
                    );
                }

                canceladaEn = LocalDateTime.now();
                motivoCancelacion = motivo;
                operadorCancelacion = operador;
            }

            if (siguiente == EstadoReserva.SUSPENDIDA) {

                if (motivo == null || motivo.isBlank()
                    || operador == null
                    || operador.isBlank()) {

                    throw new IllegalArgumentException(
                        "La suspension requiere motivo y operador"
                    );
                }

                suspendidaEn = LocalDateTime.now();
                motivoSuspension = motivo.trim();
                operadorSuspension = operador.trim();
            }

            estado = siguiente;
        }
    }

    // HISTORIAL DE RESERVAS

    public record MovimientoReserva(
        String codigo,
        String reservaId,
        LocalDateTime fechaHora,
        EstadoReserva anterior,
        EstadoReserva nuevo,
        String motivo,
        String operador
    ) {}

    // CUENTAS

    public record Cuenta(
        String codigo,
        String reservaId,
        LocalDateTime creada
    ) {}

    // DETALLE DE CUENTA

    public record Linea(
        String codigo,
        String cuentaId,
        Categoria categoria,
        String referencia,
        String descripcion,
        int cantidad,
        BigDecimal precioUnitario,
        String ventaId
    ) {

        public BigDecimal total() {

            return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad)
            ).setScale(2);
        }
    }

    // VENTAS

    public record Venta(
        String codigo,
        String cuentaId,
        LocalDateTime fecha
    ) {}

    // PAGOS

    public record Pago(
        String codigo,
        String cuentaId,
        LocalDateTime fecha,
        BigDecimal monto,
        String metodo,
        String observacion
    ) {}

    // DISTRIBUCION DE COBROS

    public record Distribucion(
        String pagoId,
        Categoria categoria,
        BigDecimal monto
    ) {}
}
