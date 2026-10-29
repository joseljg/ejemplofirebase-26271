package joseluis.ayala.ejemplofirebase26271;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import joseluis.ayala.ejemplofirebase26271.clases.Producto;

public class InsertEditProductoActivity extends AppCompatActivity {

    private EditText etNombre, etPrecio, etCodigo;
    private ImageView ivFotoProducto;
    private Button btnSeleccionarFoto, btnGuardar;
    private Uri imagenUriSeleccionada;
    private String imagenUrlActual = "";
    private String productoId = null;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(InsertEditProductoActivity.this, LogueoEmail.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    // Selector de imágenes de la galería
    private final ActivityResultLauncher<String> selectorGaleria = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imagenUriSeleccionada = uri;
                    ivFotoProducto.setImageURI(uri); // Mostrar vista previa local
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_edit_producto);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etCodigo = findViewById(R.id.etCodigoProducto);
        etNombre = findViewById(R.id.etNombreProducto);
        etPrecio = findViewById(R.id.etPrecioProducto);
        ivFotoProducto = findViewById(R.id.ivFotoProducto);
        btnSeleccionarFoto = findViewById(R.id.btnSeleccionarFoto);
        btnGuardar = findViewById(R.id.btnGuardarProducto);

        // Inicializamos la referencia de Firebase de manera segura con el UID del usuario
        mDatabase = FirebaseDatabase.getInstance()
                .getReference("productos")
                .child(user.getUid());

        // Comprobar si estamos editando un producto existente
        if (getIntent() != null && getIntent().hasExtra("PRODUCTO_ID")) {
            productoId = getIntent().getStringExtra("PRODUCTO_ID");
            etNombre.setText(getIntent().getStringExtra("PRODUCTO_NOMBRE"));
            etPrecio.setText(String.valueOf(getIntent().getDoubleExtra("PRODUCTO_PRECIO", 0.0)));
            imagenUrlActual = getIntent().getStringExtra("PRODUCTO_IMAGEN");

            if (imagenUrlActual != null && !imagenUrlActual.isEmpty()) {
                Glide.with(this).load(imagenUrlActual).into(ivFotoProducto);
            }
        }

        btnSeleccionarFoto.setOnClickListener(v -> selectorGaleria.launch("image/*"));
        btnGuardar.setOnClickListener(v -> guardarProducto());
    }

    private void guardarProducto() {
        String codigo = etCodigo.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (codigo.isEmpty() || nombre.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = Double.parseDouble(precioStr);

        // Si el usuario seleccionó una nueva imagen, la subimos a Cloudinary
        if (imagenUriSeleccionada != null) {
            Toast.makeText(this, "Subiendo imagen a Cloudinary...", Toast.LENGTH_SHORT).show();

            MediaManager.get().upload(imagenUriSeleccionada)
                    .unsigned("subidas_no_firmada") // <-- Asegúrate de poner aquí tu preset real de Cloudinary
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String nuevaUrl = (String) resultData.get("secure_url");
                            runOnUiThread(() -> subirBaseDeDatos(codigo, nombre, precio, nuevaUrl));
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() ->
                                    Toast.makeText(InsertEditProductoActivity.this, "Error subiendo imagen: " + error.getDescription(), Toast.LENGTH_LONG).show()
                            );
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    })
                    .dispatch();
        } else {
            subirBaseDeDatos(codigo, nombre, precio, imagenUrlActual);
        }
    }

    private void subirBaseDeDatos(String idPersonalizado, String nombre, double precio, String urlImagen) {
        String key = (idPersonalizado != null && !idPersonalizado.isEmpty())
                ? idPersonalizado
                : mDatabase.push().getKey();

        Producto producto = new Producto(key, nombre, precio, urlImagen);

        if (key != null) {
            mDatabase.child(key).setValue(producto).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error al guardar en base de datos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}