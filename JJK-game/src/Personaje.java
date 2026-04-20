import java.util.*;

public abstract class Personaje {
    protected String nombre;
    protected int vida, maxVida, energia, maxEnergia;
    protected boolean defendiendo = false;
    protected boolean dominioActivo = false;
    protected boolean dentroDeDominioEnemigo = false;
    protected int turnosBurnout = 0;
    protected int turnosInmortal = 0;
    protected int turnosInmovilizado = 0;
    protected String causaInmovilizacion = "tecnica enemiga";
    protected int turnosPotenciado = 0;

    protected boolean curacionDisponible  = false;
    protected boolean portadorHerramienta = false;
    protected boolean sinEnergiaMaldita   = false;

    protected List<Habilidad> habilidades = new ArrayList<>();

    public Personaje(String nombre, int vida, int energia) {
        this.nombre = nombre; this.vida = vida; this.maxVida = vida;
        this.energia = energia; this.maxEnergia = energia;
    }

    public void setCuracionDisponible (boolean b) { this.curacionDisponible  = b; }
    public void setPortadorHerramienta(boolean b) { this.portadorHerramienta = b; }
    public void setSinEnergiaMaldita  (boolean b) { this.sinEnergiaMaldita   = b; }

    public boolean isSinEnergiaMaldita() { return sinEnergiaMaldita; }
    public boolean puedeUsarEspeciales() { return !sinEnergiaMaldita; }
    public boolean puedeCurarse()        { return curacionDisponible; }

    public void curarse() {
        if (sinEnergiaMaldita) {
            this.vida = Math.min(this.maxVida, this.vida + 150);
            System.out.println(">>> " + nombre + " se cura usando regeneracion fisica extrema.");
        } else if (nombre.equals("Gojo Satoru")) {
            if (energia >= 5) { this.energia -= 5; this.vida = Math.min(this.maxVida, this.vida + 250);
                System.out.println(">>> " + nombre + " activa la Tecnica Inversa al instante (+250 HP).");
            } else { System.out.println("No hay suficiente energia para curarse."); }
        } else if (energia >= 50) {
            this.energia -= 50; this.vida = Math.min(this.maxVida, this.vida + 250);
            System.out.println(">>> " + nombre + " usa la Tecnica Inversa (+250 HP).");
        } else { System.out.println("No hay suficiente energia para curarse."); }
    }

    public void ataqueBasico(Personaje objetivo) {
        System.out.println("\n>>> " + this.nombre + " lanza un Ataque Fisico Comun.");
        int danio = 30;
        if (puedeUsarEspeciales() && Math.random() < 0.05) {
            System.out.println("DESTELLO NEGRO! (Black Flash)"); danio *= 2.5;
        }
        objetivo.marcarUltimoGolpe(false);
        objetivo.recibirDanio(danio);
    }

    public void recargarEnergia() {
        if (puedeUsarEspeciales()) {
            this.energia = Math.min(this.maxEnergia, this.energia + 80);
            System.out.println(">>> " + this.nombre + " recarga su Energia Maldita.");
        }
    }

    public void usarHabilidad(int indice, Personaje objetivo) throws Exception {
        if (turnosInmovilizado > 0) throw new Exception(this.nombre + " esta inmovilizado y no puede atacar.");
        if (this.turnosBurnout > 0 && indice > 0) throw new Exception("BURNOUT! Tu tecnica ritual esta desactivada.");
        Habilidad h = habilidades.get(indice);
        if (this.energia < h.getCosteEnergia()) throw new Exception("Energia maldita insuficiente.");
        this.energia -= h.getCosteEnergia();
        boolean golpeEnergetico = h.getCosteEnergia() > 0 || this.portadorHerramienta;
        objetivo.marcarUltimoGolpe(golpeEnergetico);

        if (h.getNombre().equals("Tajo de Exterminio") && objetivo instanceof Maldicion) {
            System.out.println("\n>>> " + this.nombre + " usa: " + h.getNombre());
            System.out.println("La Espada del Exterminio purifica al instante!");
            objetivo.vida = 0; return;
        }
        if (h.getNombre().contains("JACOB") && h.getNombre().contains("ANIQUILACI") && objetivo instanceof Maldicion) {
            System.out.println("\n>>> " + this.nombre + " usa: " + h.getNombre());
            System.out.println("La Katana Jacob destruye la maldicion sin defensa posible!");
            objetivo.setDentroDeDominio(true);
            objetivo.vida = Math.max(0, objetivo.vida - h.getDanio() * 2);
            System.out.println(objetivo.getNombre() + " recibe " + (h.getDanio()*2) + " danio angelico. (HP: " + objetivo.vida + ")");
            return;
        }

        int danioFinal = h.getDanio();
        if (this.turnosPotenciado > 0)  danioFinal = (int)(danioFinal * 1.5);
        else if (this.turnosPotenciado < 0) danioFinal = (int)(danioFinal * 0.6);
        if (this.dominioActivo) danioFinal *= 1.3;

        if (h.isFisico() && puedeUsarEspeciales()) {
            boolean bf = h.getNombre().contains("DESTELLO NEGRO") || h.getNombre().equals("Destello Negro") || Math.random() < 0.05;
            if (bf) { System.out.println("DESTELLO NEGRO! Las chispas oscuras vuelan."); danioFinal *= 2.5; }
        }

        System.out.println("\n>>> " + this.nombre + " usa: " + h.getNombre());
        System.out.println("INFO: " + h.getDescripcion());

        String nom = h.getNombre();
        if (nom.contains("VAC") && nom.contains("INFINITO")) {
            System.out.println("El Vacio Infinito paraliza a " + objetivo.getNombre() + " durante 2 turnos.");
            objetivo.setTurnosInmovilizado(2, "VACIO INFINITO");
        } else if (nom.contains("AMOR MUTUO") || nom.contains("AUTOENCARNACI") || nom.contains("ATA") && nom.contains("D DE LA")) {
            this.turnosPotenciado = 2;
            System.out.println("Las estadisticas de " + this.nombre + " se potencian durante 2 turnos.");
        } else if (nom.contains("IDLE DEATH GAMBLE")) {
            if (Math.random() < 0.33) {
                System.out.println("JACKPOT! " + this.nombre + " obtiene energia infinita e inmortalidad.");
                this.turnosInmortal = 4; this.energia = 9999;
            } else { System.out.println("Sin suerte en el IDLE DEATH GAMBLE."); }
        } else if (nom.contains("VEREDICTO: CULPABLE")) {
            System.out.println(objetivo.getNombre() + " queda inmovilizado por sentencia (1 turno).");
            objetivo.setTurnosInmovilizado(1, "VEREDICTO: CULPABLE");
        } else if (nom.contains("Confiscaci")) {
            System.out.println("Las tecnicas de " + objetivo.getNombre() + " quedan debilitadas.");
            objetivo.setTurnosPotenciado(-1);
        } else if (nom.contains("Purificaci")) {
            this.turnosBurnout = 0; this.turnosInmovilizado = 0;
            this.vida = Math.min(this.maxVida, this.vida + 80);
            System.out.println("Efectos negativos eliminados y +80 HP.");
        } else if (nom.contains("Barrera de Sonido")) {
            System.out.println(objetivo.getNombre() + " queda desorientado (1 turno).");
            objetivo.setTurnosInmovilizado(1, "Barrera de Sonido");
        } else if (nom.contains("Barrera S") && nom.contains("nica Maldita")) {
            System.out.println(objetivo.getNombre() + " queda inmovilizado 2 turnos.");
            objetivo.setTurnosInmovilizado(2, "Barrera Sonica Maldita");
        } else if (nom.contains("Barrera Anti-Hechicero")) {
            System.out.println("La CE de " + objetivo.getNombre() + " queda suprimida.");
            objetivo.setTurnosPotenciado(-2);
        } else if (nom.contains("GRAN JUEGO")) {
            System.out.println(objetivo.getNombre() + " queda atrapado en el Gran Juego (2 turnos, sin CE).");
            objetivo.setTurnosInmovilizado(2, "GRAN JUEGO"); objetivo.confiscarEnergia();
        } else if (nom.contains("Torrente") && nom.contains("Velocidad")) {
            this.turnosPotenciado = 1;
            System.out.println(this.nombre + " alcanza velocidad absoluta este turno.");
        } else if (nom.contains("Orgullo del Clan Zenin")) {
            this.turnosPotenciado = 2; this.vida = Math.min(this.maxVida, this.vida + 100);
            System.out.println(this.nombre + " potenciado 2 turnos y +100 HP.");
        } else if (nom.contains("TORMENTA FINAL")) {
            objetivo.setDentroDeDominio(true);
            System.out.println("TORMENTA FINAL: no puede bloquearse ni esquivarse.");
        } else if (nom.contains("Masa Virtual: Escudo")) {
            this.defendiendo = true;
            System.out.println("Escudo de masa virtual activo (danio reducido a la mitad este turno).");
            System.out.println("Contragolpe: " + objetivo.getNombre() + " recibe 40 danio.");
            objetivo.recibirDanioFijo(40);
        } else if (nom.contains("COLAPSO ESTELAR")) {
            System.out.println("Singularidad liberada sobre " + objetivo.getNombre() + ".");
        }
        if (danioFinal > 0) objetivo.recibirDanio(danioFinal);
    }

    public void recibirDanio(int cantidad) {
        if (this.turnosInmortal > 0) { System.out.println(nombre + " es inmortal: regenera al instante!"); return; }
        if (!dentroDeDominioEnemigo && Math.random() < 0.15) { System.out.println(this.nombre + " esquiva!"); return; }
        if (dentroDeDominioEnemigo) System.out.println("Golpe garantizado por el Dominio.");
        int danioFinal = defendiendo ? cantidad / 2 : cantidad;
        if (defendiendo) System.out.println(nombre + " bloquea: danio reducido a la mitad.");
        this.vida = Math.max(0, this.vida - danioFinal);
        System.out.println(nombre + " recibe " + danioFinal + " danio. (HP: " + this.vida + "/" + this.maxVida + ")");
    }

    public void recibirDanioFijo(int cantidad) {
        if (this.turnosInmortal > 0) return;
        this.vida = Math.max(0, this.vida - cantidad);
        System.out.println(nombre + " sufre " + cantidad + " danio continuo. (HP: " + this.vida + ")");
    }

    public void prepararTurno() {
        this.defendiendo = false;
        if (turnosBurnout > 0)      turnosBurnout--;
        if (turnosInmortal > 0)     turnosInmortal--;
        if (turnosInmovilizado > 0) turnosInmovilizado--;
        if (turnosPotenciado > 0)   turnosPotenciado--;
    }

    public boolean tieneHerramientaMaldita() { return !herramientaConfiscada && portadorHerramienta; }

    public void confiscarHerramienta() {
        herramientaConfiscada = true;
        this.turnosPotenciado = Math.min(this.turnosPotenciado - 2, -2);
    }

    public void confiscarEnergia()  { this.energia = 0; System.out.println("  " + nombre + " queda sin energia maldita."); }

    public void drenarEnergia(int cantidad) {
        int d = Math.min(this.energia, cantidad); this.energia -= d;
        if (d > 0) System.out.println("  " + nombre + " pierde " + d + " CE. (CE: " + this.energia + ")");
    }

    public void    marcarUltimoGolpe(boolean b) { this.ultimoGolpeFueEnergetico = b; }
    public boolean ultimoGolpeFueEnergetico()   { return ultimoGolpeFueEnergetico; }

    public boolean estaVivo()         { return vida > 0; }
    public String  getNombre()        { return nombre; }
    public int     getVida()          { return vida; }
    public int     getMaxVida()       { return maxVida; }
    public int     getEnergia()       { return energia; }
    public int     getMaxEnergia()    { return maxEnergia; }
    public List<Habilidad> getHabilidades()      { return habilidades; }
    public void    addHabilidad(Habilidad h)     { this.habilidades.add(h); }
    public void    setDefensa(boolean d)         { this.defendiendo = d; }
    public void    setBurnout(int t)             { this.turnosBurnout = t; }
    public boolean isDominioActivo()             { return dominioActivo; }
    public void    setDominioActivo(boolean b)   { this.dominioActivo = b; }
    public void    setDentroDeDominio(boolean b) { this.dentroDeDominioEnemigo = b; }
    public void    setTurnosInmovilizado(int t)  { this.turnosInmovilizado = t; }
    public void    setTurnosInmovilizado(int t, String causa) { this.turnosInmovilizado = t; this.causaInmovilizacion = causa; }
    public String  getCausaInmovilizacion()      { return causaInmovilizacion; }
    public void    setTurnosPotenciado(int t)    { this.turnosPotenciado = Math.max(this.turnosPotenciado + t, -2); }

    protected boolean ultimoGolpeFueEnergetico = true;
    protected boolean herramientaConfiscada    = false;
}
