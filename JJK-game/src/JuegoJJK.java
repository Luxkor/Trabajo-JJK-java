import java.util.*;
import java.io.*;

public class JuegoJJK {
    private static List<Personaje> catalogo = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);
    private static Personaje duenyoDominioActual = null;
    private static int turnosRestantesDominio = 0;

    private static String nombreJugador1 = "Jugador 1";
    private static String nombreJugador2 = "Jugador 2";

    // ── Sistema de música ─────────────────────────────────────────────
    // MusicaAudio reproduce el WAV real en bucle via javax.sound.sampled.
    // Si el archivo no existe, cae a solo-anuncio sin interrumpir la partida.
    private static final MusicaJJK musica = new MusicaJJK.MusicaAudio();

    // ── Rutas CSV ─────────────────────────────────────────────────────
    private static final String RUTA_CSV_PERSONAJES = "personajes.csv";
    private static final String RUTA_CSV_CRIMENES   = "crimenes.csv";

    // ── Crímenes del Tribunal (cargados desde crimenes.csv) ──────────
    // [i][0] = texto del crimen | [i][1] = defensa correcta | [i][2] = gravedad ("1","2","3")
    private static String[][] crimenesTribunal = null;

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

    // ── Helpers de UI ─────────────────────────────────────────────────

    static String barraHP(int actual, int maximo) {
        int bloques = 20;
        int llenos  = Math.max(0, Math.min(bloques, (int)((double) actual / maximo * bloques)));
        String color = llenos > 12 ? VERDE_INT : llenos > 6 ? AMARILLO_INT : ROJO_INT;
        StringBuilder sb = new StringBuilder(color + "[");
        for (int i = 0; i < bloques; i++) sb.append(i < llenos ? "█" : "░");
        return sb.append("] " + actual + "/" + maximo + RESET).toString();
    }

    static String barraEnergia(int actual, int maximo) {
        if (maximo == 0) return AZUL + "[Sin energía maldita]" + RESET;
        int bloques = 15;
        int llenos  = Math.max(0, Math.min(bloques, (int)((double) actual / maximo * bloques)));
        StringBuilder sb = new StringBuilder(AZUL + "[");
        for (int i = 0; i < bloques; i++) sb.append(i < llenos ? "▰" : "▱");
        return sb.append("] " + actual + "/" + maximo + RESET).toString();
    }

    static String linea(int ancho, String color) {
        return color + "═".repeat(ancho) + RESET;
    }

    static void cajaTitulo(String titulo, String colorBorde, String colorTexto) {
        int ancho   = 52;
        int padding = (ancho - titulo.length()) / 2;
        System.out.println(colorBorde + "╔" + "═".repeat(ancho) + "╗" + RESET);
        System.out.println(colorBorde + "║" + RESET + " ".repeat(padding) + colorTexto + NEGRITA + titulo + RESET
                + " ".repeat(ancho - padding - titulo.length()) + colorBorde + "║" + RESET);
        System.out.println(colorBorde + "╚" + "═".repeat(ancho) + "╝" + RESET);
    }

    // ─────────────────────────────────────────────────────────────────
    //  MAIN
    // ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        String opcion = "";

        System.out.println("\n" + MAGENTA + NEGRITA);
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║        JUJUTSU KAISEN  —  BATTLE SYSTEM          ║");
        System.out.println("  ║                    v 4.1                         ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝" + RESET);

        System.out.print(CYAN    + "  🎮 Nombre del Jugador 1: " + RESET);
        String n1 = sc.nextLine().trim(); if (!n1.isEmpty()) nombreJugador1 = n1;
        System.out.print(AMARILLO + "  🎮 Nombre del Jugador 2: " + RESET);
        String n2 = sc.nextLine().trim(); if (!n2.isEmpty()) nombreJugador2 = n2;
        System.out.println(VERDE_INT + "\n  ¡Bienvenidos, " + NEGRITA + nombreJugador1 + RESET
                + VERDE_INT + " y " + NEGRITA + nombreJugador2 + RESET + VERDE_INT + "!" + RESET);

        do {
            System.out.println("\n" + MAGENTA + NEGRITA);
            System.out.println("  ╔══════════════════════════════════════════════════╗");
            System.out.println("  ║        JUJUTSU KAISEN  —  BATTLE SYSTEM          ║");
            System.out.println("  ╚══════════════════════════════════════════════════╝" + RESET);
            System.out.println("  " + CYAN   + "J1: " + NEGRITA + nombreJugador1 + RESET + "   "
                    + AMARILLO + "J2: " + NEGRITA + nombreJugador2 + RESET);
            System.out.println("  " + linea(50, AZUL));
            System.out.println("  " + VERDE_INT   + "1." + RESET + " JUGAR PARTIDA");
            System.out.println("  " + AMARILLO_INT + "2." + RESET + " ELEGIR MÚSICA DE FONDO");
            System.out.println("  " + ROJO_INT     + "3." + RESET + " SALIR");
            System.out.print("\n  " + BLANCO + "▶ Selecciona una opción: " + RESET);
            opcion = sc.nextLine();

            if      (opcion.equals("1")) comenzarPartida();
            else if (opcion.equals("2")) musica.mostrarMenuSeleccion(sc);
            else if (!opcion.equals("3")) System.out.println("  " + ROJO + "Opción no válida." + RESET);
        } while (!opcion.equals("3"));

        musica.detener();  // limpieza al cerrar el juego
        System.out.println(CYAN + "\n  Gracias por jugar. ¡Adiós!" + RESET);
    }

    // ─────────────────────────────────────────────────────────────────
    //  PARTIDA
    // ─────────────────────────────────────────────────────────────────
    private static void comenzarPartida() {
        try {
            catalogo.clear();
            cargarPersonajesDesdeCSV(RUTA_CSV_PERSONAJES);
            cargarCrimenesDesdeCSV(RUTA_CSV_CRIMENES);

            duenyoDominioActual  = null;
            turnosRestantesDominio = 0;
            penaDeMusertActiva   = false;
            golpesPenaDeMusert   = 0;
            espadaVerdugoActiva  = false;

            List<Personaje> equipo1 = seleccionarEquipo(1, 1);
            List<Personaje> equipo2 = seleccionarEquipo(2, 1);
            equipoActual1 = equipo1;
            equipoActual2 = equipo2;

            musica.reproducir();   // ← canción suena al inicio del combate

            while (equipoVivo(equipo1) && equipoVivo(equipo2)) {
                ejecutarTurnoEquipo(equipo1, equipo2, "EQUIPO 1");
                if (equipoVivo(equipo2)) ejecutarTurnoEquipo(equipo2, equipo1, "EQUIPO 2");
            }

            musica.detener();      // ← música para al terminar el combate

            System.out.println("\n" + AMARILLO_INT + NEGRITA);
            System.out.println("  ╔══════════════════════════════════════════════════╗");
            System.out.println("  ║             ¡EL COMBATE HA TERMINADO!            ║");
            System.out.println("  ╚══════════════════════════════════════════════════╝" + RESET);
            String ganadorNombre, ganadorJugador;
            if (equipoVivo(equipo1)) { ganadorNombre = equipo1.get(0).getNombre(); ganadorJugador = nombreJugador1; }
            else                     { ganadorNombre = equipo2.get(0).getNombre(); ganadorJugador = nombreJugador2; }
            System.out.println("  " + AMARILLO_INT + "🏆 GANADOR: " + NEGRITA + ganadorNombre + RESET
                    + AMARILLO_INT + "  [" + ganadorJugador + "]" + RESET);
            System.out.print("\n  Presiona Enter para volver al menú...");
            sc.nextLine();

        } catch (Exception e) {
            System.out.println("Error en la partida: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  CARGA DE PERSONAJES DESDE CSV (37 columnas)
    //
    //  Columnas 0-33: igual que en la versión anterior.
    //  Columna 34: puede_curarse  (true/false)
    //  Columna 35: herramienta_maldita (true/false)
    //  Columna 36: sin_energia    (true/false)  → sustituye la lista esMakiOToji
    // ─────────────────────────────────────────────────────────────────
    private static void cargarPersonajesDesdeCSV(String ruta) throws Exception {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta),
                        java.nio.charset.StandardCharsets.UTF_8));
        String linea;
        boolean primeraLinea = true;
        int cargados = 0;

        while ((linea = br.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) continue;
            if (primeraLinea) { primeraLinea = false; continue; }

            String[] f = linea.split("\\|", -1);
            if (f.length < 37) {
                br.close();
                throw new Exception("Línea con " + f.length + " columnas (se necesitan 37). "
                        + "Personaje: " + f[0]);
            }

            String  nombre  = f[0].trim();
            boolean esMald  = f[1].trim().equalsIgnoreCase("MALDICION");
            int     vida    = Integer.parseInt(f[2].trim());
            int     energia = Integer.parseInt(f[3].trim());

            Personaje pers = esMald ? new Maldicion(nombre, vida, energia)
                                    : new Hechicero(nombre, vida, energia);

            // Habilidades (columnas 4-33)
            for (int i = 0; i < 5; i++) {
                int b = 4 + i * 6;
                pers.addHabilidad(new Habilidad(
                        f[b].trim(), f[b+1].trim(),
                        Integer.parseInt(f[b+2].trim()),
                        Integer.parseInt(f[b+3].trim()),
                        Efecto.Tipo.valueOf(f[b+4].trim()),
                        Boolean.parseBoolean(f[b+5].trim())
                ));
            }

            // Flags de comportamiento (columnas 34-36)
            pers.setCuracionDisponible (Boolean.parseBoolean(f[34].trim()));
            pers.setPortadorHerramienta(Boolean.parseBoolean(f[35].trim()));
            pers.setSinEnergiaMaldita  (Boolean.parseBoolean(f[36].trim()));

            catalogo.add(pers);
            cargados++;
        }
        br.close();
        if (cargados == 0) throw new Exception("personajes.csv no contiene personajes válidos.");
        System.out.println(VERDE_INT + "  ✓ " + cargados + " personajes cargados desde " + ruta + RESET);
    }

    // ─────────────────────────────────────────────────────────────────
    //  CARGA DE CRÍMENES DESDE CSV
    //  Formato: gravedad|crimen|defensa
    //  gravedad: "1" = LEVE, "2" = GRAVE, "3" = FATAL
    // ─────────────────────────────────────────────────────────────────
    private static void cargarCrimenesDesdeCSV(String ruta) {
        List<String[]> lista = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(ruta),
                            java.nio.charset.StandardCharsets.UTF_8));
            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (primeraLinea) { primeraLinea = false; continue; }
                String[] f = linea.split("\\|", -1);
                if (f.length >= 3) {
                    // [0]=crimen texto [1]=defensa correcta [2]=gravedad
                    lista.add(new String[]{f[1].trim(), f[2].trim(), f[0].trim()});
                }
            }
            br.close();
            if (!lista.isEmpty()) {
                crimenesTribunal = lista.toArray(new String[0][]);
                System.out.println(VERDE_INT + "  ✓ " + lista.size() + " crímenes cargados desde " + ruta + RESET);
                return;
            }
        } catch (Exception ignored) { /* usará el array de respaldo */ }

        // Respaldo hardcoded si el CSV falla
        crimenesTribunal = new String[][]{
            {"uso no autorizado de técnica maldita en zona residencial de Shibuya",
             "La técnica fue activada de manera involuntaria al contacto con una maldición de grado 2 que atacó a civiles.", "1"},
            {"participación en misión de exorcismo sin acreditación vigente del Consejo de Hechiceros",
             "La acreditación estaba en proceso de renovación y actué bajo orden verbal de un supervisor de rango superior.", "1"},
            {"destrucción de infraestructura del Colegio Técnico de Magia de Tokio durante entrenamiento",
             "El daño fue consecuencia directa de un ataque no provocado por parte de otro alumno; actué en defensa propia.", "1"},
            {"colaboración con el Plan de Vuelta de Kenjaku para suprimir la barrera de Shibuya",
             "Fui manipulado mediante técnica de sustitución de cuerpo; mis acciones no respondían a mi voluntad.", "2"},
            {"liberación deliberada del contenedor de maldición especial Ryomen Sukuna durante combate activo",
             "El contenedor fue dañado por el ataque de una maldición de grado especial, no por acción propia.", "2"},
            {"traición al Colegio Técnico de Magia al facilitar información clasificada al Clan Kamo disidente",
             "La información fue transmitida bajo coerción extrema mientras mis compañeros estaban retenidos como rehenes.", "2"},
            {"masacre del personal del Hospital Eisei durante el incidente de Shibuya bajo el control de Sukuna",
             "El cuerpo fue tomado por Ryomen Sukuna de forma involuntaria; no existe consciencia ni intencionalidad de mi parte en dichos actos.", "3"},
            {"conspiración con Kenjaku para ejecutar el Gran Juego y someter a la humanidad a la evolución forzada mediante Tengen",
             "No existe prueba física que demuestre mi participación activa; las órdenes vinieron de una entidad que habitó mi cuerpo sin consentimiento.", "3"},
            {"apertura del Juego de la Culpa que resultó en la muerte de más de mil hechiceros certificados en la Colonia de Tokio",
             "La acusación carece de testigos supervivientes vinculantes y toda evidencia fue recopilada dentro del propio Juego, lo que invalida su valor legal.", "3"}
        };
        System.out.println(AMARILLO + "  ⚠ crimenes.csv no encontrado; usando crímenes de respaldo." + RESET);
    }

    // ─────────────────────────────────────────────────────────────────
    //  GESTIÓN DEL DOMINIO
    // ─────────────────────────────────────────────────────────────────
    private static void gestionarDominio(List<Personaje> todos) {
        if (duenyoDominioActual == null) return;

        if (turnosRestantesDominio > 0 && duenyoDominioActual.estaVivo()) {
            turnosRestantesDominio--;
            System.out.println("\n" + MAGENTA + "  ┌─ DOMINIO ACTIVO: " + NEGRITA
                    + duenyoDominioActual.getNombre() + RESET + MAGENTA
                    + " — Turnos restantes: " + turnosRestantesDominio + " ─┐" + RESET);

            if (duenyoDominioActual.getNombre().equals("Sukuna")) {
                System.out.println("⚔️ Santuario Malévolo lanza cortes incesantes sobre el área...");
                for (Personaje p : todos)
                    if (p != duenyoDominioActual && p.estaVivo()) p.recibirDanioFijo(40);
            }
            if (duenyoDominioActual.getNombre().equals("Kenjaku")) {
                System.out.println("🌀 El Gran Juego absorbe la energía maldita del área...");
                for (Personaje p : todos)
                    if (p != duenyoDominioActual && p.estaVivo()) p.drenarEnergia(60);
            }
            for (Personaje p : todos)
                if (p != duenyoDominioActual) p.setDentroDeDominio(true);
        } else {
            System.out.println("\n" + MAGENTA + "  El dominio de " + NEGRITA
                    + duenyoDominioActual.getNombre() + RESET + MAGENTA + " se ha disipado." + RESET);
            duenyoDominioActual.setDominioActivo(false);
            duenyoDominioActual.setBurnout(2);
            for (Personaje p : todos) p.setDentroDeDominio(false);
            duenyoDominioActual = null;
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  TURNO DE UN EQUIPO
    // ─────────────────────────────────────────────────────────────────
    private static void ejecutarTurnoEquipo(List<Personaje> atacantes,
                                            List<Personaje> defensores,
                                            String nombreEq) {
        List<Personaje> todosEnCombate = new ArrayList<>(atacantes);
        todosEnCombate.addAll(defensores);
        gestionarDominio(todosEnCombate);

        String nombreJugadorAtacante = nombreEq.equals("EQUIPO 1") ? nombreJugador1 : nombreJugador2;
        String nombreJugadorDefensor = nombreEq.equals("EQUIPO 1") ? nombreJugador2 : nombreJugador1;

        for (int i = 0; i < atacantes.size(); i++) {
            Personaje p = atacantes.get(i);
            if (p == null || !p.estaVivo()) continue;

            p.prepararTurno();
            if (p.turnosInmovilizado > 0) {
                System.out.println("\n  " + ROJO + NEGRITA + "TURNO OMITIDO: " + RESET + ROJO
                        + p.getNombre() + " — inmovilizado por " + NEGRITA + p.getCausaInmovilizacion() + RESET);
                continue;
            }

            String colorJ = nombreEq.equals("EQUIPO 1") ? CYAN : AMARILLO;
            System.out.println("\n" + colorJ + NEGRITA);
            System.out.println("  ╔══════════════════════════════════════════════════╗");
            System.out.println("  ║  TURNO: " + p.getNombre() + RESET + colorJ
                    + " ".repeat(Math.max(1, 41 - p.getNombre().length())) + "║");
            System.out.println("  ║  🎮 " + nombreJugadorAtacante + " (" + nombreEq + ")"
                    + " ".repeat(Math.max(1, 42 - nombreJugadorAtacante.length() - nombreEq.length())) + "║");
            System.out.println("  ╚══════════════════════════════════════════════════╝" + RESET);
            System.out.println("  ❤  HP:  " + barraHP(p.getVida(), p.getMaxVida()));
            if (p.getMaxEnergia() > 0)
                System.out.println("  ⚡ CE:  " + barraEnergia(p.getEnergia(), p.getMaxEnergia()));

            boolean turnoValido = false;
            while (!turnoValido) {
                System.out.println("\n  " + linea(50, AZUL));
                System.out.println("  " + VERDE_INT + "1." + RESET + " Habilidades Especiales");
                System.out.println("  " + VERDE_INT + "2." + RESET + " Ataque Básico (Físico)");
                System.out.println("  " + VERDE_INT + "3." + RESET + " Guardia");
                if (p.puedeUsarEspeciales()) System.out.println("  " + VERDE_INT + "4." + RESET + " Recargar Energía");
                if (p.puedeCurarse())        System.out.println("  " + VERDE_INT + "5." + RESET + " Curarse (RCT / Regeneración)");
                System.out.print("  " + BLANCO + "▶ Acción: " + RESET);
                try {
                    int accion = Integer.parseInt(sc.nextLine());
                    switch (accion) {
                        case 1:
                            List<Habilidad> habs = p.getHabilidades();
                            System.out.println("\n  " + CYAN + NEGRITA + "── Habilidades de " + p.getNombre() + " ──" + RESET);
                            for (int j = 0; j < habs.size(); j++) {
                                String costeStr = habs.get(j).getCosteEnergia() > 0
                                        ? AZUL + " (CE: " + habs.get(j).getCosteEnergia() + ")" + RESET
                                        : VERDE + " (Sin coste)" + RESET;
                                System.out.println("  " + AMARILLO_INT + j + "." + RESET + " " + habs.get(j).getNombre() + costeStr);
                            }
                            System.out.print("  " + BLANCO + "▶ Habilidad (0-" + (habs.size()-1) + ") o -1 para volver: " + RESET);
                            int habElegida = Integer.parseInt(sc.nextLine());
                            if (habElegida >= 0 && habElegida < habs.size()) {
                                Habilidad h = habs.get(habElegida);
                                if (h.getNombre().equals("MAHORAGA")) {
                                    System.out.println("\nMegumi une sus manos y canta: 'Con este tesoro invoco... ¡Al General Divino Mahoraga!'");
                                    atacantes.set(i, crearMahoraga());
                                    turnoValido = true; break;
                                }
                                if (h.getNombre().equals("ESPADA DEL VERDUGO")) {
                                    Personaje obj = buscarPrimerVivo(defensores);
                                    if (obj != null) ejecutarAtaquePenaDeMusert(obj);
                                    turnoValido = true; break;
                                }
                                boolean esDominio = h.getNombre().contains("EXPANSIÓN")
                                        || h.getNombre().contains("IDLE DEATH")
                                        || h.getNombre().contains("AUTOENCARNACIÓN")
                                        || h.getNombre().contains("ATAÚD");
                                if (esDominio) {
                                    Personaje rival = buscarPrimerVivo(defensores);
                                    if (h.getNombre().contains("TRIBUNAL MALDITO")) {
                                        resolverAcciones(p, habElegida, rival, new int[]{0,-1}, atacantes, defensores, i, todosEnCombate);
                                    } else {
                                        int[] accionRival = pedirAccionRival(rival, nombreJugadorDefensor, atacantes);
                                        resolverAcciones(p, habElegida, rival, accionRival, atacantes, defensores, i, todosEnCombate);
                                    }
                                } else {
                                    p.usarHabilidad(habElegida, buscarPrimerVivo(defensores));
                                }
                                comprobarTransformacionNaoya(defensores, atacantes);
                                comprobarTransformacionNaoya(atacantes,  defensores);
                                turnoValido = true;
                            }
                            break;
                        case 2:
                            p.ataqueBasico(buscarPrimerVivo(defensores));
                            comprobarTransformacionNaoya(defensores, atacantes);
                            turnoValido = true; break;
                        case 3:
                            p.setDefensa(true);
                            System.out.println(p.getNombre() + " se pone en guardia.");
                            turnoValido = true; break;
                        case 4:
                            if (p.puedeUsarEspeciales()) { p.recargarEnergia(); turnoValido = true; }
                            else System.out.println("Este personaje no usa energía maldita.");
                            break;
                        case 5:
                            if (p.puedeCurarse()) { p.curarse(); turnoValido = true; }
                            else System.out.println("Este personaje no puede curarse.");
                            break;
                        default:
                            System.out.println("Acción no reconocida.");
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage() + ". Intenta de nuevo.");
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  DOMINIO: respuesta del rival, choque, activación
    // ─────────────────────────────────────────────────────────────────
    private static int[] pedirAccionRival(Personaje rival, String nombreRival, List<Personaje> equipoRival) {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("  ⚠️  ¡DOMINIO DECLARADO! El rival expande su técnica.");
        System.out.println("  🎮 [" + nombreRival + "] — " + rival.getNombre() + " debe responder AHORA.");
        System.out.println("  HP: " + rival.getVida() + " | ENERGÍA: " + rival.getEnergia());
        System.out.println("╚══════════════════════════════════════════════════╝");
        while (true) {
            System.out.println("1. Habilidades Especiales\n2. Ataque Básico\n3. Guardia");
            if (rival.puedeUsarEspeciales()) System.out.println("4. Recargar Energía");
            if (rival.puedeCurarse())        System.out.println("5. Curarse");
            System.out.print("[" + nombreRival + "] Elige tu respuesta: ");
            try {
                int accion = Integer.parseInt(sc.nextLine());
                if (accion == 1) {
                    List<Habilidad> habs = rival.getHabilidades();
                    for (int j = 0; j < habs.size(); j++)
                        System.out.println("  " + j + ". " + habs.get(j).getNombre() + " (Coste: " + habs.get(j).getCosteEnergia() + ")");
                    System.out.print("  Elige habilidad (0-" + (habs.size()-1) + ") o -1 para volver: ");
                    int hab = Integer.parseInt(sc.nextLine());
                    if (hab >= 0 && hab < habs.size()) return new int[]{1, hab};
                } else if (accion == 2) { return new int[]{2,-1};
                } else if (accion == 3) { return new int[]{3,-1};
                } else if (accion == 4 && rival.puedeUsarEspeciales()) { return new int[]{4,-1};
                } else if (accion == 5 && rival.puedeCurarse())        { return new int[]{5,-1};
                } else { System.out.println("Acción no válida."); }
            } catch (NumberFormatException e) { System.out.println("Entrada inválida."); }
        }
    }

    private static void resolverAcciones(Personaje atacante, int habIdx,
                                         Personaje rival, int[] accionRival,
                                         List<Personaje> eqAtacante, List<Personaje> eqRival,
                                         int idxAtacante, List<Personaje> todos) {
        Habilidad habAtacante = atacante.getHabilidades().get(habIdx);

        if (habAtacante.getNombre().contains("TRIBUNAL MALDITO")) {
            System.out.println("\n══════════════ RESOLUCIÓN SIMULTÁNEA ══════════════");
            duenyoDominioActual = atacante;
            atacante.setDominioActivo(true);
            turnosRestantesDominio = 1;
            for (Personaje p2 : todos)
                if (p2 != atacante && !p2.isSinEnergiaMaldita()) p2.setDentroDeDominio(true);
            System.out.println("⚖️  ¡" + atacante.getNombre() + " EXPANDE EL TRIBUNAL MALDITO!");
            System.out.println("   El espacio colapsa en una sala de juicios. Judgeman aparece.");
            ejecutarJuicioTribunal(rival, resolverNombreJugador(rival, todos));
            duenyoDominioActual.setDominioActivo(false);
            duenyoDominioActual.setBurnout(2);
            for (Personaje p2 : todos) p2.setDentroDeDominio(false);
            duenyoDominioActual = null;
            turnosRestantesDominio = 0;
            System.out.println("════════════════════════════════════════════════════");
            return;
        }

        boolean rivalLanzaDominio = false;
        if (accionRival[0] == 1 && accionRival[1] >= 0 && rival.estaVivo()) {
            Habilidad hr = rival.getHabilidades().get(accionRival[1]);
            rivalLanzaDominio = hr.getNombre().contains("EXPANSIÓN") || hr.getNombre().contains("IDLE DEATH")
                    || hr.getNombre().contains("AUTOENCARNACIÓN") || hr.getNombre().contains("ATAÚD");
        }

        System.out.println("\n══════════════ RESOLUCIÓN SIMULTÁNEA ══════════════");
        if (rivalLanzaDominio) {
            System.out.println("⚡ ¡Ambos hechiceros expanden su dominio al mismo tiempo!");
            duenyoDominioActual = atacante;
            atacante.setDominioActivo(true);
            turnosRestantesDominio = 4;
            for (Personaje p2 : todos) if (p2 != atacante) p2.setDentroDeDominio(true);
            manejarChoqueDominio(rival, todos);
            try { atacante.usarHabilidad(habIdx, rival); } catch (Exception ex) { System.out.println(ex.getMessage()); }
        } else {
            manejarChoqueDominio(atacante, todos);
            try { atacante.usarHabilidad(habIdx, rival); } catch (Exception ex) { System.out.println(ex.getMessage()); }
            System.out.println("\n--- Acción de respuesta de " + rival.getNombre() + " ---");
            ejecutarAccionSimple(rival, accionRival, eqAtacante, eqRival, idxAtacante);
        }
        System.out.println("════════════════════════════════════════════════════");
    }

    private static void ejecutarAccionSimple(Personaje p, int[] accion,
                                             List<Personaje> eqObjetivo, List<Personaje> eqPropio,
                                             int idxPropio) {
        try {
            switch (accion[0]) {
                case 1:
                    Habilidad h = p.getHabilidades().get(accion[1]);
                    if (h.getNombre().equals("MAHORAGA")) {
                        System.out.println("\nMegumi une sus manos y canta: 'Con este tesoro invoco... ¡Al General Divino Mahoraga!'");
                        eqPropio.set(idxPropio, crearMahoraga());
                    } else { p.usarHabilidad(accion[1], buscarPrimerVivo(eqObjetivo)); }
                    break;
                case 2: p.ataqueBasico(buscarPrimerVivo(eqObjetivo)); break;
                case 3: p.setDefensa(true); System.out.println(p.getNombre() + " se pone en guardia."); break;
                case 4: if (p.puedeUsarEspeciales()) p.recargarEnergia(); break;
                case 5: if (p.puedeCurarse()) p.curarse(); break;
            }
        } catch (Exception e) { System.out.println("Error acción rival: " + e.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────────────
    //  TRIBUNAL MALDITO
    // ─────────────────────────────────────────────────────────────────
    private static int     golpesPenaDeMusert  = 0;
    private static boolean penaDeMusertActiva  = false;
    private static boolean espadaVerdugoActiva = false;

    private static void ejecutarJuicioTribunal(Personaje acusado, String nombreJugador) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("  ⚖️  TRIBUNAL MALDITO — JUDGEMAN DICTA SENTENCIA");
        System.out.println("  Acusado: " + acusado.getNombre() + "  [" + nombreJugador + "]");
        System.out.println("╚══════════════════════════════════════════════════════╝");

        if (penaDeMusertActiva) { ejecutarAtaquePenaDeMusert(acusado); return; }

        int[] resultado  = celebrarJuicio(acusado.getNombre(), nombreJugador, "PRIMER JUICIO");
        int   gravedad   = resultado[0];
        boolean inocente = resultado[1] == 1;

        if (inocente) {
            System.out.println("  ✅ Judgeman escucha los argumentos...");
            System.out.println("  ⚖️  VEREDICTO: ¡INOCENTE! El cargo queda retirado.");
            System.out.println("  " + acusado.getNombre() + " no sufre consecuencias este turno.");
            return;
        }

        System.out.println("  ❌ La defensa es insuficiente...");
        System.out.println("  ⚖️  VEREDICTO PROVISIONAL: ¡CULPABLE!");

        if (gravedad >= 2) {
            System.out.println("\n  ⚠️  La sentencia es grave. ¿Deseas solicitar un SEGUNDO JUICIO?");
            System.out.println("  Si ganas la apelación, la sentencia queda anulada.");
            System.out.println("  Si la pierdes, se aplica la sentencia original sin cambios.");
            System.out.print("  [" + nombreJugador + "] ¿Apelar? (s/n): ");
            String resp = sc.nextLine().trim().toLowerCase();
            if (resp.equals("s") || resp.equals("si") || resp.equals("sí")) {
                System.out.println("\n  📜 El acusado solicita un segundo juicio. Judgeman acepta.");
                int[] resultado2 = celebrarJuicio(acusado.getNombre(), nombreJugador, "APELACIÓN");
                if (resultado2[1] == 1) {
                    System.out.println("  ✅ La apelación prospera. Judgeman revisa el caso...");
                    System.out.println("  ⚖️  SENTENCIA ANULADA. " + acusado.getNombre() + " queda en libertad.");
                } else {
                    System.out.println("  ❌ La apelación fracasa. La sentencia original se confirma.");
                    aplicarVeredicto(acusado, gravedad);
                }
                return;
            }
        }
        aplicarVeredicto(acusado, gravedad);
    }

    /**
     * Selecciona un crimen aleatorio del array cargado desde crimenes.csv,
     * baraja las defensas y devuelve {gravedad, 1/0 (acertó/falló)}.
     */
    private static int[] celebrarJuicio(String nombrePersonaje, String nombreJugador, String etiqueta) {
        int idx      = (int)(Math.random() * crimenesTribunal.length);
        String crimen       = crimenesTribunal[idx][0];
        String defensaCorr  = crimenesTribunal[idx][1];
        int    gravedad     = Integer.parseInt(crimenesTribunal[idx][2]);

        String nivelStr = gravedad == 1 ? "⚪ LEVE" : gravedad == 2 ? "🟡 GRAVE" : "🔴 FATAL";
        System.out.println("\n  ── " + etiqueta + " ──");
        System.out.println("  Judgeman golpea su mazo.");
        System.out.println("  Cargo: " + nivelStr + " — " + crimen.toUpperCase());
        System.out.println("\n  [" + nombreJugador + "], elige la defensa de " + nombrePersonaje + ":");

        List<String> opciones = new ArrayList<>();
        opciones.add(defensaCorr);
        List<Integer> otrosIdx = new ArrayList<>();
        for (int k = 0; k < crimenesTribunal.length; k++) if (k != idx) otrosIdx.add(k);
        Collections.shuffle(otrosIdx);
        opciones.add(crimenesTribunal[otrosIdx.get(0)][1]);
        opciones.add(crimenesTribunal[otrosIdx.get(1)][1]);
        Collections.shuffle(opciones);

        int posCorrecta = opciones.indexOf(defensaCorr);
        for (int k = 0; k < opciones.size(); k++)
            System.out.println("  " + (k+1) + ". " + opciones.get(k));

        int eleccion = -1;
        while (eleccion < 1 || eleccion > 3) {
            System.out.print("  Tu defensa (1-3): ");
            try { eleccion = Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { eleccion = -1; }
        }
        return new int[]{gravedad, (eleccion-1) == posCorrecta ? 1 : 0};
    }

    private static void aplicarVeredicto(Personaje acusado, int gravedad) {
        if (gravedad <= 2) {
            System.out.println("\n  🔨 Judgeman ejecuta la CONFISCACIÓN.");
            if (acusado.tieneHerramientaMaldita()) {
                System.out.println("  📦 La herramienta maldita de " + acusado.getNombre() + " es destruida.");
                acusado.confiscarHerramienta();
            } else if (acusado.puedeUsarEspeciales()) {
                System.out.println("  🚫 La técnica maldita de " + acusado.getNombre() + " es sellada.");
                acusado.setBurnout(2);
            } else {
                System.out.println("  ⚡ La energía maldita de " + acusado.getNombre() + " es confiscada.");
                acusado.confiscarEnergia();
            }
        } else {
            System.out.println("\n  💀 Judgeman dicta la PENA DE MUERTE.");
            if (!espadaVerdugoActiva) {
                System.out.println("  ⚔️  ¡Higuruma obtiene la ESPADA DEL VERDUGO para el resto de la partida!");
                espadaVerdugoActiva = true;
                agregarEspadaVerdugoAHiguruma();
            } else {
                System.out.println("  ⚔️  Higuruma blande la Espada del Verdugo...");
            }
            penaDeMusertActiva = true;
            golpesPenaDeMusert = 0;
            ejecutarAtaquePenaDeMusert(acusado);
        }
    }

    private static void agregarEspadaVerdugoAHiguruma() {
        for (List<Personaje> eq : Arrays.asList(equipoActual1, equipoActual2)) {
            if (eq == null) continue;
            for (Personaje p : eq) {
                if (p != null && p.getNombre().equals("Hiromi Higuruma")) {
                    boolean yaLaTiene = p.getHabilidades().stream()
                            .anyMatch(h -> h.getNombre().equals("ESPADA DEL VERDUGO"));
                    if (!yaLaTiene)
                        p.addHabilidad(new Habilidad("ESPADA DEL VERDUGO",
                                "La sentencia de muerte otorgada por Judgeman. 1 impacto quita el 60% de vida; 2 matan al instante.",
                                0, 0, Efecto.Tipo.NORMAL, true));
                    return;
                }
            }
        }
    }

    static void ejecutarAtaquePenaDeMusert(Personaje acusado) {
        if (!espadaVerdugoActiva) return;
        System.out.println("\n  ⚔️  LA ESPADA DEL VERDUGO APUNTA A " + acusado.getNombre().toUpperCase());
        if (Math.random() < 0.30) {
            acusado.marcarUltimoGolpe(true);
            golpesPenaDeMusert++;
            if (golpesPenaDeMusert == 1) {
                int danio = (int)(acusado.getVida() * 0.60);
                System.out.println("  🩸 ¡LA ESPADA CONECTA! Primer golpe.");
                System.out.println("  " + acusado.getNombre() + " pierde el 60% de su vida (" + danio + " daño).");
                acusado.recibirDanioFijo(danio);
                System.out.println("  (Un segundo golpe sería letal.)");
            } else {
                System.out.println("  ☠️  ¡SEGUNDO GOLPE! La sentencia se cumple.");
                acusado.recibirDanioFijo(acusado.getVida());
                espadaVerdugoActiva = false; penaDeMusertActiva = false; golpesPenaDeMusert = 0;
            }
        } else {
            System.out.println("  💨 La espada falla... " + acusado.getNombre() + " esquiva la ejecución.");
        }
        if (turnosRestantesDominio == 0) penaDeMusertActiva = false;
    }

    // ─────────────────────────────────────────────────────────────────
    //  CHOQUE DE DOMINIOS
    // ─────────────────────────────────────────────────────────────────
    private static boolean tieneDominioPropio(Personaje p) {
        for (Habilidad h : p.getHabilidades()) {
            String n = h.getNombre();
            if (n.contains("EXPANSIÓN") || n.contains("IDLE DEATH")
                    || n.contains("AUTOENCARNACIÓN") || n.contains("ATAÚD")) return true;
        }
        return false;
    }

    private static void manejarChoqueDominio(Personaje atacante, List<Personaje> todos) {
        if (duenyoDominioActual == null || !duenyoDominioActual.estaVivo() || duenyoDominioActual == atacante) {
            System.out.println("\n⚡ ¡" + atacante.getNombre() + " EXPANDE SU DOMINIO!");
            activarDominio(atacante, todos);
            return;
        }

        Personaje defensor = duenyoDominioActual;

        // Personajes sin energía maldita (Maki / Toji) son inmunes al golpe seguro
        if (defensor.isSinEnergiaMaldita()) {
            System.out.println("\n⚡ ¡" + atacante.getNombre() + " intenta expandir su dominio!");
            System.out.println("💪 " + defensor.getNombre() + " carece de energía maldita: el golpe seguro falla.");
            System.out.println("   El dominio del atacante se expande igualmente.");
            defensor.setDentroDeDominio(false);
            activarDominio(atacante, todos);
            defensor.setDentroDeDominio(false);
            return;
        }

        if (!tieneDominioPropio(defensor)) {
            System.out.println("\n⚡ ¡" + atacante.getNombre() + " EXPANDE SU DOMINIO!");
            System.out.println("😱 " + defensor.getNombre() + " no posee técnica de dominio propia. Queda atrapado.");
            activarDominio(atacante, todos);
            return;
        }

        // Choque real: minijuego de secuencias
        String nombreAtacante = resolverNombreJugador(atacante, todos);
        String nombreDefensor = resolverNombreJugador(defensor, todos);

        System.out.println("\n" + MAGENTA_INT + NEGRITA);
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║            💥  CHOQUE DE DOMINIOS  💥            ║");
        System.out.println("  ║  " + centrar(atacante.getNombre() + "  VS  " + defensor.getNombre(), 48) + "║");
        System.out.println("  ╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println("\n  " + BLANCO + "▶  SECUENCIA DE TECLAS BAJO PRESIÓN" + RESET);
        System.out.println("  3 rondas — secuencias de 4 → 5 → 6 dígitos (independientes por jugador).");

        boolean aPrio = atacante.getNombre().equals("Sukuna") || atacante.getNombre().equals("Kenjaku");
        boolean dPrio = defensor.getNombre().equals("Sukuna")  || defensor.getNombre().equals("Kenjaku");
        int pA = aPrio ? 1 : 0, pD = dPrio ? 1 : 0;

        if (aPrio) System.out.println("\n  " + ROJO_INT + NEGRITA + "⚠  Dominio de " + atacante.getNombre() + " — densidad superior, +1 punto." + RESET);
        if (dPrio) System.out.println("\n  " + ROJO_INT + NEGRITA + "⚠  Dominio de " + defensor.getNombre() + " — densidad superior, +1 punto." + RESET);
        System.out.println("\n  " + AMARILLO + "En caso de empate gana el DEFENSOR." + RESET);
        System.out.print("  Pulsa Enter cuando estéis listos...");
        sc.nextLine();

        for (int ronda = 1; ronda <= 3; ronda++) {
            int lon = 3 + ronda;
            int[] sA = generarSecuencia(lon), sD = generarSecuencia(lon);
            System.out.println("\n  " + CYAN + NEGRITA + "══ RONDA " + ronda + " ══" + RESET
                    + CYAN + "  (" + lon + " dígitos)" + RESET);
            System.out.println("  " + CYAN + "[" + nombreAtacante + "] " + pA + "  —  " + pD + "  [" + nombreDefensor + "]" + RESET);
            boolean aA = turnoSecuencia(sA, atacante.getNombre(), nombreAtacante, CYAN);
            boolean aD = turnoSecuencia(sD, defensor.getNombre(),  nombreDefensor,  AMARILLO);
            if (aA) pA++; if (aD) pD++;
            System.out.print("  Ronda " + ronda + ": ");
            if (aA && aD)       System.out.println(VERDE + "Ambos acertaron." + RESET + "  (" + pA + "-" + pD + ")");
            else if (aA)        System.out.println(VERDE + "Punto para [" + nombreAtacante + "]" + RESET + "  (" + pA + "-" + pD + ")");
            else if (aD)        System.out.println(AMARILLO_INT + "Punto para [" + nombreDefensor + "]" + RESET + "  (" + pA + "-" + pD + ")");
            else                System.out.println(ROJO + "Ambos fallaron." + RESET + "  (" + pA + "-" + pD + ")");
        }

        System.out.println("\n  " + linea(50, MAGENTA));
        System.out.println("  " + MAGENTA_INT + NEGRITA + "RESULTADO FINAL" + RESET);
        System.out.println("  [" + CYAN + NEGRITA + nombreAtacante + RESET + "] " + pA
                + "  —  " + pD + "  [" + AMARILLO + NEGRITA + nombreDefensor + RESET + "]");
        System.out.println("  " + linea(50, MAGENTA));

        if (pA > pD) {
            System.out.println("  " + CYAN + NEGRITA + "🏆 [" + nombreAtacante + "] sobrepone su dominio!" + RESET);
            defensor.setDominioActivo(false); defensor.setBurnout(2);
            for (Personaje p : todos) p.setDentroDeDominio(false);
            activarDominio(atacante, todos);
        } else {
            System.out.println("  " + AMARILLO + NEGRITA + "🛡  [" + nombreDefensor + "] mantiene su dominio intacto." + RESET);
            atacante.setBurnout(2);
        }
        System.out.println("  " + linea(50, MAGENTA));
    }

    private static void activarDominio(Personaje atacante, List<Personaje> todos) {
        duenyoDominioActual = atacante;
        atacante.setDominioActivo(true);
        turnosRestantesDominio = 4;
        for (Personaje p : todos) {
            if (p != atacante) {
                if (!p.isSinEnergiaMaldita()) p.setDentroDeDominio(true);
                else System.out.println("  ⚔️  " + p.getNombre() + " resiste el golpe seguro del dominio.");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────
    private static String centrar(String texto, int ancho) {
        if (texto.length() >= ancho) return texto.substring(0, ancho);
        int izq = (ancho - texto.length()) / 2;
        return " ".repeat(izq) + texto + " ".repeat(ancho - texto.length() - izq);
    }

    private static int[] generarSecuencia(int longitud) {
        int[] seq = new int[longitud];
        for (int i = 0; i < longitud; i++) seq[i] = (int)(Math.random() * 4) + 1;
        return seq;
    }

    private static boolean turnoSecuencia(int[] sec, String nombrePersonaje, String nombreJugador, String color) {
        System.out.println("\n  " + color + NEGRITA + "🎮 [" + nombreJugador + "] — " + nombrePersonaje + RESET);
        System.out.print("  Pulsa Enter para ver tu secuencia secreta...");
        sc.nextLine();
        StringBuilder sb = new StringBuilder("  " + color + NEGRITA + "► ");
        for (int d : sec) sb.append(d).append(" ");
        System.out.println(sb + RESET);
        System.out.print("  Memorízala y pulsa Enter para ocultarla...");
        sc.nextLine();
        for (int i = 0; i < 8; i++) System.out.println();
        System.out.println("  " + ROJO_INT + NEGRITA + "*** SECUENCIA OCULTA — ESCRIBE LOS DÍGITOS ***" + RESET);
        System.out.print("  " + color + "▶ Tu secuencia (" + sec.length + " dígitos, sin espacios): " + RESET);
        String input = sc.nextLine().trim();
        if (input.length() != sec.length) { System.out.println("  " + ROJO + "✗ Longitud incorrecta." + RESET); return false; }
        for (int i = 0; i < sec.length; i++) {
            if ((input.charAt(i) - '0') != sec[i]) {
                System.out.print("  " + ROJO + "✗ Error en posición " + (i+1) + ". Secuencia correcta: ");
                for (int d : sec) System.out.print(d);
                System.out.println(RESET);
                return false;
            }
        }
        System.out.println("  " + VERDE_INT + NEGRITA + "✓ ¡Correcto!" + RESET);
        return true;
    }

    private static String resolverNombreJugador(Personaje p, List<Personaje> todos) {
        if (equipoActual1 != null && equipoActual1.contains(p)) return nombreJugador1;
        if (equipoActual2 != null && equipoActual2.contains(p)) return nombreJugador2;
        return "Jugador ?";
    }

    private static List<Personaje> equipoActual1 = null;
    private static List<Personaje> equipoActual2 = null;

    private static List<Personaje> seleccionarEquipo(int numEq, int cant) {
        List<Personaje> eq = new ArrayList<>();
        String nombreJugador = (numEq == 1) ? nombreJugador1 : nombreJugador2;
        String colorJ = (numEq == 1) ? CYAN : AMARILLO;
        System.out.println("\n" + colorJ + NEGRITA + "  ── SELECCIÓN: " + nombreJugador.toUpperCase() + " (Equipo " + numEq + ") ──" + RESET);
        System.out.println("  " + linea(50, AZUL));
        for (int i = 0; i < catalogo.size(); i++) {
            Personaje c = catalogo.get(i);
            boolean esMald = c instanceof Maldicion;
            System.out.printf("  %s%2d.%s %-28s %s%s%s%n",
                    AMARILLO_INT, i, RESET, c.getNombre(),
                    esMald ? ROJO : VERDE, esMald ? "[Maldición]" : "[Hechicero]", RESET);
        }
        System.out.println("  " + linea(50, AZUL));
        for (int j = 0; j < cant; j++) {
            System.out.print("  " + colorJ + "[" + nombreJugador + "] Elige tu personaje (ID): " + RESET);
            Personaje sel = catalogo.get(Integer.parseInt(sc.nextLine()));
            eq.add(sel);
            if (sel instanceof Combatiente) ((Combatiente) sel).manifestarAura();
        }
        return eq;
    }

    private static Personaje buscarPrimerVivo(List<Personaje> lista) {
        for (Personaje p : lista) if (p != null && p.estaVivo()) return p;
        return null;
    }

    private static boolean equipoVivo(List<Personaje> lista) {
        for (Personaje p : lista) if (p != null && p.estaVivo()) return true;
        return false;
    }

    // ─────────────────────────────────────────────────────────────────
    //  PERSONAJES ESPECIALES (creados programáticamente en combate)
    // ─────────────────────────────────────────────────────────────────
    private static Personaje crearMahoraga() {
        Personaje m = new Maldicion("Mahoraga (General Divino)", 800, 500);
        m.addHabilidad(new Habilidad("Golpe Físico",       "Ataque bruto",                                        50,  0, Efecto.Tipo.NORMAL, true));
        m.addHabilidad(new Habilidad("Adaptación",         "Se regenera rápidamente",                              0, 50, Efecto.Tipo.NORMAL, false));
        m.addHabilidad(new Habilidad("Tajo de Exterminio", "Elimina de un solo golpe a cualquier maldición",     150,100, Efecto.Tipo.NORMAL, true));
        m.addHabilidad(new Habilidad("Ráfaga de golpes",   "Multigolpe",                                          70, 20, Efecto.Tipo.NORMAL, true));
        m.addHabilidad(new Habilidad("Embestida pesada",   "Daño puro",                                           90, 30, Efecto.Tipo.NORMAL, true));
        return m;
    }

    private static void comprobarTransformacionNaoya(List<Personaje> equipo, List<Personaje> rivales) {
        for (int i = 0; i < equipo.size(); i++) {
            Personaje p = equipo.get(i);
            if (p == null) continue;
            if (p.getNombre().equals("Naoya Zenin") && !p.estaVivo() && !p.ultimoGolpeFueEnergetico()) {
                System.out.println("\n╔══════════════════════════════════════════════════╗");
                System.out.println("  💀 Naoya Zenin ha caído... pero no descansa en paz.");
                System.out.println("  ☠️  ¡NAOYA ZENIN RENACE COMO MALDICIÓN ESPECIAL!");
                System.out.println("╚══════════════════════════════════════════════════╝");
                equipo.set(i, crearNaoyaMaldicion());
                if (equipoActual1 == equipo) equipoActual1 = equipo;
                if (equipoActual2 == equipo) equipoActual2 = equipo;
                return;
            }
        }
    }

    private static Personaje crearNaoyaMaldicion() {
        Personaje n = new Maldicion("Naoya Zenin (Maldición)", 550, 0);
        n.addHabilidad(new Habilidad("Vórtice Maldito",            "Vórtice de aire corrompido con energía maldita.",               95,0,Efecto.Tipo.NORMAL,   true));
        n.addHabilidad(new Habilidad("Ventilación: Torbellino",    "Espiral de odio puro comprimido. Desgarra desde dentro.",       115,0,Efecto.Tipo.NORMAL,   true));
        n.addHabilidad(new Habilidad("Barrera Sónica Maldita",     "Inmoviliza 2 turnos con energía corrosiva.",                    75,0,Efecto.Tipo.ATURDIDO,  true));
        n.addHabilidad(new Habilidad("Orgullo del Clan Zenin",     "Potenciado 2 turnos y recupera 100 HP.",                        0, 0,Efecto.Tipo.POTENCIADO,false));
        n.addHabilidad(new Habilidad("TORMENTA FINAL: RENCOR ETERNO","No puede bloquearse ni esquivarse.",                         190,0,Efecto.Tipo.NORMAL,   true));
        return n;
    }
}
