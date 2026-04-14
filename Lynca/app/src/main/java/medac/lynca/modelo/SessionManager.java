package medac.lynca.modelo;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME       = "LyncaSession";
    private static final String KEY_PERFIL_ID   = "perfil_id";
    private static final String KEY_EMAIL       = "email";
    private static final String KEY_NOMBRE      = "nombre_completo";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private static SessionManager instance;

    private SessionManager(Context context) {
        prefs  = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) instance = new SessionManager(context);
        return instance;
    }

    // Guardar sesión tras login/registro
    public void saveSession(String perfilId, String email, String nombre) {
        editor.putString(KEY_PERFIL_ID, perfilId);
        editor.putString(KEY_EMAIL,     email);
        editor.putString(KEY_NOMBRE,    nombre);
        editor.apply();
    }

    // Borrar sesión
    public void clearSession() {
        editor.clear().apply();
        instance = null;
    }

    // Getters
    public String getPerfilId()  { return prefs.getString(KEY_PERFIL_ID, null); }
    public String getEmail()     { return prefs.getString(KEY_EMAIL, ""); }
    public String getNombre()    { return prefs.getString(KEY_NOMBRE, "Usuario"); }
    public boolean isLoggedIn()  { return getPerfilId() != null; }
}