package com.example.rouletteapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
// import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RouletteView extends View {

    private List<Participant> participants = new ArrayList<>();
    private Paint sectionPaint;
    private Paint textPaint;
    private Paint linePaint; // Para las líneas entre secciones
    private RectF rouletteRect;
    private float currentRotation = 0f; // Grados de rotación actual

    public RouletteView(Context context) {
        super(context);
        init();
    }

    public RouletteView(Context context, /*@Nullable*/ AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RouletteView(Context context, /*@Nullable*/ AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        sectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectionPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE); // Color de texto por defecto
        textPaint.setTextSize(30); // Tamaño de texto, ajustar según sea necesario
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE); // Color de las líneas divisorias
        linePaint.setStrokeWidth(2); // Grosor de las líneas

        rouletteRect = new RectF();
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = (participants != null) ? new ArrayList<>(participants) : new ArrayList<>();
        invalidate(); // Redibujar la vista cuando los participantes cambian
    }

    public void setRotationAngle(float degrees) {
        this.currentRotation = degrees % 360;
        invalidate(); // Redibujar con la nueva rotación
    }

    public float getCurrentRotation() {
        return currentRotation;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Define el rectángulo en el que se dibujará la ruleta (un círculo)
        float padding = 20; // Espacio para que el texto no se corte en los bordes
        float diameter = Math.min(w, h) - 2 * padding;
        float left = (w - diameter) / 2;
        float top = (h - diameter) / 2;
        float right = left + diameter;
        float bottom = top + diameter;
        rouletteRect.set(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (participants == null || participants.isEmpty()) {
            // Opcional: dibujar algo si no hay participantes (e.g., un círculo vacío o texto)
            sectionPaint.setColor(Color.LTGRAY);
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, rouletteRect.width() / 2f, sectionPaint);
            textPaint.setColor(Color.DKGRAY);
            canvas.drawText("Vacío", getWidth() / 2f, getHeight() / 2f + textPaint.getTextSize() / 3, textPaint);
            return;
        }

        float sweepAngle = 360f / participants.size();
        float startAngle = currentRotation; // Iniciar el dibujo desde el ángulo de rotación actual

        // Guardar el estado del canvas antes de rotar para el texto
        canvas.save();
        // Rotar el canvas completo para la animación de giro
        // canvas.rotate(currentRotation, getWidth() / 2f, getHeight() / 2f); // No, la rotación se aplica al startAngle

        for (int i = 0; i < participants.size(); i++) {
            Participant p = participants.get(i);
            sectionPaint.setColor(p.getColor());

            // Dibujar la sección (arco)
            canvas.drawArc(rouletteRect, startAngle, sweepAngle, true, sectionPaint);

            // Dibujar la línea divisoria
            float lineEndX = (float) (getWidth() / 2f + rouletteRect.width() / 2f * Math.cos(Math.toRadians(startAngle)));
            float lineEndY = (float) (getHeight() / 2f + rouletteRect.height() / 2f * Math.sin(Math.toRadians(startAngle)));
            canvas.drawLine(getWidth() / 2f, getHeight() / 2f, lineEndX, lineEndY, linePaint);


            // Calcular la posición del texto
            // El texto se dibuja en una ruta para que siga la curvatura o se posicione correctamente
            float textAngle = startAngle + sweepAngle / 2; // Ángulo central de la sección
            Path textPath = new Path();
            // Crear un arco para la ruta del texto, ligeramente más pequeño que la sección
            RectF textArcRect = new RectF(rouletteRect.left + rouletteRect.width() * 0.2f,
                                        rouletteRect.top + rouletteRect.height() * 0.2f,
                                        rouletteRect.right - rouletteRect.width() * 0.2f,
                                        rouletteRect.bottom - rouletteRect.height() * 0.2f);
            textPath.addArc(textArcRect, startAngle, sweepAngle);

            // Ajustar el tamaño del texto si es muy largo
            String participantName = p.getName();
            float maxTextWidth = rouletteRect.width() * 0.35f; // Aprox.
            textPaint.setTextSize(30); // Reset text size
            while(textPaint.measureText(participantName) > maxTextWidth && textPaint.getTextSize() > 10) {
                textPaint.setTextSize(textPaint.getTextSize() - 1);
            }

            // Dibujar el texto a lo largo de la ruta
            // El offset horizontal (hOffset) centra el texto en el arco.
            // El offset vertical (vOffset) mueve el texto "hacia adentro" o "hacia afuera" del centro.
            // Estos cálculos son aproximados y pueden necesitar ajustes.
            float textRadius = textArcRect.width() / 2f;
            float circumference = (float) (Math.PI * textRadius);
            float arcLength = (sweepAngle / 360f) * circumference;
            float hOffset = (arcLength / 2f) - (textPaint.measureText(participantName) / 2f);

            textPaint.setColor(getContrastColor(p.getColor())); // Asegurar contraste del texto
            canvas.drawTextOnPath(participantName, textPath, hOffset, textPaint.getTextSize() * 0.5f /* vOffset */, textPaint);

            startAngle += sweepAngle;
        }
        // Restaurar el canvas si se guardó
         canvas.restore();
    }

    // Determina si usar texto blanco o negro basado en el color de fondo
    private int getContrastColor(int backgroundColor) {
        // Contar YIQ del color de fondo
        double y = (299 * Color.red(backgroundColor) + 587 * Color.green(backgroundColor) + 114 * Color.blue(backgroundColor)) / 1000.0;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }
}
