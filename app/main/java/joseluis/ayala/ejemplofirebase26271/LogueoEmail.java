package joseluis.ayala.ejemplofirebase26271;

import android.os.Bundle;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogueoEmail extends AppCompatActivity {

    private static final String TAG = "EmailAuth";

    // Firebase Auth
    private FirebaseAuth mAuth;

    // Vistas de interfaz
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister, btnCerrar;
    private TextView tvStatus;

    @Override
    protected void onStart() {
        super.onStart();
        // Verificar si hay sesión activa al iniciar
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logueo_email);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Mapeo con los IDs de activity_logueo_email.xml
        etEmail = findViewById(R.id.edtNombreLogueoe);
        etPassword = findViewById(R.id.edtPasswordLogueoe);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnCerrar = findViewById(R.id.btnCerrarSesion);
        tvStatus = findViewById(R.id.txtEstado);

        // Listeners de los botones
        btnLogin.setOnClickListener(v->loginUser());
        btnRegister.setOnClickListener(v -> registerUser());
        btnCerrar.setOnClickListener(v -> cerrarSesionUser());
    }

    private void cerrarSesionUser() {
        mAuth.signOut();
        Toast.makeText(this, "CIERRE DE SESIÓN CORRECTO", Toast.LENGTH_SHORT).show();
        tvStatus.setText("Cierre de sesión correcto");
        updateUI(null);
    }

    // -------------------------
    // LOGIN
    // -------------------------
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateForm(email, password)) return;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "signInWithEmail:success");
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        tvStatus.setText("Credenciales incorrectas");
                    }
                });
    }

    // -------------------------
    // REGISTRO
    // -------------------------
    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateForm(email, password)) return;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "createUserWithEmail:success");
                        updateUI(user);
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        tvStatus.setText("No se pudo registrar el usuario");
                    }
                });
    }

    // -------------------------
    // VALIDACIÓN
    // -------------------------
    private boolean validateForm(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Requerido");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Requerido");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return false;
        }

        return true;
    }

    // -------------------------
    // ACTUALIZAR INTERFAZ
    // -------------------------
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            tvStatus.setText("Bienvenido: " + user.getEmail());
            Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show();
            abrirMain();
        } else {
            tvStatus.setText("Debes loguearte");
        }
    }

    private void abrirMain() {
        Intent intent = new Intent(LogueoEmail.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}