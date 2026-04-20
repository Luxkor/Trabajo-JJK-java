public class Maldicion extends Personaje implements Combatiente {
    public Maldicion(String nombre, int vida, int energia) { super(nombre, vida, energia); }
    @Override public boolean esMaldicion() { return true; }
    @Override
    public void manifestarAura() {
        System.out.println("  \uD83D\uDC80 " + getNombre() + " deforma el aire con una presencia oscura y asfixiante.");
    }
}
