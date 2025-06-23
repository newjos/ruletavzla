package com.example.rouletteapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
// import androidx.recyclerview.widget.LinearLayoutManager;
// import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ConfigActivity extends Activity implements ParticipantAdapter.ParticipantClickListener {

    private TextView tvNombreRuletaConfig;
    private EditText etNuevoParticipante;
    private Button btnAgregarParticipante;
    private Button btnBarajarParticipantes;
    // private RecyclerView rvParticipantes; // Asumiendo RecyclerView
    private View rvParticipantes; // Placeholder si RecyclerView no está disponible
    private Button btnEliminarDuplicados;
    private Button btnEliminarTodos;
    private Button btnCrearRuletaConInfo;
    private Button btnVolverConfig;

    private ArrayList<Participant> participantsList;
    private ParticipantAdapter participantAdapter;
    private String nombreRuleta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asumiendo que R.layout.activity_config está disponible
        setContentView(getResources().getIdentifier("activity_config", "layout", getPackageName()));

        nombreRuleta = getIntent().getStringExtra("NOMBRE_RULETA");

        tvNombreRuletaConfig = (TextView) findViewById(getResources().getIdentifier("tvNombreRuletaConfig", "id", getPackageName()));
        etNuevoParticipante = (EditText) findViewById(getResources().getIdentifier("etNuevoParticipante", "id", getPackageName()));
        btnAgregarParticipante = (Button) findViewById(getResources().getIdentifier("btnAgregarParticipante", "id", getPackageName()));
        btnBarajarParticipantes = (Button) findViewById(getResources().getIdentifier("btnBarajarParticipantes", "id", getPackageName()));
        // rvParticipantes = (RecyclerView) findViewById(getResources().getIdentifier("rvParticipantes", "id", getPackageName()));
        rvParticipantes = findViewById(getResources().getIdentifier("rvParticipantes", "id", getPackageName())); // Placeholder
        btnEliminarDuplicados = (Button) findViewById(getResources().getIdentifier("btnEliminarDuplicados", "id", getPackageName()));
        btnEliminarTodos = (Button) findViewById(getResources().getIdentifier("btnEliminarTodos", "id", getPackageName()));
        btnCrearRuletaConInfo = (Button) findViewById(getResources().getIdentifier("btnCrearRuletaConInfo", "id", getPackageName()));
        btnVolverConfig = (Button) findViewById(getResources().getIdentifier("btnVolverConfig", "id", getPackageName()));

        if (nombreRuleta != null) {
            tvNombreRuletaConfig.setText(nombreRuleta);
        }

        participantsList = new ArrayList<>();
        // El adaptador y RecyclerView se comentan/simplifican si las clases no están disponibles
        // sin dependencias de Gradle. En un proyecto real, esto sería:
        /*
        participantAdapter = new ParticipantAdapter(this, participantsList, this);
        rvParticipantes.setLayoutManager(new LinearLayoutManager(this));
        rvParticipantes.setAdapter(participantAdapter);
        */
        // Simulación si RecyclerView no está configurado:
        // En este entorno simulado, la UI del RecyclerView no se renderizará ni funcionará completamente.
        // Se necesitaría un sistema de build (Gradle) para incluir RecyclerView.
        // Por ahora, las operaciones de lista se harán en 'participantsList',
        // y la UI no reflejará los cambios dinámicamente sin el RecyclerView real.

        setupListeners();
    }

    private void setupListeners() {
        btnAgregarParticipante.setOnClickListener(v -> agregarParticipante());

        etNuevoParticipante.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                agregarParticipante();
                return true;
            }
            return false;
        });

        btnBarajarParticipantes.setOnClickListener(v -> barajarParticipantes());
        btnEliminarDuplicados.setOnClickListener(v -> eliminarDuplicados());
        btnEliminarTodos.setOnClickListener(v -> eliminarTodosLosParticipantes());

        btnCrearRuletaConInfo.setOnClickListener(v -> {
            if (participantsList.isEmpty()) {
                Toast.makeText(this, getStringIdentifier("ruleta_sin_participantes"), Toast.LENGTH_SHORT).show();
                return;
            }
            // Navegar a RouletteActivity, pasando la lista de participantes y el nombre de la ruleta
            Intent rouletteIntent = new Intent(ConfigActivity.this, RouletteActivity.class);
            rouletteIntent.putExtra("NOMBRE_RULETA", nombreRuleta);
            // Participant ya es Parcelable, así que podemos pasar la lista.
            rouletteIntent.putParcelableArrayListExtra("LISTA_PARTICIPANTES", participantsList);
            startActivity(rouletteIntent);
            // Toast.makeText(this, "Creando ruleta con " + participantsList.size() + " participantes.", Toast.LENGTH_SHORT).show(); // No necesario si navegamos
        });

        btnVolverConfig.setOnClickListener(v -> finish()); // Vuelve a la actividad anterior (MainActivity)
    }

    private void agregarParticipante() {
        String nombre = etNuevoParticipante.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, getStringIdentifier("ingrese_nombre_participante"), Toast.LENGTH_SHORT).show();
            return;
        }
        // Se asigna un color aleatorio. Participant.java debe tener un constructor que lo acepte.
        Participant newParticipant = new Participant(nombre, ParticipantAdapter.getRandomColor(), participantsList.size());
        participantsList.add(newParticipant);

        // if (participantAdapter != null) participantAdapter.notifyItemInserted(participantsList.size() - 1);
        // Simulación:
        if (participantAdapter != null) participantAdapter.updateParticipants(participantsList);


        etNuevoParticipante.setText("");
        Toast.makeText(this, getStringIdentifier("participante_agregado") + " " + nombre, Toast.LENGTH_SHORT).show();
        // Log.d("ConfigActivity", "Participantes: " + participantsList.toString());
    }

    private void barajarParticipantes() {
        if (participantsList.isEmpty()) {
            Toast.makeText(this, getStringIdentifier("lista_de_participantes_vacia"), Toast.LENGTH_SHORT).show();
            return;
        }
        Collections.shuffle(participantsList);
        // if (participantAdapter != null) participantAdapter.notifyDataSetChanged();
        if (participantAdapter != null) participantAdapter.updateParticipants(participantsList);
        Toast.makeText(this, getStringIdentifier("participantes_barajados"), Toast.LENGTH_SHORT).show();
    }

    private void eliminarDuplicados() {
        if (participantsList.isEmpty()) {
            Toast.makeText(this, getStringIdentifier("lista_de_participantes_vacia"), Toast.LENGTH_SHORT).show();
            return;
        }
        Set<String> nombresVistos = new HashSet<>();
        List<Participant> unicos = new ArrayList<>();
        List<Participant> aEliminar = new ArrayList<>();

        for (Participant p : participantsList) {
            if (!nombresVistos.contains(p.getName().toLowerCase())) {
                nombresVistos.add(p.getName().toLowerCase());
                unicos.add(p);
            } else {
                aEliminar.add(p); // Marcar para eliminar si ya se vio este nombre
            }
        }

        if (!aEliminar.isEmpty()) {
            participantsList.clear();
            participantsList.addAll(unicos);
            // if (participantAdapter != null) participantAdapter.notifyDataSetChanged();
            if (participantAdapter != null) participantAdapter.updateParticipants(participantsList);
            Toast.makeText(this, getStringIdentifier("duplicados_eliminados"), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se encontraron duplicados por nombre.", Toast.LENGTH_SHORT).show();
        }
    }


    private void eliminarTodosLosParticipantes() {
        if (participantsList.isEmpty()) {
            Toast.makeText(this, getStringIdentifier("lista_de_participantes_vacia"), Toast.LENGTH_SHORT).show();
            return;
        }
        participantsList.clear();
        // if (participantAdapter != null) participantAdapter.notifyDataSetChanged();
        if (participantAdapter != null) participantAdapter.updateParticipants(participantsList);
        Toast.makeText(this, getStringIdentifier("todos_los_participantes_eliminados"), Toast.LENGTH_SHORT).show();
    }

    // --- Implementación de ParticipantAdapter.ParticipantClickListener ---
    @Override
    public void onRemoveParticipantClick(int position) {
        if (position >= 0 && position < participantsList.size()) {
            String nombreEliminado = participantsList.get(position).getName();
            participantsList.remove(position);
            // if (participantAdapter != null) participantAdapter.notifyItemRemoved(position);
            // if (participantAdapter != null) participantAdapter.notifyItemRangeChanged(position, participantsList.size()); // Actualizar posiciones
            if (participantAdapter != null) participantAdapter.updateParticipants(participantsList);
            Toast.makeText(this, getStringIdentifier("participante_eliminado") + " " + nombreEliminado, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRepeatParticipantCheckedChange(int position, boolean isChecked) {
        if (position >= 0 && position < participantsList.size()) {
            participantsList.get(position).setCanRepeat(isChecked);
            // No es necesario notificar al adaptador por este cambio si no afecta la vista directamente
            // a menos que el color o algo más cambie basado en esto.
            // Toast.makeText(this, participantsList.get(position).getName() + (isChecked ? " se repetirá" : " no se repetirá"), Toast.LENGTH_SHORT).show();
        }
    }

    // Método helper para obtener strings ya que R.java no está disponible
    private String getStringIdentifier(String name) {
        return getString(getResources().getIdentifier(name, "string", getPackageName()));
    }

    // Para que Participant pueda ser pasado vía Intent, debe ser Parcelable.
    // Esto es un recordatorio para cuando se implemente la navegación a RouletteActivity.
    // Si no se hace Parcelable, se tendrían que pasar los datos de otra forma (e.g. arrays de strings y ints).
}
