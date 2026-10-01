package joseluis.ayala.ejemplofirebase26271;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView txtHolaMain;
    private Button btnProductos, btnCerrarSesionMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        txtHolaMain = findViewById(R.id.txtHolaMain);
        btnProductos = findViewById(R.id.btnProductos);
        btnCerrarSesionMain = findViewById(R.id.btnCerrarSesionMain);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            abrirLoginEmail();
            return;
        }

        String nombre = user.getDisplayName();
        String identificador = (nombre != null && !nombre.trim().isEmpty())
                ? nombre
                : user.getEmail();
        txtHolaMain.setText("Bienvenido, " + identificador);

        btnProductos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CrudProductos.class)));

        btnCerrarSesionMain.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            abrirLoginEmail();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void abrirLoginEmail() {
        Intent intent = new Intent(MainActivity.this, LogueoEmail.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
