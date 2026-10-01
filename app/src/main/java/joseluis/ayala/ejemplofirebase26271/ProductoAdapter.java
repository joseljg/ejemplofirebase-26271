package joseluis.ayala.ejemplofirebase26271;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import joseluis.ayala.ejemplofirebase26271.clases.Producto;
import com.bumptech.glide.Glide;
public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ViewHolder> {

    private final List<Producto> lista;
    private final DatabaseReference dbProductos;

    public ProductoAdapter(List<Producto> lista) {
        this.lista = lista;

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        dbProductos = uid == null
                ? null
                : FirebaseDatabase.getInstance().getReference("productos").child(uid);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto producto = lista.get(position);

        holder.tvNombre.setText(producto.getNombre());
        holder.tvCodigo.setText("Código: " + producto.getCodigo());
        holder.tvPrecio.setText(String.format(
                java.util.Locale.getDefault(),
                "Precio: %.2f €",
                producto.getPrecio()
        ));

        // Cargar imagen desde la URL guardada en Firebase
        Glide.with(holder.ivFoto.getContext())
                .load(producto.getFoto())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivFoto);

        holder.btnEliminar.setOnClickListener(v ->
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Eliminar producto")
                        .setMessage("¿Seguro que quieres borrar '"
                                + producto.getNombre() + "'?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            if (dbProductos != null) {
                                dbProductos.child(producto.getCodigo()).removeValue();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show());

        holder.btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(
                    v.getContext(),
                    InsertEditProductoActivity.class
            );
            intent.putExtra("producto", producto);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvNombre, tvCodigo, tvPrecio;
        final Button btnEditar, btnEliminar;
        final ImageView ivFoto;

        ViewHolder(View itemView) {
            super(itemView);

            ivFoto = itemView.findViewById(R.id.ivFotoProducto);
            tvNombre = itemView.findViewById(R.id.tvNombreProducto);
            tvCodigo = itemView.findViewById(R.id.tvCodigoProducto);
            tvPrecio = itemView.findViewById(R.id.tvPrecioProducto);
            btnEditar = itemView.findViewById(R.id.btnEditarProducto);
            btnEliminar = itemView.findViewById(R.id.btnEliminarProducto);
        }
    }
}
