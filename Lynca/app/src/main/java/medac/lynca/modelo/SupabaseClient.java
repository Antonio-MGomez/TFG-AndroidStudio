package medac.lynca.modelo;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static SupabaseClient instance;
    private final OkHttpClient http;
    private final Handler mainHandler;

    public interface Callback {
        void onSuccess(String responseBody);
        void onError(String errorMessage);
    }

    private SupabaseClient() {
        http = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static SupabaseClient getInstance() {
        if (instance == null) instance = new SupabaseClient();
        return instance;
    }

    // ════════════════════════════════════════════════════════════
    //  AUTH — Login
    // ════════════════════════════════════════════════════════════
    public void login(String email, String password, Callback callback) {
        String url = SupabaseConfig.REST_URL
                + "/perfiles?email=eq." + email
                + "&password=eq." + password
                + "&limit=1";

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        executeAsync(req, callback);
    }

    // ════════════════════════════════════════════════════════════
    //  AUTH — Registro
    // ════════════════════════════════════════════════════════════
    public void register(String email, String password,
                         String nombreCompleto, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email",           email);
            body.put("password",        password);
            body.put("nombre_completo", nombreCompleto);
            body.put("rol",             "USER");
            body.put("esta_baneado",    false);

            Request req = new Request.Builder()
                    .url(SupabaseConfig.REST_URL + "/perfiles")
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            executeAsync(req, callback);
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════
    //  RESERVAS — Obtener con datos de pista
    // ════════════════════════════════════════════════════════════
    public void getReservas(String perfilId, Callback callback) {
        String url = SupabaseConfig.REST_URL
                + "/reservas?perfil_id=eq." + perfilId
                + "&order=fecha_creacion.desc"
                + "&select=id,perfil_id,pista_id,fecha_reserva,"
                + "hora_inicio,hora_fin,estado_reserva,fecha_creacion,"
                + "pistas(nombre,tipo_deporte,precio_hora,"
                + "instalaciones(direccion),"
                + "imagenes_pista(url_imagen,es_principal))";

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        executeAsync(req, callback);
    }

    // ════════════════════════════════════════════════════════════
    //  RESERVAS — Crear
    // ════════════════════════════════════════════════════════════
    public void insertReserva(JSONObject reserva, Callback callback) {
        Request req = new Request.Builder()
                .url(SupabaseConfig.REST_URL + "/reservas")
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(RequestBody.create(reserva.toString(), JSON))
                .build();

        executeAsync(req, callback);
    }

    // ════════════════════════════════════════════════════════════
    //  RESERVAS — Cancelar
    // ════════════════════════════════════════════════════════════
    public void deleteReserva(String reservaId, Callback callback) {
        Request req = new Request.Builder()
                .url(SupabaseConfig.REST_URL + "/reservas?id=eq." + reservaId)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .delete()
                .build();

        executeAsync(req, callback);
    }

    // ════════════════════════════════════════════════════════════
    //  PISTAS — Con imágenes y dirección
    // ════════════════════════════════════════════════════════════
    public void getPistasConImagenes(Callback callback) {
        String url = SupabaseConfig.REST_URL
                + "/pistas?select=id,nombre,tipo_deporte,precio_hora,"
                + "descripcion,instalaciones(direccion),"
                + "imagenes_pista(url_imagen,es_principal)";

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        executeAsync(req, callback);
    }

    // ════════════════════════════════════════════════════════════
    //  PISTAS — Con filtro de deporte
    // ════════════════════════════════════════════════════════════
    public void getPistas(String tipoDeporte, Callback callback) {
        String url = SupabaseConfig.REST_URL + "/pistas?estado=eq.disponible";
        if (tipoDeporte != null && !tipoDeporte.isEmpty()) {
            url += "&tipo_deporte=eq." + tipoDeporte;
        }

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        executeAsync(req, callback);
    }

    // ════════════════════════════════════════════════════════════
    //  CUENTA — Eliminar
    // ════════════════════════════════════════════════════════════
    public void deleteAccount(String perfilId, Callback callback) {
        Request req = new Request.Builder()
                .url(SupabaseConfig.REST_URL + "/perfiles?id=eq." + perfilId)
                .addHeader("apikey", SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .delete()
                .build();

        executeAsync(req, callback);
    }

    // ════════════════════════════════════════════════════════════
    //  INTERNO
    // ════════════════════════════════════════════════════════════
    private void executeAsync(Request request, Callback callback) {
        http.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() ->
                        callback.onError("Sin conexión: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null
                        ? response.body().string() : "[]";
                if (response.isSuccessful()) {
                    mainHandler.post(() -> callback.onSuccess(body));
                } else {
                    mainHandler.post(() ->
                            callback.onError("Error " + response.code() + ": " + body));
                }
            }
        });
    }
}