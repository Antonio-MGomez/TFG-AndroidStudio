package medac.lynca.vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.json.JSONArray;

import medac.lynca.R;
import medac.lynca.modelo.SessionManager;
import medac.lynca.modelo.SupabaseClient;

public class PerfilActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "LyncaPrefs";
    private static final String KEY_DARK   = "dark_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplicar idioma guardado
        LanguageHelper.applyOnCreate(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        SessionManager session = SessionManager.getInstance(this);

        // ── Nombre y email ────────────────────────────────────────
        TextView tvNombre = findViewById(R.id.tvNombrePerfil);
        TextView tvEmail  = findViewById(R.id.tvEmailPerfil);
        if (tvNombre != null) tvNombre.setText(session.getNombre());
        if (tvEmail  != null) tvEmail.setText(session.getEmail());

        // ── Idioma actual en el TextView ──────────────────────────
        TextView tvIdiomaActual = findViewById(R.id.tvIdiomaActual);
        if (tvIdiomaActual != null) {
            tvIdiomaActual.setText(
                    getNombreIdioma(LanguageHelper.getSavedLanguage(this)));
        }

        // ── Estadísticas ──────────────────────────────────────────
        cargarEstadisticas(session.getPerfilId());

        // ── Mis Reservas ──────────────────────────────────────────
        LinearLayout btnMisReservas = findViewById(R.id.btnMisReservas);
        if (btnMisReservas != null) {
            btnMisReservas.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
        }

        // ── Editar Perfil ─────────────────────────────────────────
        LinearLayout btnEditar = findViewById(R.id.btnEditarPerfil);
        if (btnEditar != null) {
            btnEditar.setOnClickListener(v ->
                    Toast.makeText(this,
                            getString(R.string.proximamente),
                            Toast.LENGTH_SHORT).show());
        }

        // ── Modo Nocturno ─────────────────────────────────────────
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

        // ── Selector de Idioma ────────────────────────────────────
        LinearLayout btnIdioma = findViewById(R.id.btnIdioma);
        if (btnIdioma != null) {
            btnIdioma.setOnClickListener(v -> mostrarSelectorIdioma());
        }

        // ── Cerrar Sesión ─────────────────────────────────────────
        LinearLayout btnCerrar = findViewById(R.id.btnCerrarSesion);
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> cerrarSesion());
        }

        // ── Eliminar Cuenta ───────────────────────────────────────
        LinearLayout btnEliminar = findViewById(R.id.btnEliminarCuenta);
        if (btnEliminar != null) {
            btnEliminar.setOnClickListener(v -> confirmarEliminarCuenta());
        }

        // ── Bottom nav ────────────────────────────────────────────
        BottomNavHelper.setup(this, "profile");
    }

    // ════════════════════════════════════════════════════════════
    //  Selector de idioma
    // ════════════════════════════════════════════════════════════
    private void mostrarSelectorIdioma() {
        String[] idiomas = {
                "Español",
                "English",
                "Català",
                "Euskera",
                "Galego"
        };
        String[] codigos = { "es", "en", "ca", "eu", "gl" };

        String actual = LanguageHelper.getSavedLanguage(this);
        int seleccionado = 0;
        for (int i = 0; i < codigos.length; i++) {
            if (codigos[i].equals(actual)) {
                seleccionado = i;
                break;
            }
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