package joseluis.ayala.ejemplofirebase26271.clases;
import java.io.Serializable;
import java.util.Objects;

/**
 * Producto almacenado en Firebase Realtime Database.
 * El código se utiliza como identificador dentro de la colección del usuario.
 */
public class Producto implements Serializable {

    private String codigo;
    private String nombre;
    private double precio;
    private String foto;

    // Constructor vacío necesario para Firebase.
    public Producto() {
    }

    public Producto(String codigo, String nombre, double precio, String foto) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.foto = foto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto producto = (Producto) o;
        return Objects.equals(codigo, producto.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
}
