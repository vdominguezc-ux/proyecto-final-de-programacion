package gt.umg.megagym.persistencia;

import gt.umg.megagym.modelo.Modelos.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;

import static java.nio.file.StandardCopyOption.*;

public final class ArchivosCSV {

    private final Path raiz;

    private final List<String> advertencias =
            new ArrayList<>();

    public ArchivosCSV(Path raiz) {
        this.raiz = raiz;
    }

    public List<String> advertencias() {
        return List.copyOf(advertencias);
    }

    public Path raiz() {
        return raiz;
    }

    public static final class Datos {

        public final Map<String, Cliente> clientes =
                new LinkedHashMap<>();

        public final Map<String, Cancha> canchas =
                new LinkedHashMap<>();

        public final Map<String, Servicio> servicios =
                new LinkedHashMap<>();

        public final Map<String, Producto> productos =
                new LinkedHashMap<>();

        public final Map<String, Tarifa> tarifas =
                new LinkedHashMap<>();

        public final Map<String, TarifaCancha> tarifasCancha =
                new LinkedHashMap<>();

        public final Map<String, Reserva> reservas =
                new LinkedHashMap<>();

        public final Map<String, MovimientoReserva> movimientos =
                new LinkedHashMap<>();

        public final Map<String, Cuenta> cuentas =
                new LinkedHashMap<>();

        public final Map<String, Linea> lineas =
                new LinkedHashMap<>();

        public final Map<String, Venta> ventas =
                new LinkedHashMap<>();

        public final Map<String, Pago> pagos =
                new LinkedHashMap<>();

        public final List<Distribucion> distribuciones =
                new ArrayList<>();

        public boolean moduloTienda = true;
    }

    // CARGAR INFORMACION

    public Datos cargar() throws IOException {

        Files.createDirectories(raiz);

        Datos d = new Datos();

        Path configuracion =
                raiz.resolve("config.properties");

        Properties p = new Properties();

        if (Files.exists(configuracion)) {

            try (Reader r = Files.newBufferedReader(
                    configuracion,
                    StandardCharsets.UTF_8)) {

                p.load(r);
            }
        }

        d.moduloTienda = Boolean.parseBoolean(
                p.getProperty("modulo_tienda", "true")
        );

        cargar("clientes.csv", r -> {

            Cliente x = new Cliente(
                    r.get(0),
                    r.get(1),
                    r.get(2),
                    r.get(3),
                    LocalDate.parse(r.get(4))
            );

            d.clientes.put(x.codigo(), x);
        });

        cargar("canchas.csv", r -> {

            Cancha x = new Cancha(
                    r.get(0),
                    r.get(1),
                    r.get(2),
                    Integer.parseInt(r.get(3)),
                    EstadoCancha.valueOf(r.get(4)),
                    LocalTime.parse(r.get(5)),
                    LocalTime.parse(r.get(6))
            );

            d.canchas.put(x.codigo(), x);
        });

        cargar("servicios.csv", r -> {

            Servicio x = new Servicio(
                    r.get(0),
                    r.get(1),
                    new BigDecimal(r.get(2))
            );

            d.servicios.put(x.codigo(), x);
        });

        cargar("productos.csv", r -> {

            Producto x = new Producto(
                    r.get(0),
                    r.get(1),
                    r.get(2),
                    new BigDecimal(r.get(3)),
                    Integer.parseInt(r.get(4)),
                    Integer.parseInt(r.get(5)),
                    Boolean.parseBoolean(r.get(6))
            );

            d.productos.put(x.codigo(), x);
        });

        cargar("tarifas.csv", r -> {

            Tarifa x = new Tarifa(
                    r.get(0),
                    r.get(1),
                    LocalTime.parse(r.get(2)),
                    LocalTime.parse(r.get(3)),
                    new BigDecimal(r.get(4))
            );

            d.tarifas.put(x.codigo(), x);
        });

        cargar("tarifas_canchas.csv", r -> {

            TarifaCancha x = new TarifaCancha(
                    r.get(0),
                    new BigDecimal(r.get(1)),
                    new BigDecimal(r.get(2))
            );

            d.tarifasCancha.put(x.canchaId(), x);
        });

        cargar("cuentas.csv", r -> {

            Cuenta x = new Cuenta(
                    r.get(0),
                    r.get(1),
                    LocalDateTime.parse(r.get(2))
            );

            d.cuentas.put(x.codigo(), x);
        });

        cargar("reservas.csv", r -> {

            Reserva x = new Reserva(
                    r.get(0),
                    r.get(1),
                    r.get(2),
                    r.get(3),
                    LocalDate.parse(r.get(4)),
                    LocalTime.parse(r.get(5)),
                    Integer.parseInt(r.get(6)),
                    EstadoReserva.valueOf(r.get(7)),
                    r.get(8).isEmpty()
                        ? null
                        : LocalDateTime.parse(r.get(8)),
                    r.get(9),
                    r.get(10)
            );

            if (r.size() >= 14) {

                x.restaurarSuspension(
                        r.get(11).isBlank()
                            ? null
                            : LocalDateTime.parse(r.get(11)),
                        r.get(12),
                        r.get(13)
                );
            }

            if (r.size() >= 16) {

                x.configurarAlquiler(
                        r.get(14).isBlank()
                            ? "NORMAL"
                            : r.get(14),
                        Boolean.parseBoolean(r.get(15))
                );
            }

            d.reservas.put(x.codigo(), x);
        });

        cargar("movimientos_reservas.csv", r -> {

            MovimientoReserva x = new MovimientoReserva(
                    r.get(0),
                    r.get(1),
                    LocalDateTime.parse(r.get(2)),
                    EstadoReserva.valueOf(r.get(3)),
                    EstadoReserva.valueOf(r.get(4)),
                    r.get(5),
                    r.get(6)
            );

            d.movimientos.put(x.codigo(), x);
        });

        cargar("ventas.csv", r -> {

            Venta x = new Venta(
                    r.get(0),
                    r.get(1),
                    LocalDateTime.parse(r.get(2))
            );

            d.ventas.put(x.codigo(), x);
        });

        cargar("lineas_cuenta.csv", r -> {

            Linea x = new Linea(
                    r.get(0),
                    r.get(1),
                    Categoria.valueOf(r.get(2)),
                    r.get(3),
                    r.get(4),
                    Integer.parseInt(r.get(5)),
                    new BigDecimal(r.get(6)),
                    r.get(7)
            );

            d.lineas.put(x.codigo(), x);
        });

        cargar("pagos.csv", r -> {

            Pago x = new Pago(
                    r.get(0),
                    r.get(1),
                    LocalDateTime.parse(r.get(2)),
                    new BigDecimal(r.get(3)),
                    r.get(4),
                    r.get(5)
            );

            d.pagos.put(x.codigo(), x);
        });

        cargar("distribuciones_pagos.csv", r -> {

            d.distribuciones.add(
                    new Distribucion(
                            r.get(0),
                            Categoria.valueOf(r.get(1)),
                            new BigDecimal(r.get(2))
                    )
            );
        });

        validarReferencias(d);

        return d;
    }

    private void validarReferencias(Datos d) {

        for (Reserva r : d.reservas.values()) {

            if (!d.cuentas.containsKey(r.cuentaId())
                    || !d.clientes.containsKey(r.clienteId())
                    || !d.canchas.containsKey(r.canchaId())) {

                advertencias.add(
                        "Referencia faltante en reserva "
                        + r.codigo()
                );
            }
        }

        for (Linea l : d.lineas.values()) {

            if (!d.cuentas.containsKey(l.cuentaId())) {

                advertencias.add(
                        "Cuenta inexistente en linea "
                        + l.codigo()
                );
            }
        }

        for (Pago p : d.pagos.values()) {

            if (!d.cuentas.containsKey(p.cuentaId())) {

                advertencias.add(
                        "Cuenta inexistente en pago "
                        + p.codigo()
                );
            }
        }

        for (MovimientoReserva m : d.movimientos.values()) {

            if (!d.reservas.containsKey(m.reservaId())) {

                advertencias.add(
                        "Reserva inexistente en movimiento "
                        + m.codigo()
                );
            }
        }
    }

    @FunctionalInterface
    private interface Lector {

        void leer(List<String> campos) throws Exception;
    }

    private void cargar(
            String archivo,
            Lector consumidor) throws IOException {

        Path f = raiz.resolve(archivo);

        if (!Files.exists(f)) {
            return;
        }

        List<String> filas = Files.readAllLines(
                f,
                StandardCharsets.UTF_8
        );

        for (int i = 1; i < filas.size(); i++) {

            String fila = filas.get(i);

            if (fila.isBlank()) {
                continue;
            }

            try {

                consumidor.leer(parsear(fila));

            } catch (Exception e) {

                advertencias.add(
                        archivo
                        + " fila "
                        + (i + 1)
                        + ": "
                        + e.getMessage()
                );
            }
        }
    }

    // GUARDAR INFORMACION

    public void guardar(Datos d) throws IOException {

        Files.createDirectories(raiz);

        guardarCsv(
                "clientes.csv",
                "codigo,nombre,telefono,correo,alta",
                d.clientes.values(),
                x -> campos(
                        x.codigo(),
                        x.nombre(),
                        x.telefono(),
                        x.correo(),
                        x.alta()
                )
        );

        guardarCsv(
                "canchas.csv",
                "codigo,nombre,tipo,capacidad,estado,apertura,cierre",
                d.canchas.values(),
                x -> campos(
                        x.codigo(),
                        x.nombre(),
                        x.tipo(),
                        x.capacidad(),
                        x.estado(),
                        x.apertura(),
                        x.cierre()
                )
        );

        guardarCsv(
                "servicios.csv",
                "codigo,nombre,precio",
                d.servicios.values(),
                x -> campos(
                        x.codigo(),
                        x.nombre(),
                        x.precio()
                )
        );

        guardarCsv(
                "productos.csv",
                "codigo,nombre,categoria,precio,existencia,minimo,activo",
                d.productos.values(),
                x -> campos(
                        x.codigo(),
                        x.nombre(),
                        x.categoria(),
                        x.precio(),
                        x.existencia(),
                        x.minimo(),
                        x.activo()
                )
        );

        guardarCsv(
                "tarifas.csv",
                "codigo,dia,inicio,fin,precioHora",
                d.tarifas.values(),
                x -> campos(
                        x.codigo(),
                        x.dia(),
                        x.inicio(),
                        x.fin(),
                        x.precioHora()
                )
        );

        guardarCsv(
                "tarifas_canchas.csv",
                "canchaId,precioNormal,precioConLuz",
                d.tarifasCancha.values(),
                x -> campos(
                        x.canchaId(),
                        x.normal(),
                        x.conLuz()
                )
        );

        guardarCsv(
                "cuentas.csv",
                "codigo,reservaId,creada",
                d.cuentas.values(),
                x -> campos(
                        x.codigo(),
                        x.reservaId(),
                        x.creada()
                )
        );

        guardarCsv(
                "reservas.csv",
                "codigo,clienteId,canchaId,cuentaId,fecha,inicio,minutos,estado,canceladaEn,motivo,operador,suspendidaEn,motivoSuspension,operadorSuspension,modalidad,conLuz",
                d.reservas.values(),
                x -> campos(
                        x.codigo(),
                        x.clienteId(),
                        x.canchaId(),
                        x.cuentaId(),
                        x.fecha(),
                        x.inicio(),
                        x.minutos(),
                        x.estado(),
                        x.canceladaEn(),
                        x.motivoCancelacion(),
                        x.operadorCancelacion(),
                        x.suspendidaEn(),
                        x.motivoSuspension(),
                        x.operadorSuspension(),
                        x.modalidad(),
                        x.conLuz()
                )
        );

        guardarCsv(
                "movimientos_reservas.csv",
                "codigo,reservaId,fechaHora,estadoAnterior,estadoNuevo,motivo,operador",
                d.movimientos.values(),
                x -> campos(
                        x.codigo(),
                        x.reservaId(),
                        x.fechaHora(),
                        x.anterior(),
                        x.nuevo(),
                        x.motivo(),
                        x.operador()
                )
        );

        guardarCsv(
                "ventas.csv",
                "codigo,cuentaId,fecha",
                d.ventas.values(),
                x -> campos(
                        x.codigo(),
                        x.cuentaId(),
                        x.fecha()
                )
        );

        guardarCsv(
                "lineas_cuenta.csv",
                "codigo,cuentaId,categoria,referencia,descripcion,cantidad,precioUnitario,ventaId",
                d.lineas.values(),
                x -> campos(
                        x.codigo(),
                        x.cuentaId(),
                        x.categoria(),
                        x.referencia(),
                        x.descripcion(),
                        x.cantidad(),
                        x.precioUnitario(),
                        x.ventaId()
                )
        );

        guardarCsv(
                "detalle_ventas.csv",
                "ventaId,lineaId,productoId,cantidad,precioUnitario,subtotal",
                d.lineas.values()
                        .stream()
                        .filter(x -> !x.ventaId().isEmpty())
                        .toList(),
                x -> campos(
                        x.ventaId(),
                        x.codigo(),
                        x.referencia(),
                        x.cantidad(),
                        x.precioUnitario(),
                        x.total()
                )
        );

        guardarCsv(
                "pagos.csv",
                "codigo,cuentaId,fecha,monto,metodo,observacion",
                d.pagos.values(),
                x -> campos(
                        x.codigo(),
                        x.cuentaId(),
                        x.fecha(),
                        x.monto(),
                        x.metodo(),
                        x.observacion()
                )
        );

        guardarCsv(
                "distribuciones_pagos.csv",
                "pagoId,categoria,monto",
                d.distribuciones,
                x -> campos(
                        x.pagoId(),
                        x.categoria(),
                        x.monto()
                )
        );

        Properties p = new Properties();

        p.setProperty(
                "modulo_tienda",
                String.valueOf(d.moduloTienda)
        );

        Path f = raiz.resolve("config.properties");

        Path tmp = raiz.resolve("config.properties.tmp");

        try (Writer w = Files.newBufferedWriter(
                tmp,
                StandardCharsets.UTF_8)) {

            p.store(w, "MegaGym - configuracion");
        }

        mover(tmp, f);

        guardarAgenda(d);
    }

    // AGENDA ADMINISTRATIVA

    private void guardarAgenda(Datos d) throws IOException {

        Path agenda = raiz.resolve("Agenda");

        String[] meses = {
            "Enero", "Febrero", "Marzo", "Abril",
            "Mayo", "Junio", "Julio", "Agosto",
            "Septiembre", "Octubre", "Noviembre",
            "Diciembre"
        };

        int primero = Math.min(
                2026,
                LocalDate.now().getYear()
        );

        for (int anio = primero; anio <= 2035; anio++) {

            for (int mes = 1; mes <= 12; mes++) {

                Files.createDirectories(
                        agenda.resolve("Anio_" + anio)
                                .resolve(
                                    String.format(
                                        "Mes_%02d_%s",
                                        mes,
                                        meses[mes - 1]
                                    )
                                )
                );
            }
        }

        Map<LocalDate, List<Reserva>> porFecha =
                new TreeMap<>();

        for (Reserva reserva : d.reservas.values()) {

            porFecha.computeIfAbsent(
                    reserva.fecha(),
                    k -> new ArrayList<>()
            ).add(reserva);
        }

        for (var registro : porFecha.entrySet()) {

            LocalDate dia = registro.getKey();

            if (dia.getYear() < primero
                    || dia.getYear() > 2035) {
                continue;
            }

            Path carpeta = agenda
                    .resolve("Anio_" + dia.getYear())
                    .resolve(
                        String.format(
                            "Mes_%02d_%s",
                            dia.getMonthValue(),
                            meses[dia.getMonthValue() - 1]
                        )
                    );

            Path archivo = carpeta.resolve(
                    String.format(
                            "Dia_%02d_%s.csv",
                            dia.getDayOfMonth(),
                            dia
                    )
            );

            Path temp = carpeta.resolve(
                    archivo.getFileName() + ".tmp"
            );

            try (BufferedWriter w = Files.newBufferedWriter(
                    temp,
                    StandardCharsets.UTF_8)) {

                w.write(
                    "reserva,fecha,horaInicio,horaFin,cancha,cliente,modalidad,conLuz,estadoReserva,cuadro,pagado,saldo"
                );

                w.newLine();

                for (Reserva r : registro.getValue()) {

                    BigDecimal total = BigDecimal.ZERO;
                    BigDecimal abonado = BigDecimal.ZERO;

                    for (Linea l : d.lineas.values()) {

                        if (l.cuentaId().equals(r.cuentaId())) {

                            total = total.add(l.total());
                        }
                    }

                    for (Pago p : d.pagos.values()) {

                        if (p.cuentaId().equals(r.cuentaId())) {

                            abonado = abonado.add(p.monto());
                        }
                    }

                    String cuadro = estadoAgenda(
                            r,
                            abonado,
                            total
                    );

                    Cliente cl = d.clientes.get(r.clienteId());

                    Cancha ca = d.canchas.get(r.canchaId());

                    w.write(
                        campos(
                            r.codigo(),
                            r.fecha(),
                            r.inicio(),
                            r.fin(),
                            ca == null
                                ? r.canchaId()
                                : ca.nombre(),
                            cl == null
                                ? r.clienteId()
                                : cl.nombre(),
                            r.modalidad(),
                            r.conLuz(),
                            r.estado(),
                            cuadro,
                            abonado,
                            total.subtract(abonado)
                        )
                    );

                    w.newLine();
                }
            }

            mover(temp, archivo);
        }
    }

    private static String estadoAgenda(
            Reserva r,
            BigDecimal abonado,
            BigDecimal total) {

        if (r.estado() == EstadoReserva.CANCELADA) {
            return "ELIMINADO";
        }

        if (r.estado() == EstadoReserva.SUSPENDIDA) {
            return "SUSPENDIDO";
        }

        if (total.signum() > 0
                && abonado.compareTo(total) >= 0) {
            return "COBRADO";
        }

        if (abonado.signum() > 0) {
            return "ABONADO";
        }

        return "PENDIENTE";
    }

    @FunctionalInterface
    private interface Escritor<T> {

        String escribir(T valor);
    }

    private <T> void guardarCsv(
            String archivo,
            String cabecera,
            Collection<T> items,
            Escritor<T> escritor) throws IOException {

        Path f = raiz.resolve(archivo);
        Path temp = raiz.resolve(archivo + ".tmp");

        try (BufferedWriter w = Files.newBufferedWriter(
                temp,
                StandardCharsets.UTF_8)) {

            w.write(cabecera);
            w.newLine();

            for (T item : items) {

                w.write(escritor.escribir(item));
                w.newLine();
            }
        }

        mover(temp, f);
    }

    private void mover(
            Path temporal,
            Path destino) throws IOException {

        try {

            Files.move(
                    temporal,
                    destino,
                    REPLACE_EXISTING,
                    ATOMIC_MOVE
            );

        } catch (AtomicMoveNotSupportedException e) {

            Files.move(
                    temporal,
                    destino,
                    REPLACE_EXISTING
            );
        }
    }

    private static String campos(Object... valores) {

        List<String> c = new ArrayList<>();

        for (Object o : valores) {

            String s = o == null ? "" : o.toString();

            c.add(
                    "\"" + s.replace("\"", "\"\"") + "\""
            );
        }

        return String.join(",", c);
    }

    private static List<String> parsear(String s) {

        if (s.startsWith("\uFEFF")) {
            s = s.substring(1);
        }

        List<String> items = new ArrayList<>();

        StringBuilder campo = new StringBuilder();

        boolean entreComillas = false;

        for (int i = 0; i < s.length(); i++) {

            char c = s.charAt(i);

            if (c == '"') {

                if (entreComillas
                        && i + 1 < s.length()
                        && s.charAt(i + 1) == '"') {

                    campo.append('"');
                    i++;

                } else {

                    entreComillas = !entreComillas;
                }

            } else if (c == ',' && !entreComillas) {

                items.add(campo.toString());

                campo.setLength(0);

            } else {

                campo.append(c);
            }
        }

        if (entreComillas) {

            throw new IllegalArgumentException(
                    "CSV: comillas incompletas"
            );
        }

        items.add(campo.toString());

        return items;
    }
}