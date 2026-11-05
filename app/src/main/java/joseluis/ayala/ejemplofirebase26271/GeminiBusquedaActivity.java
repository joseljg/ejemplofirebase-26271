package joseluis.ayala.ejemplofirebase26271;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;
import com.google.firebase.ai.java.GenerativeModelFutures;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import joseluis.ayala.ejemplofirebase26271.clases.AppDatabase;
import joseluis.ayala.ejemplofirebase26271.clases.Producto;

public class GeminiBusquedaActivity extends AppCompatActivity {

    private EditText etPromptGemini;
    private Button btnBuscarGemini, btnAnadirSeleccionados;
    private RecyclerView recyclerView;
    private GeminiProductoAdapter adapter;
    private List<GeminiProductoAdapter.ProductoItemSeleccion> listaItems = new ArrayList<>();

    private GenerativeModelFutures modelFutures;
    private AppDatabase db;
    private ExecutorService executorService;
    private FirebaseAuth mAuth;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(GeminiBusquedaActivity.this, LogueoEmail.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini_busqueda);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etPromptGemini = findViewById(R.id.etPromptGemini);
        btnBuscarGemini = findViewById(R.id.btnBuscarGemini);
        btnAnadirSeleccionados = findViewById(R.id.btnAnadirSeleccionados);
        recyclerView = findViewById(R.id.recyclerGeminiProductos);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GeminiProductoAdapter(listaItems);
        recyclerView.setAdapter(adapter);

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Inicialización con el modelo funcional actual de alto rendimiento
        try {
            GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                    .generativeModel("gemini-3.5-flash-lite");

            modelFutures = GenerativeModelFutures.from(generativeModel);
        } catch (Exception e) {
            Log.e("GEMINI_DEBUG", "Error al iniciar Firebase AI", e);
            Toast.makeText(this, "Error al iniciar Firebase AI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        btnBuscarGemini.setOnClickListener(v -> consultarGemini());
        btnAnadirSeleccionados.setOnClickListener(v -> guardarSeleccionadosEnBaseDeDatos());
    }

    private void consultarGemini() {
        if (modelFutures == null) {
            Toast.makeText(this, "El modelo de IA no se ha inicializado", Toast.LENGTH_SHORT).show();
            return;
        }

        String query = etPromptGemini.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Escribe un tema o categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        btnBuscarGemini.setEnabled(false);
        Toast.makeText(this, "Consultando a Gemini...", Toast.LENGTH_SHORT).show();

        String promptText = "Genera una lista de productos basados en la categoría: '" + query +
                "'. Devuelve ÚNICAMENTE un array JSON válido, sin bloques markdown ni texto adicional, con esta estructura exacta por objeto: " +
                "[{\"nombre\": \"Ejemplo\", \"precio\": 10.0, \"descripcion\": \"Descripción breve\"}]";

        Content content = new Content.Builder()
                .addText(promptText)
                .build();

        ListenableFuture<GenerateContentResponse> responseFuture = modelFutures.generateContent(content);

        Futures.addCallback(responseFuture, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> btnBuscarGemini.setEnabled(true));

                try {
                    String jsonText = result != null ? result.getText() : null;
                    Log.d("GEMINI_DEBUG", "Respuesta cruda de la IA: " + jsonText);

                    runOnUiThread(() -> parsearYMostrarResultados(jsonText));
                } catch (Exception e) {
                    Log.e("GEMINI_DEBUG", "Error obteniendo texto de la respuesta", e);
                    runOnUiThread(() -> Toast.makeText(GeminiBusquedaActivity.this, "Error al leer la respuesta de la IA", Toast.LENGTH_LONG).show());
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                runOnUiThread(() -> {
                    btnBuscarGemini.setEnabled(true);
                    Log.e("GEMINI_DEBUG", "Fallo en la petición a Gemini", t);
                    Toast.makeText(GeminiBusquedaActivity.this, "Error de IA: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void parsearYMostrarResultados(String jsonResponse) {
        try {
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                Toast.makeText(this, "La respuesta de la IA está vacía", Toast.LENGTH_SHORT).show();
                return;
            }

            String jsonLimpiado = jsonResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            Log.d("GEMINI_DEBUG", "JSON limpiado para Gson: " + jsonLimpiado);

            Gson gson = new Gson();
            Type tipoLista = new TypeToken<List<Producto>>() {}.getType();
            List<Producto> productosCrudos = gson.fromJson(jsonLimpiado, tipoLista);

            listaItems.clear();
            if (productosCrudos != null) {
                for (Producto p : productosCrudos) {
                    listaItems.add(new GeminiProductoAdapter.ProductoItemSeleccion(p));
                }
            }

            adapter.notifyDataSetChanged();

            if (listaItems.isEmpty()) {
                Toast.makeText(this, "No se encontraron productos para parsear", Toast.LENGTH_SHORT).show();
            } else {
                btnAnadirSeleccionados.setEnabled(true);
                Toast.makeText(this, "¡Productos cargados con éxito!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("GEMINI_DEBUG", "Error al parsear el JSON con Gson", e);
            Toast.makeText(this, "Error al interpretar la respuesta JSON (Revisa el Logcat)", Toast.LENGTH_LONG).show();
        }
    }

    private void guardarSeleccionadosEnBaseDeDatos() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = user.getUid();

        List<Producto> aInsertar = new ArrayList<>();
        for (GeminiProductoAdapter.ProductoItemSeleccion item : listaItems) {
            if (item.seleccionado) {
                Producto p = item.producto;

                if (p.getCodigo() == null || p.getCodigo().trim().isEmpty()) {
                    p.setCodigo(UUID.randomUUID().toString());
                }

                aInsertar.add(p);
            }
        }

        if (aInsertar.isEmpty()) {
            Toast.makeText(this, "No has seleccionado ningún producto", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            try {
                for (Producto p : aInsertar) {
                    // 1. Guardar en Base de Datos Local (Room)
                    db.productoDao().insert(p);

                    // 2. Guardar en Firebase Realtime Database bajo la ruta del usuario actual
                    FirebaseDatabase.getInstance().getReference("productos")
                            .child(uid)
                            .child(p.getCodigo())
                            .setValue(p);
                }

                runOnUiThread(() -> {
                    Toast.makeText(GeminiBusquedaActivity.this, "¡Productos guardados en Local y en Remoto!", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                Log.e("GEMINI_DEBUG", "Error al guardar en local o remoto", e);
                runOnUiThread(() -> {
                    Toast.makeText(GeminiBusquedaActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}