package joseluis.ayala.ejemplofirebase26271.clases;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.Ignore;
import java.io.Serializable;

@Entity(tableName = "productos")
public class Producto implements Serializable {

    @PrimaryKey
    @NonNull
    private String codigo = "";
    private String nombre;
    private double precio;
    private String foto; // Esta es la URL de Cloudinary

    public Producto() {}

    @Ignore
    public Producto(String codigo, String nombre, double precio, String foto) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.foto = foto;
    }

    @NonNull
    public String getCodigo() { return codigo; }
    public void setCodigo(@NonNull String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
}