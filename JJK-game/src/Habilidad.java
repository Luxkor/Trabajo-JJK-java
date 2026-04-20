public class Habilidad {
    private final String  nombre, descripcion;
    private final int     danio, costeEnergia;
    private final boolean esFisico;
    public Habilidad(String nombre, String descripcion, int danio, int costeEnergia, Efecto.Tipo tipo, boolean esFisico) {
        this.nombre = nombre; this.descripcion = descripcion;
        this.danio = danio; this.costeEnergia = costeEnergia; this.esFisico = esFisico;
    }
    public String  getNombre()       { return nombre; }
    public String  getDescripcion()  { return descripcion; }
    public int     getDanio()        { return danio; }
    public int     getCosteEnergia() { return costeEnergia; }
    public boolean isFisico()        { return esFisico; }
}
