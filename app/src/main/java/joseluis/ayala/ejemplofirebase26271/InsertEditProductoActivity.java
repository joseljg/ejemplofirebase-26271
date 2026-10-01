package joseluis.ayala.ejemplofirebase26271;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;

import joseluis.ayala.ejemplofirebase26271.clases.Producto;

public class InsertEditProductoActivity extends AppCompatActivity {

    private EditText etCodigo, etNombre, etPrecio, etFoto;
    private Button btnGuardar;
    private DatabaseReference dbProductos;
    private boolean esEdicion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_edit_producto);

        etCodigo = findViewById(R.id.etCodigoProducto);
        etNombre = findViewById(R.id.etNombreProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        etFoto = findViewById(R.id.etFotoProducto);
        btnGuardar = findViewById(R.id.btnGuardarProducto);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbProductos = FirebaseDatabase.getInstance()
                .getReference("productos")
                .child(user.getUid());

        Serializable serializable = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            serializable = getIntent().getSerializableExtra("producto", Producto.class);
        } else {
            serializable = getIntent().getSerializableExtra("producto");
        }

        if (serializable instanceof Producto) {
            esEdicion = true;
            Producto producto = (Producto) serializable;

            etCodigo.setText(producto.getCodigo());
            etCodigo.setEnabled(false); // El código actúa como clave del producto.
            etNombre.setText(producto.getNombre());
            etPrecio.setText(String.valueOf(producto.getPrecio()));
            etFoto.setText(producto.getFoto());
        }

        btnGuardar.setOnClickListener(v -> guardarProducto());
    }

    private void guardarProducto() {
        String codigo = etCodigo.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String foto = etFoto.getText().toString().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Completa código, nombre y precio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (codigo.contains(".") || codigo.contains("#") || codigo.contains("$") ||
                codigo.contains("[") || codigo.contains("]") || codigo.contains("/")) {
            Toast.makeText(this, "El código contiene caracteres no válidos", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr.replace(',', '.'));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (precio < 0) {
            Toast.makeText(this, "El precio no puede ser negativo", Toast.LENGTH_SHORT).show();
            return;
        }

        Producto producto = new Producto(codigo, nombre, precio, foto);

        dbProductos.child(codigo).setValue(producto)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            esEdicion ? "Producto actualizado" : "Producto insertado",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
