package com.example.rouletteapp;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer; // Para sonidos
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class RouletteActivity extends Activity {

    private RouletteView rouletteView;
    private Button btnGirarRuleta;
    private Button btnVolverRoulette;
    private Button btnVerGanadores;
    private TextView tvNombreRuletaJuego;

    private ArrayList<Participant> participantsList;
    private ArrayList<Participant> initialParticipantsList; // Para restaurar si es necesario
    private ArrayList<String> winnersList = new ArrayList<>(); // Para guardar los ganadores
    private String nombreRuleta;
    private boolean isSpinning = false;
    private Random random = new Random();
    private ObjectAnimator spinAnimator;

    // private MediaPlayer spinSoundPlayer;
    // private MediaPlayer winSoundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("activity_roulette", "layout", getPackageName()));

        rouletteView = findViewById(getResources().getIdentifier("rouletteView", "id", getPackageName()));
        btnGirarRuleta = findViewById(getResources().getIdentifier("btnGirarRuleta", "id", getPackageName()));
        btnVolverRoulette = findViewById(getResources().getIdentifier("btnVolverRoulette", "id", getPackageName()));
        btnVerGanadores = findViewById(getResources().getIdentifier("btnVerGanadores", "id", getPackageName()));
        tvNombreRuletaJuego = findViewById(getResources().getIdentifier("tvNombreRuletaJuego", "id", getPackageName()));

        nombreRuleta = getIntent().getStringExtra("NOMBRE_RULETA");
        // Hacer una copia defensiva de la lista
        ArrayList<Participant> receivedList = getIntent().getParcelableArrayListExtra("LISTA_PARTICIPANTES");
        if (receivedList != null) {
            participantsList = new ArrayList<>(receivedList);
            initialParticipantsList = new ArrayList<>(receivedList); // Guardar copia original
        } else {
            participantsList = new ArrayList<>();
            initialParticipantsList = new ArrayList<>();
            Toast.makeText(this, "Error: No se recibieron participantes.", Toast.LENGTH_LONG).show();
            finish(); // Volver si no hay datos
            return;
        }


        if (nombreRuleta != null) {
            tvNombreRuletaJuego.setText(nombreRuleta);
        }

        if (participantsList != null && !participantsList.isEmpty()) {
            rouletteView.setParticipants(participantsList);
        } else {
            Toast.makeText(this, getStringIdentifier("no_hay_suficientes_participantes"), Toast.LENGTH_LONG).show();
            // Considerar deshabilitar el botón de girar o volver
        }

        // initSoundPlayers(); // Inicializar reproductores de sonido

        btnGirarRuleta.setOnClickListener(v -> startSpin());
        btnVolverRoulette.setOnClickListener(v -> finish());
        btnVerGanadores.setOnClickListener(v -> {
            Intent winnersIntent = new Intent(RouletteActivity.this, WinnersActivity.class);
            winnersIntent.putExtra("NOMBRE_RULETA", nombreRuleta);
            winnersIntent.putStringArrayListExtra("LISTA_GANADORES", winnersList);
            startActivity(winnersIntent);
        });
    }

    /*
    private void initSoundPlayers() {
        try {
            // Suponiendo que tienes spin_sound.mp3 y win_sound.mp3 en res/raw
            // spinSoundPlayer = MediaPlayer.create(this, getResources().getIdentifier("spin_sound", "raw", getPackageName()));
            // winSoundPlayer = MediaPlayer.create(this, getResources().getIdentifier("win_sound", "raw", getPackageName()));
        } catch (Exception e) {
            Log.e("RouletteActivity", "Error inicializando sonidos", e);
        }
    }

    private void playSpinSound() {
        if (spinSoundPlayer != null) {
            spinSoundPlayer.seekTo(0);
            spinSoundPlayer.start();
        }
    }

    private void stopSpinSound() {
        if (spinSoundPlayer != null && spinSoundPlayer.isPlaying()) {
            spinSoundPlayer.pause(); // o stop() si no se va a reanudar pronto
        }
    }

    private void playWinSound() {
        if (winSoundPlayer != null) {
            winSoundPlayer.seekTo(0);
            winSoundPlayer.start();
        }
    }
    */

    private void startSpin() {
        if (isSpinning) {
            Toast.makeText(this, getStringIdentifier("ruleta_girando"), Toast.LENGTH_SHORT).show();
            return;
        }
        if (participantsList == null || participantsList.isEmpty()) {
            Toast.makeText(this, getStringIdentifier("no_hay_suficientes_participantes"), Toast.LENGTH_SHORT).show();
            return;
        }

        isSpinning = true;
        btnGirarRuleta.setEnabled(false);
        // playSpinSound();

        int numParticipants = participantsList.size();
        float sectionAngle = 360f / numParticipants;

        // Rotación aleatoria: varias vueltas completas + ángulo para un sector
        int randomRotations = 5 + random.nextInt(5); // Entre 5 y 9 vueltas completas
        int winningIndex = random.nextInt(numParticipants);

        // El indicador está en la parte superior (270 grados en un círculo matemático, o -90).
        // Queremos que el *inicio* del sector ganador termine en la posición del indicador.
        // El ángulo final debe ser tal que el puntero (que está a 0° o 360° relativo a la vista,
        // o más bien 270° si el 0° está a la derecha) apunte al medio del sector ganador.
        // Si el indicador está arriba (posición visual de las 12 en punto), y 0° es a la derecha (3 en punto):
        // El ángulo del puntero es 270°.
        // El ángulo de cada sector es sectionAngle.
        // El ángulo del inicio del sector i es i * sectionAngle.
        // El ángulo del medio del sector i es (i * sectionAngle) + (sectionAngle / 2).
        // Queremos que (currentRotation + (medio del sector ganador)) % 360 = 270 (o -90)
        // targetRotation + (winningIndex * sectionAngle + sectionAngle / 2) = 270 (o un múltiplo de 360 + 270)

        // Simplificación: calculamos el ángulo final para que el *inicio* del segmento ganador
        // quede justo después del puntero (que asumimos está en la parte superior, equivalente a -90 o 270 grados).
        // La rotación de la vista es en sentido horario.
        // El ángulo 0 de la ruleta (inicio del primer segmento) debe apuntar a la posición del puntero.
        // El puntero está en la posición fija superior.
        // El ángulo del sector ganador (considerando 0 en la parte superior) es `winningIndex * sectionAngle`.
        // Queremos que la rotación final sea `-(winningIndex * sectionAngle + sectionAngle / 2)` para centrarlo.
        // O más simple: el ángulo final es el negativo del ángulo medio del sector ganador.

        float targetAngle = -( (winningIndex * sectionAngle) + (sectionAngle / 2f) );
        float finalRotation = (randomRotations * 360f) + targetAngle;

        // Asegurarse que la rotación actual se considera para el cálculo
        float currentViewRotation = rouletteView.getCurrentRotation();
        // El valor de 'rotation' en ObjectAnimator es absoluto, no relativo,
        // pero queremos que gire desde su posición actual una cantidad 'finalRotation'.
        // Sin embargo, 'setRotationAngle' en RouletteView toma un ángulo absoluto.
        // El ObjectAnimator debe animar la propiedad 'rotationAngle' de RouletteView.

        spinAnimator = ObjectAnimator.ofFloat(rouletteView, "rotationAngle", rouletteView.getCurrentRotation(), finalRotation);
        spinAnimator.setDuration(3000 + random.nextInt(2000)); // Duración aleatoria entre 3-5 segundos
        spinAnimator.setInterpolator(new DecelerateInterpolator());

        spinAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                isSpinning = false;
                btnGirarRuleta.setEnabled(true);
                // stopSpinSound();
                // playWinSound();

                // El ángulo final de la ruletaView.getRotationAngle() puede tener muchas vueltas.
                // Necesitamos normalizarlo a 0-360 para determinar el ganador.
                float actualFinalAngle = rouletteView.getCurrentRotation() % 360;
                if (actualFinalAngle < 0) actualFinalAngle += 360; // Normalizar a positivo

                // El puntero está en la parte superior (270 grados en el sistema de coordenadas de drawArc,
                // o 0 grados si consideramos que la rotación se aplica y el puntero es fijo).
                // El sector que está bajo el puntero (0 grados o 360 grados) es el ganador.
                // El ángulo que está en la posición 0 (o 360) después de la rotación.
                // Si 'actualFinalAngle' es la rotación de la ruleta, un punto fijo en la ruleta que estaba en 0 grados
                // ahora está en 'actualFinalAngle'. El puntero está en la posición 0 (fija).
                // El sector ganador es aquel cuyo rango angular (después de la rotación) contiene el ángulo 0 (o 360).
                // O, más fácil: el ángulo del puntero es 0. ¿Qué sector está en 0?
                // El ángulo de inicio de cada sector después de la rotación: `(i * sectionAngle + actualFinalAngle) % 360`
                // El índice ganador es aquel `idx` tal que `(idx * sectionAngle + actualFinalAngle)` está más cerca de 0/360
                // O el `winningIndex` que usamos para calcular `targetAngle` debería ser el correcto.

                Participant winner = participantsList.get(winningIndex);
                winnersList.add(winner.getName()); // Guardar el nombre del ganador
                showWinnerDialog(winner);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isSpinning = false;
                btnGirarRuleta.setEnabled(true);
                // stopSpinSound();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        spinAnimator.start();
    }

    private void showWinnerDialog(Participant winner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getStringIdentifier("dialog_ganador_titulo"));
        builder.setMessage(String.format(getStringIdentifier("dialog_ganador_mensaje"), winner.getName()));

        // Si el participante puede ser eliminado (no es el último y no tiene la opción de repetir siempre)
        final boolean canBeEliminated = participantsList.size() > 1 || !winner.canRepeat();

        if (canBeEliminated) {
            builder.setPositiveButton(getStringIdentifier("dialog_opcion_eliminar"), (dialog, which) -> {
                removeParticipant(winner);
                Toast.makeText(this, getStringIdentifier("ganador_eliminado"), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            builder.setNegativeButton(getStringIdentifier("dialog_opcion_dejar"), (dialog, which) -> {
                Toast.makeText(this, getStringIdentifier("ganador_conservado"), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        } else {
            builder.setPositiveButton(getStringIdentifier("dialog_opcion_aceptar"), (dialog, which) -> {
                 dialog.dismiss();
            });
        }
        builder.setCancelable(false); // No se puede cerrar tocando fuera

        // Pequeño retraso para asegurar que la animación de giro ha terminado visualmente
        new Handler().postDelayed(builder::show, 100);
    }

    private void removeParticipant(Participant participantToRemove) {
        // Si el participante no tiene la opción "canRepeat" activada permanentemente,
        // o si es el último participante y "canRepeat" no está activado, se elimina.
        // La lógica de "canRepeat" en el sentido de que un participante puede ganar múltiples veces
        // sin ser eliminado ya está implícita si no se elimina.
        // La opción "repetir" en ConfigActivity podría interpretarse como "puede ganar múltiples veces".
        // Si "canRepeat" significa que NUNCA debe ser eliminado de la lista, incluso si gana,
        // entonces esta lógica debe cambiar.
        // Asumiendo que "eliminar ganador" significa eliminarlo de la ruleta actual.

        boolean removed = false;
        for (int i = 0; i < participantsList.size(); i++) {
            if (participantsList.get(i).getId().equals(participantToRemove.getId())) {
                if (!participantsList.get(i).canRepeat() || participantsList.size() == 1) {
                    participantsList.remove(i);
                    removed = true;
                    break;
                }
            }
        }

        if (removed) {
            rouletteView.setParticipants(participantsList); // Actualizar la vista de la ruleta
            if (participantsList.isEmpty()) {
                 Toast.makeText(this, "¡Todos los participantes han sido seleccionados!", Toast.LENGTH_LONG).show();
                 btnGirarRuleta.setEnabled(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos de MediaPlayer
        // if (spinSoundPlayer != null) {
        //     spinSoundPlayer.release();
        //     spinSoundPlayer = null;
        // }
        // if (winSoundPlayer != null) {
        //     winSoundPlayer.release();
        //     winSoundPlayer = null;
        // }
        if (spinAnimator != null && spinAnimator.isRunning()) {
            spinAnimator.cancel();
        }
    }

    private String getStringIdentifier(String name) {
        return getString(getResources().getIdentifier(name, "string", getPackageName()));
    }
}
