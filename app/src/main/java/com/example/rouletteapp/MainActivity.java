package com.example.rouletteapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private EditText etNombreRuleta;
    private Button btnCrearRuleta;
    private Button btnCompartirWhatsApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asumimos que el nombre del archivo de layout es activity_main.xml
        // y que R.layout.activity_main se generará correctamente.
        // Si la herramienta de compilación no genera R.java, esto fallará en tiempo de compilación.
        setContentView(getResources().getIdentifier("activity_main", "layout", getPackageName()));

        etNombreRuleta = (EditText) findViewById(getResources().getIdentifier("etNombreRuleta", "id", getPackageName()));
        btnCrearRuleta = (Button) findViewById(getResources().getIdentifier("btnCrearRuleta", "id", getPackageName()));
        btnCompartirWhatsApp = (Button) findViewById(getResources().getIdentifier("btnCompartirWhatsApp", "id", getPackageName()));

        btnCrearRuleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreRuleta = etNombreRuleta.getText().toString().trim();
                if (nombreRuleta.isEmpty()) {
                    // Usar el string de resources si está disponible, sino el literal.
                    String mensaje = "Por favor, ingrese un nombre para la ruleta.";
                    try {
                        int resId = getResources().getIdentifier("ingrese_nombre_ruleta", "string", getPackageName());
                        if (resId != 0) mensaje = getString(resId);
                    } catch (Exception e) { /* Usar el mensaje por defecto */ }
                    Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                } else {
                    Intent configIntent = new Intent(MainActivity.this, ConfigActivity.class);
                    configIntent.putExtra("NOMBRE_RULETA", nombreRuleta);
                    startActivity(configIntent);
                }
            }
        });

        btnCompartirWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica para compartir por WhatsApp
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta increíble app de ruleta!");
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");

                try {
                    startActivity(sendIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "WhatsApp no está instalado.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
