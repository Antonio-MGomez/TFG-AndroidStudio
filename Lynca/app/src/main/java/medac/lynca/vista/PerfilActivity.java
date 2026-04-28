package medac.lynca.vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONArray;
import org.json.JSONObject;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;
import medac.lynca.modelo.SupabaseConfig;

public class PerfilActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LyncaPrefs";
    private static final String KEY_DARK   = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        setupUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SessionManager session = SessionManager.getInstance(this);

        // Recargar nombre y email
        TextView tvNombre = findViewById(R.id.tvNombrePerfil);
        TextView tvEmail  = findViewById(R.id.tvEmailPerfil);
        if (tvNombre != null) tvNombre.setText(session.getNombre());
        if (tvEmail  != null) tvEmail.setText(session.getEmail());

        // Recargar foto circular
        ImageView imgPerfil = findViewById(R.id.imgPerfil);
        if (imgPerfil != null) {
            cargarFotoPerfil(imgPerfil, session.getPerfilId());
        }

        // Recargar idioma
        TextView tvIdiomaActual = findViewById(R.id.tvIdiomaActual);
        if (tvIdiomaActual != null) {
            tvIdiomaActual.setText(
                    getNombreIdioma(LanguageHelper.getSavedLanguage(this)));
        }

        // Recargar deporte favorito
        String deporte = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString("deporte_favorito", "Tenis");
        TextView tvDeporte = findViewById(R.id.tvDeporteFavorito);
        if (tvDeporte != null) {
            switch (deporte) {
                case "Tenis":      tvDeporte.setText("🎾"); break;
                case "Pádel":      tvDeporte.setText("🏓"); break;
                case "Fútbol":     tvDeporte.setText("⚽"); break;
                case "Baloncesto": tvDeporte.setText("🏀"); break;
                default:           tvDeporte.setText("🎾"); break;
            }
        }

        cargarEstadisticas(session.getPerfilId());
        cargarFechaRegistro(session.getPerfilId());
    }

    private void setupUI() {
        SessionManager session = SessionManager.getInstance(this);

        // Nombre y email
        TextView tvNombre = findViewById(R.id.tvNombrePerfil);
        TextView tvEmail  = findViewById(R.id.tvEmailPerfil);
        if (tvNombre != null) tvNombre.setText(session.getNombre());
        if (tvEmail  != null) tvEmail.setText(session.getEmail());

        // Foto circular
        ImageView imgPerfil = findViewById(R.id.imgPerfil);
        if (imgPerfil != null) {
            cargarFotoPerfil(imgPerfil, session.getPerfilId());
        }

        // Mis Reservas
        LinearLayout btnMisReservas = findViewById(R.id.btnMisReservas);
        if (btnMisReservas != null) {
            btnMisReservas.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
        }

        // Editar Perfil
        LinearLayout btnEditar = findViewById(R.id.btnEditarPerfil);
        if (btnEditar != null) {
            btnEditar.setOnClickListener(v ->
                    startActivity(new Intent(this, EditarPerfilActivity.class)));
        }

        // Modo Nocturno
        Switch switchNocturno = findViewById(R.id.switchModoNocturno);
        if (switchNocturno != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean darkMode = prefs.getBoolean(KEY_DARK, false);
            switchNocturno.setChecked(darkMode);

            switchNocturno.setOnCheckedChangeListener(
                    (CompoundButton btn, boolean isChecked) -> {
                        prefs.edit().putBoolean(KEY_DARK, isChecked).apply();
                        if (isChecked) {
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_YES);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        recreate();
                    });
        }

        // Selector de Idioma
        LinearLayout btnIdioma = findViewById(R.id.btnIdioma);
        if (btnIdioma != null) {
            btnIdioma.setOnClickListener(v -> mostrarSelectorIdioma());
        }

        // Ayuda
        LinearLayout btnAyuda = findViewById(R.id.btnAyudaSoporte);
        if (btnAyuda != null) {
            btnAyuda.setOnClickListener(v ->
                    startActivity(new Intent(this, AyudaActivity.class)));
        }

        // Términos
        LinearLayout btnTerminos = findViewById(R.id.btnTerminos);
        if (btnTerminos != null) {
            btnTerminos.setOnClickListener(v ->
                    startActivity(new Intent(this, TerminosActivity.class)));
        }

        // Cerrar Sesión
        LinearLayout btnCerrar = findViewById(R.id.btnCerrarSesion);
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> cerrarSesion());
        }

        // Eliminar Cuenta
        LinearLayout btnEliminar = findViewById(R.id.btnEliminarCuenta);
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> confirmarEliminarCuenta());
        }

        BottomNavHelper.setup(this, "profile");
    }

    // ════════════════════════════════════════════════════════════
    //  Foto de perfil circular
    // ════════════════════════════════════════════════════════════
    private void cargarFotoPerfil(ImageView imgView, String perfilId) {
        if (perfilId == null) {
            Glide.with(this).load(R.drawable.foto_jose)
                    .transform(new CircleCrop()).into(imgView);
            return;
        }

        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url(SupabaseConfig.REST_URL
                                + "/perfiles?id=eq." + perfilId
                                + "&select=foto_url")
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .get().build();

                okhttp3.Response response = client.newCall(req).execute();
                String body = response.body() != null
                        ? response.body().string() : "[]";

                runOnUiThread(() -> {
                    try {
                        JSONArray arr = new JSONArray(body);
                        String fotoUrl = "";
                        if (arr.length() > 0) {
                            fotoUrl = arr.getJSONObject(0)
                                    .optString("foto_url", "");
                        }
                        if (!fotoUrl.isEmpty()) {
                            Glide.with(PerfilActivity.this)
                                    .load(fotoUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.foto_jose)
                                    .error(R.drawable.foto_jose)
                                    .into(imgView);
                        } else {
                            Glide.with(PerfilActivity.this)
                                    .load(R.drawable.foto_jose)
                                    .transform(new CircleCrop())
                                    .into(imgView);
                        }
                    } catch (Exception e) {
                        Glide.with(PerfilActivity.this)
                                .load(R.drawable.foto_jose)
                                .transform(new CircleCrop())
                                .into(imgView);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Glide.with(PerfilActivity.this)
                                .load(R.drawable.foto_jose)
                                .transform(new CircleCrop())
                                .into(imgView));
            }
        }).start();
    }

    // ════════════════════════════════════════════════════════════
    //  Fecha de registro
    // ════════════════════════════════════════════════════════════
    private void cargarFechaRegistro(String perfilId) {
        if (perfilId == null) return;

        new Thread(() -> {
            try {
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.Request req = new okhttp3.Request.Builder()
                        .url(SupabaseConfig.REST_URL
                                + "/perfiles?id=eq." + perfilId
                                + "&select=fecha_creacion")
                        .addHeader("apikey", SupabaseConfig.ANON_KEY)
                        .get().build();

                okhttp3.Response response = client.newCall(req).execute();
                String body = response.body() != null
                        ? response.body().string() : "[]";

                runOnUiThread(() -> {
                    try {
                        JSONArray arr = new JSONArray(body);
                        if (arr.length() > 0) {
                            JSONObject perfil = arr.getJSONObject(0);
                            String fechaRaw = perfil.optString(
                                    "fecha_creacion", "");
                            if (!fechaRaw.isEmpty()) {
                                String[] partes = fechaRaw.split("T")[0].split("-");
                                if (partes.length == 3) {
                                    String fechaFormato = partes[2] + "/"
                                            + partes[1] + "/"
                                            + partes[0].substring(2);
                                    TextView tvFecha = findViewById(
                                            R.id.tvFechaRegistro);
                                    if (tvFecha != null)
                                        tvFecha.setText(fechaFormato);
                                }
                            }
                        }
                    } catch (Exception e) { /* ignorar */ }
                });
            } catch (Exception e) { /* ignorar */ }
        }).start();
    }

    // ════════════════════════════════════════════════════════════
    //  Estadísticas
    // ════════════════════════════════════════════════════════════
    private void cargarEstadisticas(String perfilId) {
        if (perfilId == null) return;
        SupabaseClient.getInstance().getReservas(perfilId,
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) {
                        try {
                            JSONArray arr = new JSONArray(body);
                            TextView tvTotal = findViewById(R.id.tvTotalReservas);
                            if (tvTotal != null)
                                tvTotal.setText(String.valueOf(arr.length()));
                        } catch (Exception e) { /* ignorar */ }
                    }
                    @Override
                    public void onError(String error) { /* ignorar */ }
                });
    }

    // ════════════════════════════════════════════════════════════
    //  Selector de idioma
    // ════════════════════════════════════════════════════════════
    private void mostrarSelectorIdioma() {
        String[] idiomas = {"Español","English","Català","Euskera","Galego"};
        String[] codigos = {"es","en","ca","eu","gl"};

        String actual = LanguageHelper.getSavedLanguage(this);
        int seleccionado = 0;
        for (int i = 0; i < codigos.length; i++) {
            if (codigos[i].equals(actual)) { seleccionado = i; break; }
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.seleccionar_idioma))
                .setSingleChoiceItems(idiomas, seleccionado,
                        (dialog, which) -> {
                            dialog.dismiss();
                            LanguageHelper.setLanguage(this, codigos[which]);
                        })
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    private String getNombreIdioma(String code) {
        switch (code) {
            case "en": return "English";
            case "ca": return "Català";
            case "eu": return "Euskera";
            case "gl": return "Galego";
            default:   return "Español";
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Cerrar sesión
    // ════════════════════════════════════════════════════════════
    private void cerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cerrar_sesion))
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Sí", (d, w) -> {
                    SessionManager.getInstance(this).clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    // ════════════════════════════════════════════════════════════
    //  Eliminar cuenta
    // ════════════════════════════════════════════════════════════
    private void confirmarEliminarCuenta() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.eliminar_cuenta))
                .setMessage("Esta acción es irreversible. " +
                        "Se borrarán tu cuenta y todas tus reservas. " +
                        "¿Continuar?")
                .setPositiveButton(getString(R.string.eliminar_cuenta),
                        (d, w) -> eliminarCuenta())
                .setNegativeButton(getString(R.string.cancelar), null)
                .show();
    }

    private void eliminarCuenta() {
        SessionManager session = SessionManager.getInstance(this);
        String perfilId = session.getPerfilId();

        if (perfilId == null) {
            Toast.makeText(this, "Error: sesión no válida",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        SupabaseClient.getInstance().deleteAccount(perfilId,
                new SupabaseClient.Callback() {
                    @Override
                    public void onSuccess(String body) {
                        Toast.makeText(PerfilActivity.this,
                                "Cuenta eliminada correctamente",
                                Toast.LENGTH_LONG).show();
                        session.clearSession();
                        Intent intent = new Intent(PerfilActivity.this,
                                LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onError(String error) {
                        Toast.makeText(PerfilActivity.this,
                                "Error al eliminar cuenta: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}