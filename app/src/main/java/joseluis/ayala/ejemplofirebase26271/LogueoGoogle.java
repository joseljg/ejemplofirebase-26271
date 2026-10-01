package joseluis.ayala.ejemplofirebase26271;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.common.SignInButton;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LogueoGoogle extends AppCompatActivity {

    private static final String TAG = "GoogleAuth";

    // Firebase y CredentialManager
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    // UI
    private SignInButton btnGoogle;
    private Button btnCerrarSesion;
    private TextView tvEstado;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_logueo_google);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar servicios
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        // Vistas
        btnGoogle = findViewById(R.id.btnGoogleSignIn);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesionGoogle);
        tvEstado = findViewById(R.id.txtEstadoGoogle);

        // Listeners
        btnGoogle.setOnClickListener(v -> lanzarGoogleSignIn());
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void lanzarGoogleSignIn() {
        String webClientId = getString(R.string.default_web_client_id);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Petición asíncrona compatible mediante CredentialManagerCallback
        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                getMainExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        procesarResultadoCredential(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        Log.e(TAG, "Error en CredentialManager", e);
                        tvEstado.setText("Error al conectar con Google");
                    }
                }
        );
    }

    private void procesarResultadoCredential(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential &&
                credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {

            try {
                GoogleIdTokenCredential googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.getData());

                String idToken = googleIdTokenCredential.getIdToken();
                firebaseAuthConGoogle(idToken);

            } catch (Exception e) {
                Log.e(TAG, "Error al extraer token de Google", e);
            }
        }
    }

    private void firebaseAuthConGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "signInWithCredential:success");
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        tvEstado.setText("Fallo en la autenticación con Firebase");
                        updateUI(null);
                    }
                });
    }

    private void cerrarSesion() {
        mAuth.signOut();
        Toast.makeText(this, "Sesión de Google cerrada", Toast.LENGTH_SHORT).show();
        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            tvEstado.setText("Sesión iniciada como:\n" + user.getDisplayName() + "\n(" + user.getEmail() + ")");
        } else {
            tvEstado.setText("No logueado");
        }
    }
}