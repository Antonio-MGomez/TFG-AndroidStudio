package medac.lynca.vista;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LanguageHelper {

    private static final String PREFS_NAME = "LyncaPrefs";
    private static final String KEY_LANG   = "language";

    // Guardar y aplicar idioma
    public static void setLanguage(Activity activity, String langCode) {
        // Guardar preferencia
        SharedPreferences prefs = activity.getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANG, langCode).apply();

        // Aplicar idioma
        applyLanguage(activity, langCode);

        // Recrear Activity para aplicar cambios
        activity.recreate();
    }

    // Aplicar idioma guardado (llamar al inicio de cada Activity)
    public static void applyLanguage(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    // Obtener idioma guardado
    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, "es"); // español por defecto
    }

    // Aplicar al arrancar la app
    public static void applyOnCreate(Activity activity) {
        String lang = getSavedLanguage(activity);
        applyLanguage(activity, lang);
    }
}