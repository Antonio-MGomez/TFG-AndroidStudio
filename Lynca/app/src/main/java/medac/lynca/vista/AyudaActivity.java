package medac.lynca.vista;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import medac.lynca.R;

public class AyudaActivity extends AppCompatActivity {

    // Preguntas y respuestas
    private final String[] preguntas = {
            "¿Cómo cancelo una reserva?",
            "¿Cómo cambio mi contraseña?",
            "¿Cómo reservo una pista?",
            "¿Puedo cambiar el idioma?"
    };

    private final String[] respuestas = {
            "Ve a tu perfil → Mis reservas → selecciona la reserva y pulsa Cancelar. Las cancelaciones son gratuitas hasta 24h antes.",
            "Ve a Perfil → Editar perfil → introduce tu nueva contraseña y confírmala. Mínimo 6 caracteres.",
            "Busca la pista en el apartado Buscar, selecciónala, elige la fecha y hora disponible y pulsa Reservar.",
            "Sí. Ve a Perfil → Idioma y selecciona entre Español, English, Català, Euskera o Galego."
    };

    private int[] faqIds    = {R.id.faq1, R.id.faq2, R.id.faq3, R.id.faq4};
    private int[] textIds   = {R.id.textFaq1, R.id.textFaq2, R.id.textFaq3, R.id.textFaq4};
    private int[] iconIds   = {R.id.iconFaq1, R.id.iconFaq2, R.id.iconFaq3, R.id.iconFaq4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applyOnCreate(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda);

        // Botón volver
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Setup FAQs accordion
        for (int i = 0; i < faqIds.length; i++) {
            setupFaq(faqIds[i], textIds[i], iconIds[i]);
        }

        // Buscador funcional
        setupBuscador();

        // Botón email
        LinearLayout btnEmail = findViewById(R.id.btnEmail);
        if (btnEmail != null) {
            btnEmail.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(
                        android.content.Intent.ACTION_SENDTO);
                intent.setData(android.net.Uri.parse("mailto:soporte@lynca.com"));
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                        "Soporte Lynca");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            });
        }

        // Botón teléfono
        LinearLayout btnWhatsapp = findViewById(R.id.btnWhatsapp);
        if (btnWhatsapp != null) {
            btnWhatsapp.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(
                        android.content.Intent.ACTION_DIAL);
                intent.setData(android.net.Uri.parse("tel:+34900123456"));
                startActivity(intent);
            });
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Buscador — filtra las FAQs según el texto
    // ════════════════════════════════════════════════════════════
    private void setupBuscador() {
        EditText etBuscar = findViewById(R.id.etBuscarAyuda);
        if (etBuscar == null) return;

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int a,int b,int c) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                String query = s.toString().toLowerCase().trim();
                filtrarFaqs(query);
            }
        });
    }

    private void filtrarFaqs(String query) {
        for (int i = 0; i < faqIds.length; i++) {
            LinearLayout faq = findViewById(faqIds[i]);
            if (faq == null) continue;

            if (query.isEmpty()) {
                faq.setVisibility(View.VISIBLE);
            } else {
                boolean coincide =
                        preguntas[i].toLowerCase().contains(query) ||
                                respuestas[i].toLowerCase().contains(query);

                faq.setVisibility(coincide ? View.VISIBLE : View.GONE);

                // Si coincide, mostrar la respuesta automáticamente
                if (coincide) {
                    TextView text = findViewById(textIds[i]);
                    ImageView icon = findViewById(iconIds[i]);
                    if (text != null) text.setVisibility(View.VISIBLE);
                    if (icon != null) icon.setImageResource(
                            android.R.drawable.arrow_up_float);
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  Accordion FAQ
    // ════════════════════════════════════════════════════════════
    private void setupFaq(int faqId, int textId, int iconId) {
        LinearLayout faq  = findViewById(faqId);
        TextView     text = findViewById(textId);
        ImageView    icon = findViewById(iconId);

        if (faq == null || text == null) return;

        faq.setOnClickListener(v -> {
            if (text.getVisibility() == View.GONE) {
                text.setVisibility(View.VISIBLE);
                if (icon != null) icon.setImageResource(
                        android.R.drawable.arrow_up_float);
            } else {
                text.setVisibility(View.GONE);
                if (icon != null) icon.setImageResource(
                        android.R.drawable.arrow_down_float);
            }
        });
    }
}