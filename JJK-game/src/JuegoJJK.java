import java.util.*;
import java.io.*;

public class JuegoJJK {

    // ── Rutas CSV (carpeta data/) ─────────────────────────────────────
    private static final String CSV_PERSONAJES = "data" + File.separator + "personajes.csv";
    private static final String CSV_CRIMENES   = "data" + File.separator + "crimenes.csv";

    // Segundos que el jugador tiene para memorizar su secuencia antes de ocultarse sola
    private static final int SEGUNDOS_SECUENCIA = 7;

    // ── Estado global ─────────────────────────────────────────────────
    private static final List<Personaje> catalogo = new ArrayList<>();
    private static final Scanner sc = new Scanner(System.in);

    private static String nombreJ1 = "Jugador 1";
    private static String nombreJ2 = "Jugador 2";

    private static List<Personaje> equipoActual1 = null;
    private static List<Personaje> equipoActual2 = null;

    private static final MusicaJJK musica = new MusicaJJK.MusicaAudio();

    // ── Dominio ───────────────────────────────────────────────────────
    private static Personaje duenyoDominio = null;
    private static int       turnosDominio = 0;

    // ── Espada del Verdugo ────────────────────────────────────────────
    private static int     golpesPena   = 0;
    private static boolean penaActiva   = false;
    private static boolean espadaActiva = false;

    // ── Crimenes ──────────────────────────────────────────────────────
    private static String[][] crimenes = null;

    // ── Colores ANSI (delegados a Colores.java para evitar duplicación) ──
    // ARREGLO: antes estas 15 constantes duplicaban literales ANSI hardcodeados.
    // Ahora referencian Colores.java, que es la única fuente de verdad.
    static final String RESET        = Colores.RESET;
    static final String NEGRITA      = Colores.NEGRITA;
    static final String ROJO         = Colores.ROJO;
    static final String VERDE        = Colores.VERDE;
    static final String AMARILLO     = Colores.AMARILLO;
    static final String AZUL         = Colores.AZUL;
    static final String MAGENTA      = Colores.MAGENTA;
    static final String CYAN         = Colores.CYAN;
    static final String BLANCO       = Colores.BLANCO;
    static final String ROJO_INT     = Colores.ROJO_INT;
    static final String VERDE_INT    = Colores.VERDE_INT;
    static final String AMARILLO_INT = Colores.AMARILLO_INT;
    static final String AZUL_INT     = Colores.AZUL_INT;
    static final String MAGENTA_INT  = Colores.MAGENTA_INT;
    static final String CYAN_INT     = Colores.CYAN_INT;

    // ═════════════════════════════════════════════════════════════════
    //  HELPERS DE UI
    // ═════════════════════════════════════════════════════════════════

    static String barraHP(int actual, int max) {
        int n = Math.max(0, Math.min(20, actual * 20 / max));
        String c = n > 12 ? VERDE_INT : n > 6 ? AMARILLO_INT : ROJO_INT;
        StringBuilder sb = new StringBuilder(c + "[");
        for (int i = 0; i < 20; i++) sb.append(i < n ? "\u2588" : "\u2591");
        return sb.append("] " + actual + "/" + max + RESET).toString();
    }

    static String barraEnergia(int actual, int max) {
        if (max == 0) return AZUL + "[Sin energia maldita]" + RESET;
        int n = Math.max(0, Math.min(15, actual * 15 / max));
        StringBuilder sb = new StringBuilder(AZUL + "[");
        for (int i = 0; i < 15; i++) sb.append(i < n ? "\u25B0" : "\u25B1");
        return sb.append("] " + actual + "/" + max + RESET).toString();
    }

    static String linea(int ancho, String color) { return color + "=".repeat(ancho) + RESET; }

    /**
     * Empuja el contenido visible fuera de pantalla imprimiendo saltos de línea
     * (funciona en cualquier entorno, incluida la consola de IntelliJ) y después
     * intenta además limpiar el buffer con ANSI/cls para terminales reales.
     */
    private static void limpiarConsola() {
        // Saltos de línea: siempre funcionan, desplazan el texto hacia arriba
        for (int i = 0; i < 60; i++) System.out.println();
        // Intentar limpieza real del buffer (terminal del sistema / cmd.exe)
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J\033[3J"); // limpia pantalla y scrollback
                System.out.flush();
            }
        } catch (Exception ignored) {}
    }

    private static String centrar(String s, int ancho) {
        if (s.length() >= ancho) return s.substring(0, ancho);
        int p = (ancho - s.length()) / 2;
        return " ".repeat(p) + s + " ".repeat(ancho - s.length() - p);
    }

    private static void caja(String titulo, String color) {
        int w = 50;
        System.out.println(color + NEGRITA
                + "  \u2554" + "\u2550".repeat(w) + "\u2557\n"
                + "  \u2551" + RESET + centrar(titulo, w) + color + NEGRITA + "\u2551\n"
                + "  \u255A" + "\u2550".repeat(w) + "\u255D" + RESET);
    }

    // ═════════════════════════════════════════════════════════════════
    //  MAIN
    // ═════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        caja("JUJUTSU KAISEN  -  BATTLE SYSTEM  v4.2", MAGENTA);
        System.out.print(CYAN    + "  Jugador 1: " + RESET);
        String n1 = sc.nextLine().trim(); if (!n1.isEmpty()) nombreJ1 = n1;
        System.out.print(AMARILLO + "  Jugador 2: " + RESET);
        String n2 = sc.nextLine().trim(); if (!n2.isEmpty()) nombreJ2 = n2;
        System.out.println(VERDE_INT + "\n  Bienvenidos, " + NEGRITA + nombreJ1
                + RESET + VERDE_INT + " y " + NEGRITA + nombreJ2 + RESET + VERDE_INT + "!" + RESET);

        String op = "";
        do {
            System.out.println();
            caja("JUJUTSU KAISEN  -  BATTLE SYSTEM", MAGENTA);
            System.out.println("  " + CYAN + "J1: " + NEGRITA + nombreJ1 + RESET
                    + "   " + AMARILLO + "J2: " + NEGRITA + nombreJ2 + RESET);
            System.out.println("  " + linea(50, AZUL));
            System.out.println("  " + VERDE_INT    + "1." + RESET + " JUGAR PARTIDA");
            System.out.println("  " + AMARILLO_INT + "2." + RESET + " ELEGIR MUSICA");
            System.out.println("  " + ROJO_INT     + "3." + RESET + " SALIR");
            System.out.print("\n  " + BLANCO + "> Opcion: " + RESET);
            op = sc.nextLine();
            if      (op.equals("1")) jugarPartida();
            else if (op.equals("2")) musica.mostrarMenuSeleccion(sc);
            else if (!op.equals("3")) System.out.println("  " + ROJO + "Opcion no valida." + RESET);
        } while (!op.equals("3"));

        musica.detener();
        System.out.println(CYAN + "\n  Gracias por jugar. Adios!" + RESET);
    }

    // ═════════════════════════════════════════════════════════════════
    //  PARTIDA
    // ═════════════════════════════════════════════════════════════════

    private static void jugarPartida() {
        try {
            catalogo.clear();
            cargarPersonajes(CSV_PERSONAJES);
            cargarCrimenes(CSV_CRIMENES);
            duenyoDominio = null; turnosDominio = 0;
            penaActiva = false; golpesPena = 0; espadaActiva = false;

            List<Personaje> e1 = seleccionarEquipo(1);
            List<Personaje> e2 = seleccionarEquipo(2);
            equipoActual1 = e1; equipoActual2 = e2;

            musica.reproducir();
            while (equipoVivo(e1) && equipoVivo(e2)) {
                turnoEquipo(e1, e2, "EQUIPO 1");
                if (equipoVivo(e2)) turnoEquipo(e2, e1, "EQUIPO 2");
            }
            musica.detener();

            boolean g1 = equipoVivo(e1);
            caja("EL COMBATE HA TERMINADO!", AMARILLO_INT);
            System.out.println("  " + AMARILLO_INT + "GANADOR: " + NEGRITA
                    + (g1 ? e1.get(0).getNombre() : e2.get(0).getNombre()) + RESET
                    + AMARILLO_INT + "  [" + (g1 ? nombreJ1 : nombreJ2) + "]" + RESET);
            System.out.print("\n  Pulsa Enter para volver al menu...");
            sc.nextLine();
        } catch (Exception e) {
            System.out.println(ROJO + "  Error: " + e.getMessage() + RESET);
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  CARGA CSV
    // ═════════════════════════════════════════════════════════════════

    private static void cargarPersonajes(String ruta) throws Exception {
        try (BufferedReader br = utf8(ruta)) {
            String linea; boolean primera = true; int n = 0;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (primera) { primera = false; continue; }
                String[] f = linea.split("\\|", -1);
                if (f.length < 37) throw new Exception("Linea con " + f.length + " columnas (necesarias 37): " + f[0]);
                boolean esMald = f[1].trim().equalsIgnoreCase("MALDICION");
                Personaje p = esMald
                        ? new Maldicion(f[0].trim(), parseInt(f[2]), parseInt(f[3]))
                        : new Hechicero(f[0].trim(), parseInt(f[2]), parseInt(f[3]));
                for (int i = 0; i < 5; i++) {
                    int b = 4 + i * 6;
                    p.addHabilidad(new Habilidad(f[b].trim(), f[b+1].trim(),
                            parseInt(f[b+2]), parseInt(f[b+3]),
                            Efecto.Tipo.valueOf(f[b+4].trim()), Boolean.parseBoolean(f[b+5].trim())));
                }
                p.setCuracionDisponible (parseBool(f[34]));
                p.setPortadorHerramienta(parseBool(f[35]));
                p.setSinEnergiaMaldita  (parseBool(f[36]));
                // Columna 38 (índice 37): especial — retrocompatible, false si ausente
                p.setEspecial(f.length > 37 && parseBool(f[37]));
                catalogo.add(p); n++;
            }
            if (n == 0) throw new Exception("personajes.csv no contiene personajes validos.");
            System.out.println(VERDE_INT + "  " + n + " personajes cargados." + RESET);
        }
    }

    private static void cargarCrimenes(String ruta) throws Exception {
        List<String[]> lista = new ArrayList<>();
        try (BufferedReader br = utf8(ruta)) {
            String linea; boolean primera = true;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (primera) { primera = false; continue; }
                String[] f = linea.split("\\|", -1);
                if (f.length >= 3) lista.add(new String[]{ f[1].trim(), f[2].trim(), f[0].trim() });
            }
        }
        // Sin fallback: si el CSV falta o está vacío, la partida no puede iniciarse
        if (lista.isEmpty())
            throw new Exception("crimenes.csv no contiene crimenes validos. Comprueba el archivo en data/.");
        crimenes = lista.toArray(new String[0][]);
        System.out.println(VERDE_INT + "  " + lista.size() + " crimenes cargados." + RESET);
    }

    private static BufferedReader utf8(String r) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(r), java.nio.charset.StandardCharsets.UTF_8));
    }
    private static int     parseInt (String s) { return Integer.parseInt(s.trim()); }
    private static boolean parseBool(String s) { return Boolean.parseBoolean(s.trim()); }

    // ═════════════════════════════════════════════════════════════════
    //  SELECCION DE EQUIPO  (con validación de entrada)
    // ═════════════════════════════════════════════════════════════════

    private static List<Personaje> seleccionarEquipo(int num) {
        String jugador = (num == 1) ? nombreJ1 : nombreJ2;
        String colorJ  = (num == 1) ? CYAN : AMARILLO;

        // Solo los personajes seleccionables (especial=false)
        List<Personaje> seleccionables = new ArrayList<>();
        for (Personaje c : catalogo) if (!c.isEspecial()) seleccionables.add(c);

        System.out.println("\n" + colorJ + NEGRITA
                + "  -- SELECCION: " + jugador.toUpperCase() + " (Equipo " + num + ") --" + RESET);
        System.out.println("  " + linea(64, AZUL));
        System.out.printf("  %s%-3s %-26s %-12s %6s %6s%s%n",
                AZUL_INT, "ID", "NOMBRE", "TIPO", "HP", "CE", RESET);
        System.out.println("  " + linea(64, AZUL));
        for (int i = 0; i < seleccionables.size(); i++) {
            Personaje c = seleccionables.get(i);
            boolean esMald = c instanceof Maldicion;
            String ct = esMald ? ROJO : VERDE;
            String tipo = esMald ? "[Maldicion] " : "[Hechicero] ";
            String ce = c.getMaxEnergia() == 0
                    ? ROJO_INT + "     -" + RESET
                    : String.format("%6d", c.getMaxEnergia());
            System.out.printf("  %s%2d.%s %-26s %s%-12s%s %s%6d%s %s%s%n",
                    AMARILLO_INT, i, RESET, c.getNombre(),
                    ct, tipo, RESET, VERDE_INT, c.getMaxVida(), RESET, AZUL_INT, ce);
        }
        System.out.println("  " + linea(64, AZUL));

        Personaje sel = null;
        while (sel == null) {
            System.out.print("  " + colorJ + "[" + jugador + "] ID del personaje (0-"
                    + (seleccionables.size() - 1) + "): " + RESET);
            try {
                int idx = parseInt(sc.nextLine());
                if (idx < 0 || idx >= seleccionables.size()) {
                    System.out.println("  " + ROJO + "ID fuera de rango. Elige entre 0 y "
                            + (seleccionables.size() - 1) + "." + RESET);
                } else {
                    sel = seleccionables.get(idx);
                }
            } catch (NumberFormatException e) {
                System.out.println("  " + ROJO + "Entrada no valida. Introduce un numero." + RESET);
            }
        }

        List<Personaje> eq = new ArrayList<>();
        eq.add(sel);
        if (sel instanceof Combatiente) ((Combatiente) sel).manifestarAura();
        return eq;
    }

    // ═════════════════════════════════════════════════════════════════
    //  GESTION DEL DOMINIO
    // ═════════════════════════════════════════════════════════════════

    private static void gestionarDominio(List<Personaje> todos) {
        if (duenyoDominio == null) return;
        if (turnosDominio > 0 && duenyoDominio.estaVivo()) {
            turnosDominio--;
            System.out.println("\n" + MAGENTA + "  DOMINIO ACTIVO: " + NEGRITA
                    + duenyoDominio.getNombre() + RESET + MAGENTA
                    + " - Turnos restantes: " + turnosDominio + RESET);
            if (duenyoDominio.getNombre().equals("Sukuna")) {
                System.out.println("  Santuario Malevolo: cortes en area (-40 HP).");
                todos.stream().filter(p -> p != duenyoDominio && p.estaVivo()).forEach(p -> p.recibirDanioFijo(40));
            }
            if (duenyoDominio.getNombre().equals("Kenjaku")) {
                System.out.println("  Gran Juego: absorbe CE del area (-60 CE).");
                todos.stream().filter(p -> p != duenyoDominio && p.estaVivo()).forEach(p -> p.drenarEnergia(60));
            }
            todos.stream().filter(p -> p != duenyoDominio).forEach(p -> p.setDentroDeDominio(true));
        } else {
            System.out.println("\n" + MAGENTA + "  El dominio de " + NEGRITA
                    + duenyoDominio.getNombre() + RESET + MAGENTA + " se ha disipado." + RESET);
            duenyoDominio.setDominioActivo(false); duenyoDominio.setBurnout(2);
            todos.forEach(p -> p.setDentroDeDominio(false));
            duenyoDominio = null;
        }
    }

    // ═════════════════════════════════════════════════════════════════
    //  TURNO DE UN EQUIPO
    // ═════════════════════════════════════════════════════════════════

    private static void turnoEquipo(List<Personaje> atacantes, List<Personaje> defensores, String nombreEq) {
        List<Personaje> todos = new ArrayList<>(atacantes); todos.addAll(defensores);
        gestionarDominio(todos);

        String jugA = nombreEq.equals("EQUIPO 1") ? nombreJ1 : nombreJ2;
        String jugD = nombreEq.equals("EQUIPO 1") ? nombreJ2 : nombreJ1;
        String colorJ = nombreEq.equals("EQUIPO 1") ? CYAN : AMARILLO;

        for (int i = 0; i < atacantes.size(); i++) {
            Personaje p = atacantes.get(i);
            if (p == null || !p.estaVivo()) continue;
            // ARREGLO: capturar el estado ANTES de prepararTurno().
            // prepararTurno() decrementa turnosInmovilizado; si era 1 pasaría a 0
            // y la comprobación posterior nunca omitía el turno (1 turno = sin efecto).
            boolean estabaInmovilizado = p.estaInmovilizado();
            p.prepararTurno();
            if (estabaInmovilizado) {
                System.out.println("\n  " + ROJO + NEGRITA + "TURNO OMITIDO: " + RESET + ROJO
                        + p.getNombre() + " - " + NEGRITA + p.getCausaInmovilizacion() + RESET);
                continue;
            }

            int pad1 = Math.max(1, 41 - p.getNombre().length());
            int pad2 = Math.max(1, 42 - jugA.length() - nombreEq.length());
            System.out.println("\n" + colorJ + NEGRITA
                    + "  \u2554" + "\u2550".repeat(50) + "\u2557\n"
                    + "  \u2551  TURNO: " + p.getNombre() + RESET + colorJ + " ".repeat(pad1) + "\u2551\n"
                    + "  \u2551  \uD83C\uDFAE " + jugA + " (" + nombreEq + ")" + " ".repeat(pad2) + "\u2551\n"
                    + "  \u255A" + "\u2550".repeat(50) + "\u255D" + RESET);
            System.out.println("  \u2764  HP:  " + barraHP(p.getVida(), p.getMaxVida()));
            if (p.getMaxEnergia() > 0)
                System.out.println("  \u26A1 CE:  " + barraEnergia(p.getEnergia(), p.getMaxEnergia()));

            boolean ok = false;
            while (!ok) {
                System.out.println("\n  " + linea(50, AZUL));
                System.out.println("  " + VERDE_INT + "1." + RESET + " Habilidades Especiales");
                System.out.println("  " + VERDE_INT + "2." + RESET + " Ataque Basico (Fisico)");
                System.out.println("  " + VERDE_INT + "3." + RESET + " Guardia");
                if (p.puedeUsarEspeciales()) System.out.println("  " + VERDE_INT + "4." + RESET + " Recargar Energia");
                if (p.puedeCurarse())        System.out.println("  " + VERDE_INT + "5." + RESET + " Curarse (RCT / Regeneracion)");
                System.out.print("  " + BLANCO + "> Accion: " + RESET);
                try { ok = accion(parseInt(sc.nextLine()), p, i, atacantes, defensores, todos, jugD); }
                catch (Exception e) { System.out.println(ROJO + "  " + e.getMessage() + ". Intenta de nuevo." + RESET); }
            }
        }
    }

    private static boolean accion(int a, Personaje p, int idx, List<Personaje> atac,
                                  List<Personaje> def, List<Personaje> todos, String jugDef) throws Exception {
        switch (a) {
            case 1: return habilidad(p, idx, atac, def, todos, jugDef);
            case 2:
                // ARREGLO: null-check para primero(def); aunque en condiciones normales
                // nunca ocurre (el bucle de combate verifica equipoVivo), es buena práctica.
                Personaje objetivo = primero(def);
                if (objetivo == null) {
                    System.out.println("  " + ROJO + "No hay rivales vivos." + RESET);
                    return false;
                }
                p.ataqueBasico(objetivo);
                checkNaoya(def, atac);
                return true;
            case 3: p.setDefensa(true); System.out.println("  " + p.getNombre() + " se pone en guardia."); return true;
            case 4:
                if (!p.puedeUsarEspeciales()) { System.out.println("  Sin energia maldita."); return false; }
                p.recargarEnergia(); return true;
            case 5:
                if (!p.puedeCurarse()) { System.out.println("  Este personaje no puede curarse."); return false; }
                p.curarse(); return true;
            default: System.out.println("  Accion no reconocida."); return false;
        }
    }

    private static boolean habilidad(Personaje p, int idx, List<Personaje> atac,
                                     List<Personaje> def, List<Personaje> todos, String jugDef) throws Exception {
        List<Habilidad> habs = p.getHabilidades();
        System.out.println("\n  " + CYAN + NEGRITA + "-- Habilidades de " + p.getNombre() + " --" + RESET);
        for (int j = 0; j < habs.size(); j++) {
            String c = habs.get(j).getCosteEnergia() > 0
                    ? AZUL + " (CE: " + habs.get(j).getCosteEnergia() + ")" + RESET
                    : VERDE + " (Gratis)" + RESET;
            System.out.println("  " + AMARILLO_INT + j + "." + RESET + " " + habs.get(j).getNombre() + c);
        }
        System.out.print("  " + BLANCO + "> Habilidad (0-" + (habs.size()-1) + ") o -1 para volver: " + RESET);

        int habIdx;
        try {
            habIdx = parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("  " + ROJO + "Entrada no valida." + RESET);
            return false;
        }

        // ARREGLO: antes se retornaba false sin mensaje si el índice era inválido.
        if (habIdx == -1) return false;
        if (habIdx < 0 || habIdx >= habs.size()) {
            System.out.println("  " + ROJO + "Indice de habilidad fuera de rango (0-"
                    + (habs.size()-1) + ")." + RESET);
            return false;
        }
        Habilidad h = habs.get(habIdx);

        if (h.getNombre().equals("MAHORAGA")) {
            System.out.println("\n  Megumi invoca al General Divino Mahoraga!");
            atac.set(idx, mahoraga()); return true;
        }
        if (h.getNombre().equals("ESPADA DEL VERDUGO")) {
            Personaje obj = primero(def);
            if (obj != null) ejecutarPena(obj);
            return true;
        }

        boolean esDominio = h.getNombre().contains("EXPANSI") || h.getNombre().contains("IDLE DEATH")
                || h.getNombre().contains("AUTOENCARNACI")
                || (h.getNombre().contains("ATA") && h.getNombre().contains("D DE LA"));

        if (esDominio) {
            Personaje rival = primero(def);
            if (rival == null) {
                System.out.println("  " + ROJO + "No hay rival sobre el que expandir el dominio." + RESET);
                return false;
            }
            if (h.getNombre().contains("TRIBUNAL MALDITO")) {
                resolverAcciones(p, habIdx, rival, new int[]{0,-1}, atac, def, idx, todos);
            } else {
                resolverAcciones(p, habIdx, rival, pedirRespuesta(rival, jugDef), atac, def, idx, todos);
            }
        } else {
            p.usarHabilidad(habIdx, primero(def));
        }
        checkNaoya(def, atac); checkNaoya(atac, def);
        return true;
    }

    // ═════════════════════════════════════════════════════════════════
    //  DOMINIOS
    // ═════════════════════════════════════════════════════════════════

    private static int[] pedirRespuesta(Personaje rival, String nombreRival) {
        System.out.println("\n" + ROJO_INT + NEGRITA + "  DOMINIO DECLARADO! [" + nombreRival + "] responde ahora." + RESET);
        System.out.println("  " + rival.getNombre() + " - HP: " + rival.getVida() + " | CE: " + rival.getEnergia());
        System.out.println("  " + linea(52, AZUL));
        while (true) {
            System.out.println("  1. Habilidades  2. Ataque Basico  3. Guardia"
                    + (rival.puedeUsarEspeciales() ? "  4. Recargar" : "")
                    + (rival.puedeCurarse() ? "  5. Curarse" : ""));
            System.out.print("  [" + nombreRival + "] > ");
            try {
                int a = parseInt(sc.nextLine());
                if (a == 1) {
                    List<Habilidad> habs = rival.getHabilidades();
                    for (int j = 0; j < habs.size(); j++)
                        System.out.println("  " + j + ". " + habs.get(j).getNombre() + " (CE:" + habs.get(j).getCosteEnergia() + ")");
                    System.out.print("  Habilidad: ");
                    try {
                        int h = parseInt(sc.nextLine());
                        if (h >= 0 && h < habs.size()) return new int[]{1, h};
                        else System.out.println("  " + ROJO + "Indice fuera de rango." + RESET);
                    } catch (NumberFormatException ex) {
                        System.out.println("  " + ROJO + "Entrada no valida." + RESET);
                    }
                } else if (a == 2) { return new int[]{2,-1};
                } else if (a == 3) { return new int[]{3,-1};
                } else if (a == 4 && rival.puedeUsarEspeciales()) { return new int[]{4,-1};
                } else if (a == 5 && rival.puedeCurarse())        { return new int[]{5,-1};
                } else { System.out.println("  Opcion no valida."); }
            } catch (NumberFormatException e) { System.out.println("  Entrada invalida."); }
        }
    }

    private static void resolverAcciones(Personaje atacante, int habIdx, Personaje rival, int[] resp,
                                         List<Personaje> eqAt, List<Personaje> eqDef, int idxAt, List<Personaje> todos) {
        Habilidad hab = atacante.getHabilidades().get(habIdx);
        if (hab.getNombre().contains("TRIBUNAL MALDITO")) {
            System.out.println("\n" + MAGENTA + "=".repeat(54) + " RESOLUCION" + RESET);
            duenyoDominio = atacante; atacante.setDominioActivo(true); turnosDominio = 1;
            todos.stream().filter(p -> p != atacante && !p.isSinEnergiaMaldita()).forEach(p -> p.setDentroDeDominio(true));
            System.out.println(MAGENTA + "  " + atacante.getNombre() + " EXPANDE EL TRIBUNAL MALDITO!" + RESET);
            juicio(rival, nombreJugador(rival));
            duenyoDominio.setDominioActivo(false); duenyoDominio.setBurnout(2);
            todos.forEach(p -> p.setDentroDeDominio(false));
            duenyoDominio = null; turnosDominio = 0;
            System.out.println(MAGENTA + "=".repeat(65) + RESET); return;
        }

        boolean rivalDominio = resp[0] == 1 && resp[1] >= 0 && rival.estaVivo() && (
                rival.getHabilidades().get(resp[1]).getNombre().contains("EXPANSI")
                        || rival.getHabilidades().get(resp[1]).getNombre().contains("IDLE DEATH")
                        || rival.getHabilidades().get(resp[1]).getNombre().contains("AUTOENCARNACI")
                        || (rival.getHabilidades().get(resp[1]).getNombre().contains("ATA")
                        && rival.getHabilidades().get(resp[1]).getNombre().contains("D DE LA")));

        System.out.println("\n" + MAGENTA + "=".repeat(54) + " RESOLUCION" + RESET);
        if (rivalDominio) {
            System.out.println("  Ambos expanden dominio simultaneamente!");
            duenyoDominio = atacante; atacante.setDominioActivo(true); turnosDominio = 4;
            todos.stream().filter(p -> p != atacante).forEach(p -> p.setDentroDeDominio(true));
            choqueDominio(rival, todos);
            try { atacante.usarHabilidad(habIdx, rival); } catch (Exception ex) { System.out.println(ex.getMessage()); }
        } else {
            choqueDominio(atacante, todos);
            try { atacante.usarHabilidad(habIdx, rival); } catch (Exception ex) { System.out.println(ex.getMessage()); }
            System.out.println("\n  -- Respuesta de " + rival.getNombre() + " --");
            accionSimple(rival, resp, eqAt, eqDef, idxAt);
        }
        System.out.println(MAGENTA + "=".repeat(65) + RESET);
    }

    private static void accionSimple(Personaje p, int[] a, List<Personaje> eqObj, List<Personaje> eqProp, int idx) {
        try {
            switch (a[0]) {
                case 1:
                    Habilidad h = p.getHabilidades().get(a[1]);
                    if (h.getNombre().equals("MAHORAGA")) { eqProp.set(idx, mahoraga()); }
                    else { p.usarHabilidad(a[1], primero(eqObj)); } break;
                case 2: p.ataqueBasico(primero(eqObj)); break;
                case 3: p.setDefensa(true); System.out.println("  " + p.getNombre() + " en guardia."); break;
                case 4: if (p.puedeUsarEspeciales()) p.recargarEnergia(); break;
                case 5: if (p.puedeCurarse()) p.curarse(); break;
            }
        } catch (Exception e) { System.out.println("  " + e.getMessage()); }
    }

    private static void choqueDominio(Personaje atacante, List<Personaje> todos) {
        if (duenyoDominio == null || !duenyoDominio.estaVivo() || duenyoDominio == atacante) {
            System.out.println("  " + atacante.getNombre() + " EXPANDE SU DOMINIO!");
            activarDominio(atacante, todos); return;
        }
        Personaje def = duenyoDominio;
        if (def.isSinEnergiaMaldita()) {
            System.out.println("  " + def.getNombre() + " sin CE: golpe seguro anulado.");
            def.setDentroDeDominio(false); activarDominio(atacante, todos); def.setDentroDeDominio(false); return;
        }
        if (!tieneDominio(def)) {
            System.out.println("  " + def.getNombre() + " sin dominio propio. Queda atrapado.");
            activarDominio(atacante, todos); return;
        }

        String nA = nombreJugador(atacante), nD = nombreJugador(def);
        System.out.println("\n" + MAGENTA_INT + NEGRITA
                + "  \u2554" + "\u2550".repeat(52) + "\u2557\n"
                + "  \u2551" + centrar("CHOQUE DE DOMINIOS", 52) + "\u2551\n"
                + "  \u2551" + centrar(atacante.getNombre() + " VS " + def.getNombre(), 52) + "\u2551\n"
                + "  \u255A" + "\u2550".repeat(52) + "\u255D" + RESET);
        System.out.println("  3 rondas (4-5-6 digitos). Empate: gana el DEFENSOR.");
        boolean aPrio = atacante.getNombre().equals("Sukuna") || atacante.getNombre().equals("Kenjaku");
        boolean dPrio = def.getNombre().equals("Sukuna") || def.getNombre().equals("Kenjaku");
        int pA = aPrio ? 1 : 0, pD = dPrio ? 1 : 0;
        if (aPrio) System.out.println("  " + ROJO_INT + atacante.getNombre() + " +1 punto de ventaja." + RESET);
        if (dPrio) System.out.println("  " + ROJO_INT + def.getNombre()      + " +1 punto de ventaja." + RESET);
        System.out.print("  Pulsa Enter cuando esteis listos..."); sc.nextLine();

        for (int r = 1; r <= 3; r++) {
            int lon = 3 + r;
            char[] sA = genSec(lon), sD = genSec(lon);
            System.out.println("\n  " + CYAN + NEGRITA + "== RONDA " + r + " ==" + RESET + CYAN + "  (" + lon + " digitos)" + RESET);
            System.out.println("  " + CYAN + "[" + nA + "] " + pA + " - " + pD + " [" + nD + "]" + RESET);
            boolean aA = secuencia(sA, atacante.getNombre(), nA, CYAN);
            boolean aD = secuencia(sD, def.getNombre(), nD, AMARILLO);
            if (aA) pA++; if (aD) pD++;
            System.out.print("  Ronda " + r + ": ");
            if (aA && aD) System.out.println(VERDE + "Ambos acertaron." + RESET + " (" + pA + "-" + pD + ")");
            else if (aA)  System.out.println(VERDE + "Punto para [" + nA + "]" + RESET + " (" + pA + "-" + pD + ")");
            else if (aD)  System.out.println(AMARILLO_INT + "Punto para [" + nD + "]" + RESET + " (" + pA + "-" + pD + ")");
            else          System.out.println(ROJO + "Ambos fallaron." + RESET + " (" + pA + "-" + pD + ")");
        }

        System.out.println("\n  " + linea(52, MAGENTA));
        System.out.printf("  %sRESULTADO%s  [%s%s%s] %d - %d [%s%s%s]%n",
                MAGENTA_INT+NEGRITA, RESET, CYAN+NEGRITA, nA, RESET, pA, pD, AMARILLO+NEGRITA, nD, RESET);
        System.out.println("  " + linea(52, MAGENTA));

        if (pA > pD) {
            System.out.println("  " + CYAN + NEGRITA + "[" + nA + "] gana el choque." + RESET);
            def.setDominioActivo(false); def.setBurnout(2);
            todos.forEach(p -> p.setDentroDeDominio(false));
            activarDominio(atacante, todos);
        } else {
            System.out.println("  " + AMARILLO + NEGRITA + "[" + nD + "] defiende su dominio." + RESET);
            atacante.setBurnout(2);
        }
        System.out.println("  " + linea(52, MAGENTA));
    }

    private static void activarDominio(Personaje atacante, List<Personaje> todos) {
        duenyoDominio = atacante; atacante.setDominioActivo(true); turnosDominio = 4;
        for (Personaje p : todos) {
            if (p == atacante) continue;
            if (p.isSinEnergiaMaldita()) System.out.println("  " + p.getNombre() + " resiste el golpe seguro.");
            else p.setDentroDeDominio(true);
        }
    }

    private static boolean tieneDominio(Personaje p) {
        return p.getHabilidades().stream().anyMatch(h -> h.getNombre().contains("EXPANSI")
                || h.getNombre().contains("IDLE DEATH") || h.getNombre().contains("AUTOENCARNACI")
                || (h.getNombre().contains("ATA") && h.getNombre().contains("D DE LA")));
    }

    // ═════════════════════════════════════════════════════════════════
    //  TRIBUNAL MALDITO
    // ═════════════════════════════════════════════════════════════════

    private static void juicio(Personaje acusado, String jugador) {
        System.out.println("\n" + MAGENTA + NEGRITA + "  == TRIBUNAL MALDITO - JUDGEMAN ==" + RESET);
        System.out.println("  Acusado: " + NEGRITA + acusado.getNombre() + RESET + "  [" + jugador + "]");
        if (penaActiva) { ejecutarPena(acusado); return; }

        int[] r = celebrarJuicio(acusado.getNombre(), jugador, "PRIMER JUICIO");
        if (r[1] == 1) { System.out.println("  " + VERDE + "INOCENTE! Queda libre." + RESET); return; }
        System.out.println("  " + ROJO + "CULPABLE provisional." + RESET);

        if (r[0] >= 2) {
            System.out.println("  Cargo grave. Apelar? (s/n): ");
            System.out.print("  [" + jugador + "] > ");
            String resp = sc.nextLine().trim().toLowerCase();
            if (resp.equals("s") || resp.equals("si")) {
                int[] r2 = celebrarJuicio(acusado.getNombre(), jugador, "APELACION");
                if (r2[1] == 1) { System.out.println("  " + VERDE + "Sentencia anulada." + RESET); }
                else { System.out.println("  " + ROJO + "Apelacion fallida." + RESET); veredicto(acusado, r[0]); }
                return;
            }
        }
        veredicto(acusado, r[0]);
    }

    private static int[] celebrarJuicio(String personaje, String jugador, String etiqueta) {
        int idx = (int)(Math.random() * crimenes.length);
        String crimen = crimenes[idx][0], defCorr = crimenes[idx][1];
        int grav = parseInt(crimenes[idx][2]);
        String nivel = grav == 1 ? "LEVE" : grav == 2 ? "GRAVE" : "FATAL";
        System.out.println("\n  -- " + etiqueta + " -- [" + nivel + "]");
        System.out.println("  Cargo: " + crimen.toUpperCase());
        System.out.println("\n  [" + jugador + "] Elige la defensa de " + personaje + ":");

        List<String> opts = new ArrayList<>(Arrays.asList(defCorr));
        // ARREGLO: si hay menos de 3 crímenes, otros.get(0/1) lanzaba IndexOutOfBoundsException.
        // Se rellena con la defensa correcta repetida si no hay suficientes alternativas.
        List<Integer> otros = new ArrayList<>();
        for (int k = 0; k < crimenes.length; k++) if (k != idx) otros.add(k);
        Collections.shuffle(otros);
        opts.add(otros.size() > 0 ? crimenes[otros.get(0)][1] : defCorr);
        opts.add(otros.size() > 1 ? crimenes[otros.get(1)][1] : defCorr);
        Collections.shuffle(opts);
        int pos = opts.indexOf(defCorr);
        for (int k = 0; k < opts.size(); k++) System.out.println("  " + (k+1) + ". " + opts.get(k));

        int el = -1;
        while (el < 1 || el > 3) {
            System.out.print("  Defensa (1-3): ");
            try { el = parseInt(sc.nextLine()); } catch (Exception e) { el = -1; }
        }
        return new int[]{ grav, (el-1) == pos ? 1 : 0 };
    }

    private static void veredicto(Personaje acusado, int grav) {
        if (grav <= 2) {
            System.out.println("\n  CONFISCACION.");
            if (acusado.tieneHerramientaMaldita()) { System.out.println("  Herramienta destruida."); acusado.confiscarHerramienta(); }
            else if (acusado.puedeUsarEspeciales()) { System.out.println("  Tecnica sellada (burnout 2)."); acusado.setBurnout(2); }
            else { System.out.println("  CE confiscada."); acusado.confiscarEnergia(); }
        } else {
            System.out.println("\n  PENA DE MUERTE.");
            if (!espadaActiva) {
                System.out.println("  Higuruma obtiene la ESPADA DEL VERDUGO!"); espadaActiva = true;
                for (List<Personaje> eq : Arrays.asList(equipoActual1, equipoActual2)) {
                    if (eq == null) continue;
                    for (Personaje p : eq) {
                        if (p != null && p.getNombre().equals("Hiromi Higuruma")) {
                            boolean tiene = p.getHabilidades().stream().anyMatch(h -> h.getNombre().equals("ESPADA DEL VERDUGO"));
                            if (!tiene) p.addHabilidad(new Habilidad("ESPADA DEL VERDUGO",
                                    "1 impacto = -60% HP; 2 impactos = muerte instantanea.", 0, 0, Efecto.Tipo.NORMAL, true));
                            break;
                        }
                    }
                }
            }
            penaActiva = true; golpesPena = 0; ejecutarPena(acusado);
        }
    }

    static void ejecutarPena(Personaje acusado) {
        if (!espadaActiva) return;
        System.out.println("\n  ESPADA DEL VERDUGO -> " + acusado.getNombre().toUpperCase());
        if (Math.random() < 0.30) {
            acusado.marcarUltimoGolpe(true); golpesPena++;
            if (golpesPena == 1) {
                int d = (int)(acusado.getVida() * 0.60);
                System.out.println("  CONECTA! " + acusado.getNombre() + " pierde el 60% de vida (" + d + " danio).");
                acusado.recibirDanioFijo(d); System.out.println("  (Un segundo golpe es letal.)");
            } else {
                System.out.println("  SEGUNDO GOLPE! Sentencia cumplida."); acusado.recibirDanioFijo(acusado.getVida());
                espadaActiva = false; penaActiva = false; golpesPena = 0;
            }
        } else { System.out.println("  " + acusado.getNombre() + " esquiva la espada."); }
        if (turnosDominio == 0) penaActiva = false;
    }

    // ═════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════

    // Alfabeto del choque: digitos 0-9 + letras A-Z (36 simbolos)
    private static final char[] ALFABETO_SEC;
    static {
        StringBuilder ab = new StringBuilder();
        for (char c = '0'; c <= '9'; c++) ab.append(c);
        for (char c = 'A'; c <= 'Z'; c++) ab.append(c);
        ALFABETO_SEC = ab.toString().toCharArray();
    }

    private static char[] genSec(int n) {
        char[] s = new char[n];
        for (int i = 0; i < n; i++) s[i] = ALFABETO_SEC[(int)(Math.random() * ALFABETO_SEC.length)];
        return s;
    }

    private static boolean secuencia(char[] sec, String personaje, String jugador, String color) {
        System.out.println("\n  " + color + NEGRITA + "[" + jugador + "] - " + personaje + RESET);
        StringBuilder sb = new StringBuilder("  " + color + NEGRITA + "> SECUENCIA: ");
        for (char c : sec) sb.append(c).append(' ');
        System.out.println(sb + RESET);

        // ── Cuenta atrás ─────────────────────────────────────────────
        // Polling de System.in.available() en el hilo principal: detecta Enter
        // sin tocar el Scanner desde otro hilo (evita corrupción de buffer).
        long fin = System.currentTimeMillis() + SEGUNDOS_SECUENCIA * 1000L;
        boolean ocultadoManual = false;
        while (System.currentTimeMillis() < fin) {
            long restantes = (fin - System.currentTimeMillis() + 999) / 1000;
            System.out.print("\r  " + AMARILLO + "Se oculta en " + restantes
                    + "s  [Enter para ocultar ya]" + RESET + "   ");
            System.out.flush();
            try {
                if (System.in.available() > 0) {
                    // consumir todos los bytes pendientes (la línea del Enter)
                    int b; do { b = System.in.read(); } while (b != -1 && b != '\n');
                    ocultadoManual = true;
                    break;
                }
                Thread.sleep(200);
            } catch (Exception ignored) { break; }
        }
        // Salto de línea tras el \r del countdown + mensaje si expiró el tiempo
        System.out.println();
        if (!ocultadoManual) {
            System.out.println("  " + ROJO_INT + NEGRITA + "Tiempo agotado!" + RESET);
            try { Thread.sleep(500); } catch (Exception ignored) {}
        }
        limpiarConsola();

        System.out.println("  " + color + NEGRITA + "[" + jugador + "] - " + personaje + RESET);
        System.out.println("  " + ROJO_INT + NEGRITA + "*** ESCRIBE LA SECUENCIA DE MEMORIA (mayusculas) ***" + RESET);
        System.out.print("  " + color + "> (" + sec.length + " caracteres, sin espacios): " + RESET);
        // toUpperCase: el jugador puede escribir en minusculas y se acepta igual
        String input = sc.nextLine().trim().toUpperCase();
        if (input.length() != sec.length) {
            System.out.println("  " + ROJO + "Longitud incorrecta (se esperaban " + sec.length + ")." + RESET);
            return false;
        }
        for (int i = 0; i < sec.length; i++) {
            if (input.charAt(i) != sec[i]) {
                System.out.print("  " + ROJO + "Error en pos " + (i+1) + ". Secuencia correcta: ");
                for (char c : sec) System.out.print(c);
                System.out.println(RESET);
                return false;
            }
        }
        System.out.println("  " + VERDE_INT + NEGRITA + "Correcto!" + RESET);
        return true;
    }

    private static String nombreJugador(Personaje p) {
        if (equipoActual1 != null && equipoActual1.contains(p)) return nombreJ1;
        if (equipoActual2 != null && equipoActual2.contains(p)) return nombreJ2;
        return "Jugador ?";
    }

    private static Personaje primero(List<Personaje> l) {
        return l.stream().filter(p -> p != null && p.estaVivo()).findFirst().orElse(null);
    }

    private static boolean equipoVivo(List<Personaje> l) {
        return l.stream().anyMatch(p -> p != null && p.estaVivo());
    }

    // ═════════════════════════════════════════════════════════════════
    //  PERSONAJES ESPECIALES
    // ═════════════════════════════════════════════════════════════════

    /**
     * Devuelve una copia fresca de Mahoraga cargada desde personajes.csv (especial=true).
     * Lanza RuntimeException si la fila no existe en el CSV.
     */
    private static Personaje mahoraga() {
        return catalogo.stream()
                .filter(p -> p.getNombre().equals("Mahoraga (General Divino)"))
                .findFirst()
                .map(Personaje::clonar)
                .orElseThrow(() -> new RuntimeException(
                        "Mahoraga (General Divino) no encontrado en personajes.csv. "
                                + "Aniade la fila con especial=true."));
    }

    private static void checkNaoya(List<Personaje> equipo, List<Personaje> rivales) {
        for (int i = 0; i < equipo.size(); i++) {
            Personaje p = equipo.get(i); if (p == null) continue;
            if (p.getNombre().equals("Naoya Zenin") && !p.estaVivo() && !p.ultimoGolpeFueEnergetico()) {
                System.out.println("\n  " + ROJO + NEGRITA + "Naoya Zenin cae... RENACE COMO MALDICION!" + RESET);
                // Forma transformada cargada desde personajes.csv (especial=true)
                Personaje template = catalogo.stream()
                        .filter(c -> c.getNombre().equals("Naoya Zenin (Maldicion)"))
                        .findFirst()
                        .orElse(null);
                if (template == null) {
                    System.out.println("  " + ROJO + "Naoya Zenin (Maldicion) no encontrado en personajes.csv: "
                            + "Naoya no puede transformarse." + RESET);
                    return;
                }
                equipo.set(i, template.clonar());
                if (equipoActual1 == equipo) equipoActual1 = equipo;
                if (equipoActual2 == equipo) equipoActual2 = equipo;
                return;
            }
        }
    }
}