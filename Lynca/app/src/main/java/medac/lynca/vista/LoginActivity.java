package medac.lynca.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.UUID;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;
import medac.lynca.modelo.SupabaseConfig;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button   btnLogin, btnGoogle, btnRegister;
    private TextView tvForgotPassword;

    private FirebaseAuth       mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);

        // Aplicar modo nocturno guardado
        SharedPreferences prefs = getSharedPreferences("LyncaPrefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Launcher para resultado de Google
        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn
                            .getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task
                                .getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Toast.makeText(this,
                                "Error con Google: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Si ya tiene sesión → ir directo al Home
        if (SessionManager.getInstance(this).isLoggedIn()) {
            goToHome();
            return;
        }

        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        btnGoogle        = findViewById(R.id.btnGoogle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnRegister      = findViewById(R.id.btnRegisterAction);

        // Login normal
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString().trim();
            if (!validarDatos(email, pass)) return;

            btnLogin.setEnabled(false);
            btnLogin.setText("Entrando...");

            SupabaseClient.getInstance().login(email, pass,
                    new SupabaseClient.Callback() {
                        @Override
                        public void onSuccess(String body) {
                            try {
                                JSONArray arr = new JSONArray(body);
                                if (arr.length() == 0) {
                                    btnLogin.setEnabled(true);
                                    btnLogin.setText(getString(R.string.inicio_sesion));
                                    Toast.makeText(LoginActivity.this,
                                            "Email o contraseña incorrectos",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                JSONObject perfil = arr.getJSONObject(0);
                                String perfilId   = perfil.getString("id");
                                String userEmail  = perfil.getString("email");
                                String nombre     = perfil.optString(
                                        "nombre_completo", "Usuario");

                                SessionManager.getInstance(LoginActivity.this)
                                        .saveSession(perfilId, userEmail, nombre);
                                goToHome();

                            } catch (Exception e) {
                                btnLogin.setEnabled(true);
                                btnLogin.setText(getString(R.string.inicio_sesion));
                                Toast.makeText(LoginActivity.this,
                                        "Error al procesar respuesta",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            btnLogin.setEnabled(true);
                            btnLogin.setText(getString(R.string.inicio_sesion));
                            Toast.makeText(LoginActivity.this,
                                    "Error de conexión. Inténtalo de nuevo.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Login con Google
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this,
                        RecuperarContrasenaActivity.class)));
    }

    // ════════════════════════════════════════════════════════════
    //  Firebase Auth con Google
    // ════════════════════════════════════════════════════════════
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider
                .getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String email  = user.getEmail();
                            String nombre = user.getDisplayName() != null
                                    ? user.getDisplayName() : "Usuario";
                            buscarOCrearPerfilGoogle(email, nombre);
                        }
                    } else {
                        Toast.makeText(this,
                                "Autenticación fallida",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ════════════════════════════════════════════════════════════
    //  Buscar perfil en Supabase o crear uno nuevo
    // ════════════════════════════════════════════════════════════
    private void buscarOCrearPerfilGoogle(String email, String nombre) {
        // Primero buscar si ya existe el perfil
        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url(SupabaseConfig.REST_URL
                                + "/perfiles?email=eq." + email
                                + "&limit=1")
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .get().build();

                okhttp3.Response response = client.newCall(req).execute();
                String body = response.body() != null
                        ? response.body().string() : "[]";

                runOnUiThread(() -> {
                    try {
                        JSONArray arr = new JSONArray(body);
                        if (arr.length() > 0) {
                            // Perfil ya existe → login directo
                            JSONObject perfil = arr.getJSONObject(0);
                            String perfilId   = perfil.getString("id");
                            String userEmail  = perfil.getString("email");
                            String userNombre = perfil.optString(
                                    "nombre_completo", nombre);
                            SessionManager.getInstance(this)
                                    .saveSession(perfilId, userEmail, userNombre);
                            goToHome();
                        } else {
                            // Perfil no existe → crear nuevo
                            crearPerfilGoogle(email, nombre);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Error al procesar el perfil",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error de conexión",
                                Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void crearPerfilGoogle(String email, String nombre) {
        try {
            String nuevoId = UUID.randomUUID().toString();
            JSONObject perfil = new JSONObject();
            perfil.put("id",              nuevoId);
            perfil.put("email",           email);
            perfil.put("nombre_completo", nombre);
            perfil.put("password",        UUID.randomUUID().toString());
            perfil.put("rol",             "USER");
            perfil.put("esta_baneado",    false);

            String body = perfil.toString();

            new Thread(() -> {
                try {
                    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                    okhttp3.MediaType JSON = okhttp3.MediaType.get(
                            "application/json");
                    okhttp3.Request req = new okhttp3.Request.Builder()
                            .url(SupabaseConfig.REST_URL + "/perfiles")
                            .addHeader("apikey", SupabaseConfig.ANON_KEY)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Prefer", "return=representation")
                            .post(okhttp3.RequestBody.create(body, JSON))
                            .build();

                    okhttp3.Response response = client.newCall(req).execute();
                    String respBody = response.body() != null
                            ? response.body().string() : "[]";

                    runOnUiThread(() -> {
                        try {
                            JSONArray arr = new JSONArray(respBody);
                            if (arr.length() > 0) {
                                JSONObject p  = arr.getJSONObject(0);
                                String pid    = p.getString("id");
                                String pemail = p.getString("email");
                                String pnom   = p.optString(
                                        "nombre_completo", nombre);
                                SessionManager.getInstance(this)
                                        .saveSession(pid, pemail, pnom);
                                goToHome();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this,
                                    "Error al crear perfil",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this,
                                    "Error de conexión",
                                    Toast.LENGTH_SHORT).show());
                }
            }).start();
        } catch (Exception e) {
            Toast.makeText(this, "Error inesperado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private boolean validarDatos(String email, String pass) {
        if (email.isEmpty()) {
            etEmail.setError("Introduce tu email"); return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido"); return false;
        }
        if (pass.isEmpty()) {
            etPassword.setError("Introduce tu contraseña"); return false;
        }
        return true;
    }
}