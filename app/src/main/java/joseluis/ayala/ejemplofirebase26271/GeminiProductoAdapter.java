package joseluis.ayala.ejemplofirebase26271;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import joseluis.ayala.ejemplofirebase26271.clases.Producto;
import java.util.List;

public class GeminiProductoAdapter extends RecyclerView.Adapter<GeminiProductoAdapter.ViewHolder> {

    private final List<ProductoItemSeleccion> listaProductos;

    public static class ProductoItemSeleccion {
        public Producto producto;
        public boolean seleccionado;

        public ProductoItemSeleccion(Producto producto) {
            this.producto = producto;
            this.seleccionado = false;
        }
    }

    public GeminiProductoAdapter(List<ProductoItemSeleccion> listaProductos) {
        this.listaProductos = listaProductos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto_seleccion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductoItemSeleccion item = listaProductos.get(position);
        holder.tvNombre.setText(item.producto.getNombre());
        // Ajusta según las propiedades de tu clase Producto (ej. Descripción, Precio, etc.)
        holder.tvDetalles.setText("Precio/Detalles: " + item.producto.getPrecio());

        holder.cbSeleccion.setChecked(item.seleccionado);
        holder.cbSeleccion.setOnCheckedChangeListener((buttonView, isChecked) -> item.seleccionado = isChecked);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDetalles;
        CheckBox cbSeleccion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvDetalles = itemView.findViewById(R.id.tvDetallesProducto);
            cbSeleccion = itemView.findViewById(R.id.cbSeleccionado);
        }
    }
}