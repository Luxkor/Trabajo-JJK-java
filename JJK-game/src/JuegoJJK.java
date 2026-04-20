import java.util.*;
import java.io.*;

public class JuegoJJK {

    // ── Rutas CSV (carpeta data/) ─────────────────────────────────────
    private static final String CSV_PERSONAJES = "data" + File.separator + "personajes.csv";
    private static final String CSV_CRIMENES   = "data" + File.separator + "crimenes.csv";

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

    // ── Colores ANSI ──────────────────────────────────────────────────
    static final String RESET       = "\u001B[0m";
    static final String NEGRITA     = "\u001B[1m";
    static final String ROJO        = "\u001B[31m";
    static final String VERDE       = "\u001B[32m";
    static final String AMARILLO    = "\u001B[33m";
    static final String AZUL        = "\u001B[34m";
    static final String MAGENTA     = "\u001B[35m";
    static final String CYAN        = "\u001B[36m";
    static final String BLANCO      = "\u001B[37m";
    static final String ROJO_INT    = "\u001B[91m";
    static final String VERDE_INT   = "\u001B[92m";
    static final String AMARILLO_INT= "\u001B[93m";
    static final String AZUL_INT    = "\u001B[94m";
    static final String MAGENTA_INT = "\u001B[95m";
    static final String CYAN_INT    = "\u001B[96m";

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
                catalogo.add(p); n++;
            }
            if (n == 0) throw new Exception("personajes.csv no contiene personajes validos.");
            System.out.println(VERDE_INT + "  " + n + " personajes cargados." + RESET);
        }
    }

    private static void cargarCrimenes(String ruta) {
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
        } catch (Exception ignored) {}
        if (!lista.isEmpty()) {
            crimenes = lista.toArray(new String[0][]);
            System.out.println(VERDE_INT + "  " + lista.size() + " crimenes cargados." + RESET);
        } else { crimenesRespaldo(); }
    }

    private static BufferedReader utf8(String r) throws FileNotFoundException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(r), java.nio.charset.StandardCharsets.UTF_8));
    }
    private static int     parseInt (String s) { return Integer.parseInt(s.trim()); }
    private static boolean parseBool(String s) { return Boolean.parseBoolean(s.trim()); }

    // ═════════════════════════════════════════════════════════════════
    //  SELECCION DE EQUIPO  (con columnas HP y CE)
    // ═════════════════════════════════════════════════════════════════

    private static List<Personaje> seleccionarEquipo(int num) {
        String jugador = (num == 1) ? nombreJ1 : nombreJ2;
        String colorJ  = (num == 1) ? CYAN : AMARILLO;
        System.out.println("\n" + colorJ + NEGRITA
                + "  -- SELECCION: " + jugador.toUpperCase() + " (Equipo " + num + ") --" + RESET);
        System.out.println("  " + linea(64, AZUL));
        System.out.printf("  %s%-3s %-26s %-12s %6s %6s%s%n",
                AZUL_INT, "ID", "NOMBRE", "TIPO", "HP", "CE", RESET);
        System.out.println("  " + linea(64, AZUL));
        for (int i = 0; i < catalogo.size(); i++) {
            Personaje c = catalogo.get(i);
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
        System.out.print("  " + colorJ + "[" + jugador + "] ID del personaje: " + RESET);
        Personaje sel = catalogo.get(parseInt(sc.nextLine()));
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
            p.prepararTurno();
            if (p.turnosInmovilizado > 0) {
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
            case 2: p.ataqueBasico(primero(def)); checkNaoya(def, atac); return true;
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
        int habIdx = parseInt(sc.nextLine());
        if (habIdx < 0 || habIdx >= habs.size()) return false;
        Habilidad h = habs.get(habIdx);

        if (h.getNombre().equals("MAHORAGA")) {
            System.out.println("\n  Megumi invoca al General Divino Mahoraga!");
            atac.set(idx, mahoraga()); return true;
        }
        if (h.getNombre().equals("ESPADA DEL VERDUGO")) {
            Personaje obj = primero(def); if (obj != null) ejecutarPena(obj); return true;
        }

        boolean esDominio = h.getNombre().contains("EXPANSI") || h.getNombre().contains("IDLE DEATH")
                || h.getNombre().contains("AUTOENCARNACI") || h.getNombre().contains("ATA") && h.getNombre().contains("D DE LA");

        if (esDominio) {
            Personaje rival = primero(def);
            if (h.getNombre().contains("TRIBUNAL MALDITO")) {
                resolverAcciones(p, habIdx, rival, new int[]{0,-1}, atac, def, idx, todos);
            } else {
                resolverAcciones(p, habIdx, rival, pedirRespuesta(rival, jugDef), atac, def, idx, todos);
            }
        } else { p.usarHabilidad(habIdx, primero(def)); }
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
                    int h = parseInt(sc.nextLine());
                    if (h >= 0 && h < habs.size()) return new int[]{1, h};
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
                || rival.getHabilidades().get(resp[1]).getNombre().contains("ATA") && rival.getHabilidades().get(resp[1]).getNombre().contains("D DE LA"));

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
            int[] sA = genSec(lon), sD = genSec(lon);
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
                || h.getNombre().contains("ATA") && h.getNombre().contains("D DE LA"));
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
        List<Integer> otros = new ArrayList<>();
        for (int k = 0; k < crimenes.length; k++) if (k != idx) otros.add(k);
        Collections.shuffle(otros);
        opts.add(crimenes[otros.get(0)][1]); opts.add(crimenes[otros.get(1)][1]);
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

    private static int[] genSec(int n) {
        int[] s = new int[n]; for (int i = 0; i < n; i++) s[i] = (int)(Math.random() * 4) + 1; return s;
    }

    private static boolean secuencia(int[] sec, String personaje, String jugador, String color) {
        System.out.println("\n  " + color + NEGRITA + "[" + jugador + "] - " + personaje + RESET);
        System.out.print("  Enter para ver tu secuencia..."); sc.nextLine();
        StringBuilder sb = new StringBuilder("  " + color + NEGRITA + "> ");
        for (int d : sec) sb.append(d).append(" ");
        System.out.println(sb + RESET);
        System.out.print("  Memorizala y pulsa Enter..."); sc.nextLine();
        for (int i = 0; i < 8; i++) System.out.println();
        System.out.println("  " + ROJO_INT + NEGRITA + "*** ESCRIBE LOS DIGITOS ***" + RESET);
        System.out.print("  " + color + "> (" + sec.length + " digitos, sin espacios): " + RESET);
        String input = sc.nextLine().trim();
        if (input.length() != sec.length) { System.out.println("  " + ROJO + "Longitud incorrecta." + RESET); return false; }
        for (int i = 0; i < sec.length; i++) {
            if ((input.charAt(i) - '0') != sec[i]) {
                System.out.print("  " + ROJO + "Error en pos " + (i+1) + ". Correcta: ");
                for (int d : sec) System.out.print(d); System.out.println(RESET); return false;
            }
        }
        System.out.println("  " + VERDE_INT + NEGRITA + "Correcto!" + RESET); return true;
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

    private static Personaje mahoraga() {
        Personaje m = new Maldicion("Mahoraga (General Divino)", 800, 500);
        m.addHabilidad(new Habilidad("Golpe Fisico",       "Ataque bruto",                      50,  0, Efecto.Tipo.NORMAL, true));
        m.addHabilidad(new Habilidad("Adaptacion",         "Regeneracion rapida",                0, 50, Efecto.Tipo.NORMAL, false));
        m.addHabilidad(new Habilidad("Tajo de Exterminio", "Instakill a cualquier Maldicion",  150,100, Efecto.Tipo.NORMAL, true));
        m.addHabilidad(new Habilidad("Rafaga de golpes",   "Multigolpe",                        70, 20, Efecto.Tipo.NORMAL, true));
        m.addHabilidad(new Habilidad("Embestida pesada",   "Danio puro",                        90, 30, Efecto.Tipo.NORMAL, true));
        return m;
    }

    private static void checkNaoya(List<Personaje> equipo, List<Personaje> rivales) {
        for (int i = 0; i < equipo.size(); i++) {
            Personaje p = equipo.get(i); if (p == null) continue;
            if (p.getNombre().equals("Naoya Zenin") && !p.estaVivo() && !p.ultimoGolpeFueEnergetico()) {
                System.out.println("\n  " + ROJO + NEGRITA + "Naoya Zenin cae... RENACE COMO MALDICION!" + RESET);
                Personaje nm = new Maldicion("Naoya Zenin (Maldicion)", 550, 0);
                nm.addHabilidad(new Habilidad("Vortice Maldito",        "Aire corrompido con CE.",              95,0,Efecto.Tipo.NORMAL,    true));
                nm.addHabilidad(new Habilidad("Ventilacion: Torbellino","Espiral de odio.",                    115,0,Efecto.Tipo.NORMAL,    true));
                nm.addHabilidad(new Habilidad("Barrera Sonica Maldita", "Inmoviliza 2 turnos.",                 75,0,Efecto.Tipo.ATURDIDO,  true));
                nm.addHabilidad(new Habilidad("Orgullo del Clan Zenin", "+100 HP y potenciado 2 turnos.",        0,0,Efecto.Tipo.POTENCIADO,false));
                nm.addHabilidad(new Habilidad("TORMENTA FINAL: RENCOR", "No puede bloquearse ni esquivarse.",  190,0,Efecto.Tipo.NORMAL,    true));
                equipo.set(i, nm);
                if (equipoActual1 == equipo) equipoActual1 = equipo;
                if (equipoActual2 == equipo) equipoActual2 = equipo;
                return;
            }
        }
    }

    private static void crimenesRespaldo() {
        crimenes = new String[][]{
            {"uso no autorizado de tecnica maldita en zona residencial",
             "La tecnica fue activada involuntariamente al contacto con una maldicion de grado 2.", "1"},
            {"participacion en mision de exorcismo sin acreditacion vigente",
             "La acreditacion estaba en renovacion y actue bajo orden verbal de un supervisor.", "1"},
            {"destruccion de infraestructura del Colegio Tecnico durante entrenamiento",
             "Fue consecuencia de un ataque no provocado por otro alumno; actue en defensa propia.", "1"},
            {"colaboracion con Kenjaku para suprimir la barrera de Shibuya",
             "Fui manipulado mediante sustitucion de cuerpo; mis acciones no respondian a mi voluntad.", "2"},
            {"liberacion del contenedor de Ryomen Sukuna durante combate activo",
             "El contenedor fue danado por una maldicion de grado especial, no por accion propia.", "2"},
            {"traicion al Colegio al facilitar informacion al Clan Kamo disidente",
             "La informacion se transmitio bajo coercion mientras mis companeros eran retenidos.", "2"},
            {"masacre del Hospital Eisei durante Shibuya bajo control de Sukuna",
             "El cuerpo fue tomado involuntariamente por Sukuna; no existe intencionalidad de mi parte.", "3"},
            {"conspiracion con Kenjaku para someter a la humanidad mediante Tengen",
             "No existe prueba fisica de participacion activa; las ordenes vinieron de una entidad externa.", "3"},
            {"apertura del Juego de la Culpa con mas de mil hechiceros muertos",
             "La acusacion carece de testigos validos y la evidencia fue recopilada dentro del Juego.", "3"}
        };
        System.out.println(AMARILLO + "  crimenes.csv no encontrado; usando respaldo." + RESET);
    }
}
