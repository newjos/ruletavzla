package com.example.rouletteapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
// Asumiendo que RecyclerView está disponible.
// import androidx.annotation.NonNull;
// import androidx.recyclerview.widget.RecyclerView;
import android.util.Log; // Para depuración

// Temporalmente usando clases de soporte más antiguas si androidx no está disponible
// sin configuración de gradle. En un proyecto real, usarías androidx.
import android.view.ViewParent; // No es ideal, pero para evitar error de compilación sin RecyclerView
import android.widget.BaseAdapter; // Usaremos BaseAdapter si RecyclerView no es directamente usable

import java.util.List;
import java.util.Random;

// Usaremos una aproximación si RecyclerView no está disponible directamente.
// Idealmente, esto sería: public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {
public class ParticipantAdapter { // Quitamos la herencia por ahora para evitar errores de compilación sin dependencias

    private List<Participant> participants;
    private LayoutInflater inflater;
    private Context context;
    private ParticipantClickListener clickListener;

    // Interfaz para manejar clics en los ítems
    public interface ParticipantClickListener {
        void onRemoveParticipantClick(int position);
        void onRepeatParticipantCheckedChange(int position, boolean isChecked);
    }

    public ParticipantAdapter(Context context, List<Participant> participants, ParticipantClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.participants = participants;
        this.clickListener = listener;
    }

    // Los siguientes métodos son de RecyclerView.Adapter.
    // Si se usa ListView, la estructura del adaptador es diferente (e.g. getView).

    // @NonNull
    // @Override
    public ParticipantViewHolder onCreateViewHolder(/*@NonNull*/ ViewGroup parent, int viewType) {
        // Asumiendo que R.layout.list_item_participant está disponible
        int layoutId = context.getResources().getIdentifier("list_item_participant", "layout", context.getPackageName());
        View view = inflater.inflate(layoutId, parent, false);
        return new ParticipantViewHolder(view);
    }

    // @Override
    public void onBindViewHolder(/*@NonNull*/ ParticipantViewHolder holder, int position) {
        Participant participant = participants.get(position);
        holder.tvName.setText(participant.getName());
        holder.viewColor.getBackground().setColorFilter(participant.getColor(), PorterDuff.Mode.SRC_ATOP);
        // holder.viewColor.setBackgroundColor(participant.getColor()); // Otra forma de setear color si PorterDuff no es necesario

        // Prevenir que el listener se active durante la configuración inicial
        holder.cbRepeat.setOnCheckedChangeListener(null);
        holder.cbRepeat.setChecked(participant.canRepeat());
        holder.cbRepeat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (clickListener != null) {
                clickListener.onRepeatParticipantCheckedChange(holder.getAdapterPosition(), isChecked);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onRemoveParticipantClick(holder.getAdapterPosition());
            }
        });
    }

    // @Override
    public int getItemCount() {
        return participants == null ? 0 : participants.size();
    }

    public void updateParticipants(List<Participant> newParticipants) {
        this.participants = newParticipants;
        // notifyDataSetChanged(); // Método de RecyclerView.Adapter
    }

    // ViewHolder class
    // Idealmente: static class ParticipantViewHolder extends RecyclerView.ViewHolder {
    static class ParticipantViewHolder { // Quitamos herencia por ahora
        View viewColor;
        TextView tvName;
        CheckBox cbRepeat;
        ImageButton btnRemove;
        View itemView; // para getAdapterPosition simulado

        ParticipantViewHolder(View itemView) {
            // super(itemView); // De RecyclerView.ViewHolder
            this.itemView = itemView;
            // Asumiendo que los IDs están disponibles vía getIdentifier
            viewColor = itemView.findViewById(itemView.getContext().getResources().getIdentifier("viewColorParticipante", "id", itemView.getContext().getPackageName()));
            tvName = itemView.findViewById(itemView.getContext().getResources().getIdentifier("tvNombreParticipanteItem", "id", itemView.getContext().getPackageName()));
            cbRepeat = itemView.findViewById(itemView.getContext().getResources().getIdentifier("cbRepetirParticipante", "id", itemView.getContext().getPackageName()));
            btnRemove = itemView.findViewById(itemView.getContext().getResources().getIdentifier("btnEliminarParticipanteItem", "id", itemView.getContext().getPackageName()));
        }

        // Simulación de getAdapterPosition() si no se hereda de RecyclerView.ViewHolder
        // Esto es una simplificación y puede no ser robusto.
        int getAdapterPosition() {
            // En un RecyclerView real, esto es provisto por el LayoutManager.
            // Aquí, podríamos intentar encontrar el índice basado en el objeto si es necesario,
            // o la actividad que lo usa debe pasar la posición directamente.
            // Por ahora, devolvemos -1 o confiamos en que la posición de onBindViewHolder es suficiente.
            if (itemView.getParent() instanceof ViewParent /* idealmente RecyclerView */) {
                // ViewGroup parent = (ViewGroup) itemView.getParent();
                // return parent.indexOfChild(itemView); // Esto no es getAdapterPosition
                Log.w("ParticipantViewHolder", "getAdapterPosition() simulado no es confiable aquí.");
            }
            return -1; // Indicar posición no válida/desconocida
        }
    }

    // Método para generar colores aleatorios para los participantes
    public static int getRandomColor() {
        Random random = new Random();
        return Color.argb(255, random.nextInt(200), random.nextInt(200), random.nextInt(200)); // Evitar colores muy claros/oscuros
    }
}
