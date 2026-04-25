import java.util.*;
import java.io.*;
import javax.sound.sampled.*;

public abstract class MusicaJJK {

    // ── Rutas ─────────────────────────────────────────────────────────
    private static final String RUTA_CSV      = "data" + File.separator + "musica.csv";
    public  static final String CARPETA_AUDIO = "assets" + File.separator + "music" + File.separator;

    // ── Colores (delegados a Colores.java para evitar duplicación) ────
    // ARREGLO: antes estas constantes duplicaban literales ANSI hardcodeados.
    private static final String RESET       = Colores.RESET;
    private static final String NEGRITA     = Colores.NEGRITA;
    private static final String MAGENTA_INT = Colores.MAGENTA_INT;
    private static final String CYAN_INT    = Colores.CYAN_INT;
    private static final String AMARILLO    = Colores.AMARILLO;
    private static final String VERDE       = Colores.VERDE;
    private static final String ROJO        = Colores.ROJO;
    private static final String BLANCO      = Colores.BLANCO;
    private static final String AZUL        = Colores.AZUL;
    private static final String VERDE_INT   = Colores.VERDE_INT;

    // ── Modelo de una pista ───────────────────────────────────────────
    protected static class Pista {
        final String titulo, artista, contexto, archivo;
        Pista(String titulo, String artista, String contexto, String archivo) {
            this.titulo = titulo.trim(); this.artista = artista.trim();
            this.contexto = contexto.trim(); this.archivo = archivo != null ? archivo.trim() : "";
        }
        @Override
        public String toString() {
            String parte = artista.isEmpty() ? "" : " - " + artista;
            return NEGRITA + titulo + RESET + parte + AZUL + "  [" + contexto + "]" + RESET;
        }
    }

    protected final List<Pista> pistas = new ArrayList<>();
    protected int pistaSeleccionada = 0;

    protected MusicaJJK() { cargarCatalogo(); }

    private void cargarCatalogo() {
        File csv = new File(RUTA_CSV);
        if (!csv.exists()) return;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(csv), java.nio.charset.StandardCharsets.UTF_8))) {
            String linea; boolean primera = true;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (primera) { primera = false; continue; }
                String[] f = linea.split("\\|", -1);
                if (f.length < 3) { System.out.println("  Linea ignorada en musica.csv: " + linea); continue; }
                final String titulo, artista, contexto, archivo;
                if (f.length >= 4) { titulo = f[0].trim(); artista = f[1].trim(); contexto = f[2].trim(); archivo = f[3].trim(); }
                else               { titulo = f[0].trim(); artista = "";           contexto = f[1].trim(); archivo = f[2].trim(); }
                pistas.add(new Pista(titulo, artista, contexto, archivo));
            }
        } catch (IOException e) {
            System.out.println("  No se pudo leer musica.csv: " + e.getMessage());
        }
    }

    public abstract void reproducir();
    public void detener() {}

    public void mostrarMenuSeleccion(Scanner sc) {
        System.out.println("\n  " + CYAN_INT + NEGRITA + "-- BANDA SONORA --" + RESET);
        System.out.println("  " + AZUL + "=".repeat(52) + RESET);
        if (pistas.isEmpty()) {
            System.out.println("  " + AMARILLO + "  No hay canciones configuradas." + RESET);
            System.out.println("  " + AMARILLO + "  Edita data/musica.csv para aniadirlas." + RESET);
            System.out.println("  " + AMARILLO + "  Consulta assets/music/LEEME.md." + RESET);
            System.out.println("  " + AZUL + "=".repeat(52) + RESET);
            System.out.print("  Pulsa Enter para volver..."); sc.nextLine(); return;
        }
        for (int i = 0; i < pistas.size(); i++) {
            String m = (pistaSeleccionada == i+1) ? VERDE + " > " + RESET : "   ";
            System.out.println("  " + m + AMARILLO + (i+1) + "." + RESET + " " + pistas.get(i));
        }
        String mOff = (pistaSeleccionada == 0) ? VERDE + " > " + RESET : "   ";
        System.out.println("  " + mOff + AMARILLO + (pistas.size()+1) + "." + RESET + " Sin musica  " + AZUL + "[Predeterminado]" + RESET);
        System.out.println("  " + AZUL + "=".repeat(52) + RESET);
        System.out.print("  " + BLANCO + "> Elige (1-" + (pistas.size()+1) + "): " + RESET);
        try {
            int sel = Integer.parseInt(sc.nextLine().trim());
            if (sel >= 1 && sel <= pistas.size()) {
                pistaSeleccionada = sel;
                System.out.println("  " + VERDE + "Pista: " + pistas.get(sel-1).titulo + RESET);
            } else if (sel == pistas.size()+1) {
                pistaSeleccionada = 0;
                System.out.println("  " + VERDE + "Musica desactivada." + RESET);
            } else { System.out.println("  " + ROJO + "Opcion invalida." + RESET); }
        } catch (NumberFormatException e) { System.out.println("  " + ROJO + "Entrada invalida." + RESET); }
    }

    public int getPistaSeleccionada() { return pistaSeleccionada; }
    public boolean hayMusicaActiva()  { return pistaSeleccionada > 0; }
    public List<Pista> getPistas()    { return Collections.unmodifiableList(pistas); }

    // ── MusicaConsola ─────────────────────────────────────────────────
    public static class MusicaConsola extends MusicaJJK {
        @Override
        public void reproducir() {
            if (!hayMusicaActiva()) return;
            Pista p = pistas.get(pistaSeleccionada - 1);
            System.out.println("\n  " + MAGENTA_INT + NEGRITA + ">> " + p.titulo
                    + (p.artista.isEmpty() ? "" : " - " + p.artista) + RESET);
            System.out.println("  " + AZUL + "   " + p.contexto + RESET);
        }
    }

    // ── MusicaAudio ───────────────────────────────────────────────────
    public static class MusicaAudio extends MusicaJJK {
        private volatile Clip clipActual = null;

        @Override
        public void reproducir() {
            if (!hayMusicaActiva()) return;
            Pista p = pistas.get(pistaSeleccionada - 1);
            System.out.println("\n  " + MAGENTA_INT + NEGRITA + ">> " + p.titulo
                    + (p.artista.isEmpty() ? "" : " - " + p.artista) + RESET);
            System.out.println("  " + AZUL + "   " + p.contexto + RESET);
            if (p.archivo.isEmpty()) return;
            File wav = new File(CARPETA_AUDIO + p.archivo);
            if (!wav.exists()) {
                System.out.println("  Archivo no encontrado: " + wav.getAbsolutePath());
                System.out.println("  Consulta assets/music/LEEME.md para instrucciones."); return;
            }
            try {
                detener();
                AudioInputStream ais = AudioSystem.getAudioInputStream(wav);
                clipActual = AudioSystem.getClip();
                clipActual.open(ais);
                clipActual.loop(Clip.LOOP_CONTINUOUSLY);
                clipActual.start();
                System.out.println("  " + VERDE_INT + "  > Reproduciendo en bucle..." + RESET);
            } catch (UnsupportedAudioFileException e) {
                System.out.println("  Formato no soportado. Convierte a WAV PCM 16-bit 44100 Hz.");
            } catch (LineUnavailableException e) {
                System.out.println("  Dispositivo de audio no disponible.");
            } catch (IOException e) {
                System.out.println("  Error al leer el archivo: " + e.getMessage());
            }
        }

        @Override
        public void detener() {
            if (clipActual == null) return;
            try { if (clipActual.isRunning()) clipActual.stop(); clipActual.close(); }
            catch (Exception ignored) {}
            clipActual = null;
        }
    }
}
