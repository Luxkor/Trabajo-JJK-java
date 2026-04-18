import java.util.*;
import java.io.*;
import javax.sound.sampled.*;

/**
 * Clase abstracta que modela el sistema de música del juego.
 *
 * Se elige una clase ABSTRACTA porque:
 *  - Necesita mantener estado (lista de pistas, pista seleccionada).
 *  - Impone el contrato reproducir() que cada subclase cumple de forma distinta.
 *  - Permite añadir en el futuro más subclases sin tocar JuegoJJK.
 *
 * El catálogo de pistas se carga desde musica.csv (misma carpeta raíz
 * que personajes.csv). Si el CSV falta, se usa una lista de respaldo.
 *
 * Subclases disponibles:
 *  - MusicaConsola : solo anuncia la pista en pantalla (sin audio real).
 *  - MusicaAudio   : anuncia la pista Y reproduce el WAV en bucle mediante
 *                    javax.sound.sampled (parte del JDK estándar, sin dependencias externas).
 */
public abstract class MusicaJJK {

    // ── Rutas ─────────────────────────────────────────────────────────
    private static final String RUTA_CSV_MUSICA = "musica.csv";

    /**
     * Carpeta donde deben colocarse los archivos WAV.
     * Relativa al directorio de trabajo del proyecto (raíz del módulo en IntelliJ).
     * Documenta la ubicación exacta para el desarrollador.
     */
    public static final String CARPETA_MUSICA =
            "assets" + File.separator + "music" + File.separator;

    // ── Colores ANSI ──────────────────────────────────────────────────
    private static final String RESET       = "\u001B[0m";
    private static final String NEGRITA     = "\u001B[1m";
    private static final String MAGENTA_INT = "\u001B[95m";
    private static final String CYAN_INT    = "\u001B[96m";
    private static final String AMARILLO    = "\u001B[33m";
    private static final String VERDE       = "\u001B[32m";
    private static final String ROJO        = "\u001B[31m";
    private static final String BLANCO      = "\u001B[37m";
    private static final String AZUL        = "\u001B[34m";
    private static final String VERDE_INT   = "\u001B[92m";

    // ── Modelo de una pista ───────────────────────────────────────────
    protected static class Pista {
        final String titulo;
        final String artista;
        final String contexto;
        final String archivo;   // nombre del WAV en CARPETA_MUSICA; vacío = solo anuncio

        Pista(String titulo, String artista, String contexto, String archivo) {
            this.titulo   = titulo;
            this.artista  = artista;
            this.contexto = contexto;
            this.archivo  = (archivo != null) ? archivo.trim() : "";
        }

        @Override
        public String toString() {
            return NEGRITA + titulo + RESET + " — " + artista +
                    AZUL + "  [" + contexto + "]" + RESET;
        }
    }

    // ── Estado compartido ─────────────────────────────────────────────
    protected final List<Pista> pistas = new ArrayList<>();
    protected int pistaSeleccionada = 0;   // 0 = sin música

    // ── Constructor ───────────────────────────────────────────────────
    protected MusicaJJK() {
        inicializarPistas();
    }

    // ── Carga del catálogo ────────────────────────────────────────────

    /**
     * Carga el catálogo EXCLUSIVAMENTE desde musica.csv.
     * No existe lista de canciones hardcodeada: el CSV es la única fuente de verdad.
     *
     * Si el archivo no existe o está vacío, la lista de pistas queda vacía y el
     * menú mostrará solo la opción "Sin música", sin lanzar ningún error.
     *
     * Formato CSV (separador '|', primera línea no comentada = cabecera ignorada):
     *   titulo | artista | contexto | archivo
     */
    protected void inicializarPistas() {
        File csv = new File(RUTA_CSV_MUSICA);
        if (!csv.exists()) {
            // CSV no creado todavía — situación normal en un proyecto nuevo
            return;
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(csv),
                        java.nio.charset.StandardCharsets.UTF_8))) {

            String linea;
            boolean primeraLinea = true;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (primeraLinea) { primeraLinea = false; continue; }   // saltar cabecera

                String[] f = linea.split("\\|", -1);
                if (f.length < 3) {
                    System.out.println("  " + AMARILLO
                            + "  ⚠ Línea ignorada en musica.csv (se necesitan al menos 3 columnas): " + linea + RESET);
                    continue;
                }
                // Acepta dos formatos:
                //   3 columnas: titulo | contexto | archivo       (sin artista)
                //   4 columnas: titulo | artista  | contexto | archivo
                final String titulo, artista, contexto, archivo;
                if (f.length >= 4) {
                    titulo   = f[0].trim();
                    artista  = f[1].trim();
                    contexto = f[2].trim();
                    archivo  = f[3].trim();
                } else {
                    titulo   = f[0].trim();
                    artista  = "";
                    contexto = f[1].trim();
                    archivo  = f[2].trim();
                }
                pistas.add(new Pista(titulo, artista, contexto, archivo));
            }

        } catch (IOException e) {
            System.out.println("  " + AMARILLO
                    + "  ⚠ No se pudo leer musica.csv: " + e.getMessage() + RESET);
        }
    }

    // ── Contrato ──────────────────────────────────────────────────────

    /**
     * Reproduce (o anuncia) la pista seleccionada.
     * Solo debe llamarse al INICIAR un combate.
     */
    public abstract void reproducir();

    /**
     * Detiene la reproducción activa (si existe).
     * Implementación por defecto vacía; MusicaAudio la sobreescribe.
     */
    public void detener() { /* no-op por defecto */ }

    // ── Menú de selección ─────────────────────────────────────────────

    public void mostrarMenuSeleccion(Scanner sc) {
        System.out.println("\n  " + CYAN_INT + NEGRITA + "── BANDA SONORA — SELECCIÓN ──" + RESET);
        System.out.println("  " + AZUL + "═".repeat(50) + RESET);

        if (pistas.isEmpty()) {
            System.out.println("  " + AMARILLO + "  No hay canciones configuradas." + RESET);
            System.out.println("  " + AMARILLO + "  Edita musica.csv para añadir las tuyas." + RESET);
            System.out.println("  " + AMARILLO + "  Consulta assets/music/LEEME.md para instrucciones." + RESET);
            System.out.println("  " + AZUL + "═".repeat(50) + RESET);
            System.out.print("  Pulsa Enter para volver...");
            sc.nextLine();
            return;
        }

        for (int i = 0; i < pistas.size(); i++) {
            String marcador = (pistaSeleccionada == i + 1) ? VERDE + " ▶ " + RESET : "   ";
            System.out.println("  " + marcador + AMARILLO + (i + 1) + "." + RESET + " " + pistas.get(i));
        }
        String marcadorOff = (pistaSeleccionada == 0) ? VERDE + " ▶ " + RESET : "   ";
        System.out.println("  " + marcadorOff + AMARILLO + (pistas.size() + 1) + "." + RESET +
                " Sin música  " + AZUL + "[Predeterminado]" + RESET);
        System.out.println("  " + AZUL + "═".repeat(50) + RESET);
        System.out.print("  " + BLANCO + "▶ Elige una pista (1-" + (pistas.size() + 1) + "): " + RESET);

        try {
            int sel = Integer.parseInt(sc.nextLine().trim());
            if (sel >= 1 && sel <= pistas.size()) {
                pistaSeleccionada = sel;
                System.out.println("  " + VERDE + "✓ Pista seleccionada: " + pistas.get(sel - 1).titulo + RESET);
            } else if (sel == pistas.size() + 1) {
                pistaSeleccionada = 0;
                System.out.println("  " + VERDE + "✓ Música desactivada." + RESET);
            } else {
                System.out.println("  " + ROJO + "Opción inválida." + RESET);
            }
        } catch (NumberFormatException e) {
            System.out.println("  " + ROJO + "Entrada inválida." + RESET);
        }
    }

    public int           getPistaSeleccionada() { return pistaSeleccionada; }
    public boolean       hayMusicaActiva()      { return pistaSeleccionada > 0; }
    public List<Pista>   getPistas()            { return Collections.unmodifiableList(pistas); }

    // ══════════════════════════════════════════════════════════════════
    //  Subclase MusicaConsola — solo imprime el anuncio en pantalla.
    //  Útil en entornos sin dispositivo de audio.
    // ══════════════════════════════════════════════════════════════════
    public static class MusicaConsola extends MusicaJJK {
        @Override
        public void reproducir() {
            if (!hayMusicaActiva()) return;
            Pista p = pistas.get(pistaSeleccionada - 1);
            System.out.println("\n  " + MAGENTA_INT + NEGRITA +
                    "♪♪ REPRODUCIENDO: " + p.titulo + " — " + p.artista + RESET);
            System.out.println("  " + AZUL + "   " + p.contexto + RESET);
        }
        // detener() heredado: no-op
    }

    // ══════════════════════════════════════════════════════════════════
    //  Subclase MusicaAudio — anuncia la pista Y la reproduce via
    //  javax.sound.sampled (parte del JDK estándar, solo soporta WAV).
    //
    //  Flujo:
    //    1. reproducir() → anuncia en consola + abre el Clip en segundo plano.
    //    2. El Clip hace loop continuo sin bloquear el hilo del juego.
    //    3. detener() para y cierra el Clip al terminar el combate.
    //
    //  Si el archivo WAV no existe, se muestra la ruta esperada y el
    //  juego continúa sin audio (graceful degradation).
    // ══════════════════════════════════════════════════════════════════
    public static class MusicaAudio extends MusicaJJK {

        private volatile Clip clipActual = null;

        @Override
        public void reproducir() {
            if (!hayMusicaActiva()) return;

            Pista p = pistas.get(pistaSeleccionada - 1);

            // Anuncio en consola siempre (independiente del audio)
            System.out.println("\n  " + MAGENTA_INT + NEGRITA +
                    "♪♪ REPRODUCIENDO: " + p.titulo + " — " + p.artista + RESET);
            System.out.println("  " + AZUL + "   " + p.contexto + RESET);

            if (p.archivo.isEmpty()) return;

            File archivoWav = new File(CARPETA_MUSICA + p.archivo);

            if (!archivoWav.exists()) {
                System.out.println("  " + AMARILLO +
                        "  ⚠ Archivo de audio no encontrado: " + archivoWav.getAbsolutePath() + RESET);
                System.out.println("  " + AMARILLO +
                        "  Coloca el WAV en: " + new File(CARPETA_MUSICA).getAbsolutePath() + RESET);
                System.out.println("  " + AMARILLO +
                        "  Consulta assets/music/LEEME.md para instrucciones." + RESET);
                return;
            }

            try {
                detener(); // para cualquier clip anterior

                AudioInputStream ais = AudioSystem.getAudioInputStream(archivoWav);
                clipActual = AudioSystem.getClip();
                clipActual.open(ais);
                clipActual.loop(Clip.LOOP_CONTINUOUSLY);  // hilo demonio; no bloquea
                clipActual.start();
                System.out.println("  " + VERDE_INT + "  ▶ Reproduciendo en bucle..." + RESET);

            } catch (UnsupportedAudioFileException e) {
                System.out.println("  " + AMARILLO +
                        "  ⚠ Formato no soportado. Convierte el archivo a WAV (PCM 16-bit, 44100 Hz)." + RESET);
            } catch (LineUnavailableException e) {
                System.out.println("  " + AMARILLO +
                        "  ⚠ Dispositivo de audio no disponible: " + e.getMessage() + RESET);
            } catch (IOException e) {
                System.out.println("  " + AMARILLO +
                        "  ⚠ Error al leer el archivo de audio: " + e.getMessage() + RESET);
            }
        }

        /**
         * Para y cierra el Clip activo.
         * Llamar al terminar el combate o al salir del juego.
         */
        @Override
        public void detener() {
            if (clipActual != null) {
                try {
                    if (clipActual.isRunning()) clipActual.stop();
                    clipActual.close();
                } catch (Exception ignored) { /* silenciar errores al cerrar */ }
                clipActual = null;
            }
        }
    }
}