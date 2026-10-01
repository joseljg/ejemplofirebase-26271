package joseluis.ayala.ejemplofirebase26271;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import joseluis.ayala.ejemplofirebase26271.clases.Producto;

public class CrudProductos extends AppCompatActivity {

    private EditText etBuscar;
    private Button btnBuscar, btnInsertar;
    private RecyclerView recyclerView;

    private DatabaseReference dbProductos;
    private final List<Producto> listaCompleta = new ArrayList<>();
    private final List<Producto> lista = new ArrayList<>();
    private ProductoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crud_productos);

        etBuscar = findViewById(R.id.etBuscarProducto);
        btnBuscar = findViewById(R.id.btnBuscarProducto);
        btnInsertar = findViewById(R.id.btnInsertarProducto);
        recyclerView = findViewById(R.id.recyclerProductos);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductoAdapter(lista);
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cada usuario trabaja únicamente con sus propios productos.
        dbProductos = FirebaseDatabase.getInstance()
                .getReference("productos")
                .child(user.getUid());

        cargarProductos();

        btnBuscar.setOnClickListener(v -> buscar());
        btnInsertar.setOnClickListener(v ->
                startActivity(new Intent(CrudProductos.this,  InsertEditProductoActivity.class)));
    }

    private void cargarProductos() {
        dbProductos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaCompleta.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Producto producto = ds.getValue(Producto.class);
                    if (producto != null) {
                        listaCompleta.add(producto);
                    }
                }

                lista.clear();
                lista.addAll(listaCompleta);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CrudProductos.this,
                        "No se pudieron cargar los productos: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void buscar() {
        String texto = etBuscar.getText().toString().trim().toLowerCase();

        lista.clear();

        if (texto.isEmpty()) {
            lista.addAll(listaCompleta);
        } else {
            for (Producto producto : listaCompleta) {
                String nombre = producto.getNombre() == null ? "" : producto.getNombre().toLowerCase();
                String codigo = producto.getCodigo() == null ? "" : producto.getCodigo().toLowerCase();

                if (nombre.contains(texto) || codigo.contains(texto)) {
                    lista.add(producto);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
