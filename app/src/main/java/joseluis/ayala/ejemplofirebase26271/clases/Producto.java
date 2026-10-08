package joseluis.ayala.ejemplofirebase26271.clases;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "productos")
public class Producto implements Serializable {

    @PrimaryKey
    @NonNull
    private String codigo;
    private String nombre;
    private double precio;
    private String foto;

    public Producto() {
    }

    public Producto(@NonNull String codigo, String nombre, double precio, String foto) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.foto = foto;
    }

    @NonNull
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(@NonNull String codigo) {
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