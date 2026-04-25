public class Habilidad {
    private final String     nombre, descripcion;
    private final int        danio, costeEnergia;
    // ARREGLO: el campo 'tipo' se declaraba en el constructor pero nunca se almacenaba.
    // Ahora se guarda correctamente y se expone mediante getTipo().
    private final Efecto.Tipo tipo;
    private final boolean     esFisico;

    public Habilidad(String nombre, String descripcion, int danio, int costeEnergia,
                     Efecto.Tipo tipo, boolean esFisico) {
        this.nombre       = nombre;
        this.descripcion  = descripcion;
        this.danio        = danio;
        this.costeEnergia = costeEnergia;
        this.tipo         = tipo;
        this.esFisico     = esFisico;
    }

    public String    getNombre()       { return nombre; }
    public String    getDescripcion()  { return descripcion; }
    public int       getDanio()        { return danio; }
    public int       getCosteEnergia() { return costeEnergia; }
    public Efecto.Tipo getTipo()       { return tipo; }
    public boolean   isFisico()        { return esFisico; }
}
