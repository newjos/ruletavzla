package com.example.rouletteapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// import androidx.core.app.ActivityCompat; // Para permisos
// import androidx.core.content.ContextCompat; // Para permisos

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WinnersActivity extends Activity {

    private static final int REQUEST_CODE_WRITE_STORAGE_PERMISSION = 101;

    private ListView lvGanadores;
    private TextView tvTituloGanadores;
    private TextView tvNoGanadores;
    private Button btnExportarPdf;
    private Button btnExportarImagen;
    private Button btnVolverWinners;

    private ArrayList<String> winnersList;
    private String nombreRuleta;
    private WinnerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("activity_winners", "layout", getPackageName()));

        lvGanadores = findViewById(getResources().getIdentifier("lvGanadores", "id", getPackageName()));
        tvTituloGanadores = findViewById(getResources().getIdentifier("tvTituloGanadores", "id", getPackageName()));
        tvNoGanadores = findViewById(getResources().getIdentifier("tvNoGanadores", "id", getPackageName()));
        btnExportarPdf = findViewById(getResources().getIdentifier("btnExportarPdf", "id", getPackageName()));
        btnExportarImagen = findViewById(getResources().getIdentifier("btnExportarImagen", "id", getPackageName()));
        btnVolverWinners = findViewById(getResources().getIdentifier("btnVolverWinners", "id", getPackageName()));

        nombreRuleta = getIntent().getStringExtra("NOMBRE_RULETA");
        winnersList = getIntent().getStringArrayListExtra("LISTA_GANADORES");

        if (nombreRuleta != null) {
            tvTituloGanadores.setText(String.format(getStringIdentifier("titulo_ganadores"), nombreRuleta));
        }

        if (winnersList == null) {
            winnersList = new ArrayList<>();
        }

        if (winnersList.isEmpty()) {
            tvNoGanadores.setVisibility(View.VISIBLE);
            lvGanadores.setVisibility(View.GONE);
            btnExportarPdf.setEnabled(false);
            btnExportarImagen.setEnabled(false);
        } else {
            tvNoGanadores.setVisibility(View.GONE);
            lvGanadores.setVisibility(View.VISIBLE);
            adapter = new WinnerAdapter(this, winnersList);
            lvGanadores.setAdapter(adapter);
            btnExportarPdf.setEnabled(true);
            btnExportarImagen.setEnabled(true);
        }

        btnVolverWinners.setOnClickListener(v -> finish());
        btnExportarPdf.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                exportToPdf();
            } else {
                requestStoragePermission();
            }
        });
        btnExportarImagen.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                exportToImage();
            } else {
                requestStoragePermission();
            }
        });
    }

    private boolean checkStoragePermission() {
        // Para Android 6.0 (API 23) y superior, se necesita permiso en tiempo de ejecución.
        // Para versiones inferiores, el permiso en Manifest es suficiente.
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //     return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        // }
        // Simulando que el permiso está concedido si no podemos usar ContextCompat/ActivityCompat
        Log.w("WinnersActivity", "Simulando verificación de permisos. En un entorno real, usar ContextCompat.");
        return true; // Simplificación para este entorno
    }

    private void requestStoragePermission() {
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //     ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE_PERMISSION);
        // }
        Log.w("WinnersActivity", "Simulando solicitud de permisos. En un entorno real, usar ActivityCompat.");
        // En este entorno simulado, no podemos manejar la respuesta, así que asumimos que se deniega o se maneja externamente.
        Toast.makeText(this, getStringIdentifier("permiso_almacenamiento_necesario"), Toast.LENGTH_LONG).show();
    }

    /*
    // @Override
    // public void onRequestPermissionsResult(int requestCode, /*@NonNull* / String[] permissions, /*@NonNull* / int[] grantResults) {
    //    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    //    if (requestCode == REQUEST_CODE_WRITE_STORAGE_PERMISSION) {
    //        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    //            Toast.makeText(this, "Permiso concedido. Intente exportar de nuevo.", Toast.LENGTH_SHORT).show();
    //        } else {
    //            Toast.makeText(this, "Permiso denegado. No se puede exportar.", Toast.LENGTH_SHORT).show();
    //        }
    //    }
    // }
    */

    private File getOutputDirectory() {
        // Guardar en el directorio de Descargas público
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private String generateFileName(String prefix, String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return prefix + "_" + (nombreRuleta != null ? nombreRuleta.replaceAll("\\s+", "_") : "Ruleta") + "_" + timeStamp + "." + extension;
    }

    private void exportToPdf() {
        if (winnersList.isEmpty()) return;

        // Usar PdfDocument de Android (API 19+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Toast.makeText(this, "Exportación a PDF no soportada en esta versión de Android.", Toast.LENGTH_LONG).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);

        float x = 30;
        float y = 50;

        // Título
        paint.setTextSize(20);
        paint.setFakeBoldText(true);
        String title = String.format(getStringIdentifier("lista_ganadores_pdf_titulo"), nombreRuleta != null ? nombreRuleta : "");
        canvas.drawText(title, x, y, paint);
        y += paint.getTextSize() * 2;
        paint.setTextSize(14);
        paint.setFakeBoldText(false);

        // Lista de ganadores
        for (int i = 0; i < winnersList.size(); i++) {
            canvas.drawText((i + 1) + ". " + winnersList.get(i), x, y, paint);
            y += paint.getTextSize() * 1.5f;
            if (y > pageInfo.getPageHeight() - 50 && i < winnersList.size() -1) { // Margen inferior y nueva página si es necesario
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }

        // Marca de agua (logo) - Simplificado
        try {
            // int logoResId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName()); // Asumiendo que existe un logo
            // Drawable d = getResources().getDrawable(logoResId); // Deprecado, usar ContextCompat.getDrawable
            // if (d instanceof BitmapDrawable) {
            //    Bitmap logo = ((BitmapDrawable) d).getBitmap();
            //    Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 100, 100, true);
            //    paint.setAlpha(70); // Opacidad para marca de agua
            //    canvas.drawBitmap(scaledLogo, canvas.getWidth() - scaledLogo.getWidth() - 30, canvas.getHeight() - scaledLogo.getHeight() - 30, paint);
            //    paint.setAlpha(255);
            // }
             paint.setTextSize(10);
             paint.setColor(Color.LTGRAY);
             canvas.save();
             canvas.rotate(-45, canvas.getWidth()/2f, canvas.getHeight()/2f);
             canvas.drawText("Logo App Aquí", canvas.getWidth()/2f, canvas.getHeight()/2f, paint);
             canvas.restore();

        } catch (Exception e) {
            Log.e("WinnersActivity", "Error al cargar/dibujar logo para PDF", e);
        }


        document.finishPage(page);

        File outputFile = new File(getOutputDirectory(), generateFileName("Ganadores", "pdf"));
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            document.writeTo(fos);
            Toast.makeText(this, String.format(getStringIdentifier("exportacion_exitosa_pdf"), outputFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("WinnersActivity", "Error escribiendo PDF", e);
            Toast.makeText(this, getStringIdentifier("exportacion_fallida_pdf"), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private void exportToImage() {
        if (winnersList.isEmpty()) return;

        // Crear un Bitmap de la ListView o una vista compuesta
        // Esto es una simplificación. Para una lista larga, se necesitaría dibujar item por item.
        View viewToCapture = lvGanadores; // O un layout contenedor si quieres el título también
        if (lvGanadores.getChildCount() == 0) { // ListView puede no estar dibujada si no hay items visibles
             Toast.makeText(this, "No hay contenido visible para capturar como imagen.", Toast.LENGTH_SHORT).show();
             return;
        }

        // Calcular altura total de la lista
        int totalHeight = 0;
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                View listItem = adapter.getView(i, null, lvGanadores);
                listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(lvGanadores.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                totalHeight += listItem.getMeasuredHeight();
            }
        }
         if (totalHeight == 0 && viewToCapture.getHeight() > 0) totalHeight = viewToCapture.getHeight();
         if (totalHeight == 0) totalHeight = 500; // Fallback

        Bitmap bitmap = Bitmap.createBitmap(viewToCapture.getWidth(), totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // Fondo blanco

        // Dibujar título
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(40);
        titlePaint.setFakeBoldText(true);
        String title = String.format(getStringIdentifier("titulo_ganadores"), nombreRuleta != null ? nombreRuleta : "");
        float currentY = 60;
        canvas.drawText(title, 30, currentY, titlePaint);
        currentY += 60;

        // Dibujar items de la lista
        Paint itemPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        itemPaint.setColor(Color.DKGRAY);
        itemPaint.setTextSize(30);

        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                 String winnerText = (i + 1) + ". " + adapter.getItem(i).toString();
                 canvas.drawText(winnerText, 40, currentY, itemPaint);
                 currentY += 40; // Espaciado
                 if (currentY > bitmap.getHeight() - 100 && i < adapter.getCount() -1) { // Evitar desbordamiento y dejar espacio para logo
                     // Si la lista es muy larga, este bitmap simple no será suficiente, se cortará.
                     // Se necesitaría un bitmap más alto o paginación.
                     Log.w("WinnersActivity", "Contenido de imagen excede la altura del bitmap, puede estar cortado.");
                     break;
                 }
            }
        }

        // Marca de agua (logo) - Simplificado
        try {
            // ... (lógica similar a la del PDF para cargar y dibujar el logo) ...
             itemPaint.setTextSize(20);
             itemPaint.setColor(Color.LTGRAY);
             canvas.save();
             // Posicionar en el centro o esquina inferior
             float logoX = canvas.getWidth() / 2f;
             float logoY = currentY + 60 > canvas.getHeight() - 60 ? canvas.getHeight() - 30 : currentY + 40; // Ajustar si hay mucho contenido
             if (logoY > canvas.getHeight() - 30) logoY = canvas.getHeight() - 30;

             canvas.translate(logoX, logoY);
             canvas.rotate(-45);
             itemPaint.setTextAlign(Paint.Align.CENTER);
             canvas.drawText("Logo App Aquí", 0, 0, itemPaint);
             canvas.restore();
        } catch (Exception e) {
            Log.e("WinnersActivity", "Error al cargar/dibujar logo para imagen", e);
        }


        File outputFile = new File(getOutputDirectory(), generateFileName("Ganadores", "png"));
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            Toast.makeText(this, String.format(getStringIdentifier("exportacion_exitosa_imagen"), outputFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e("WinnersActivity", "Error guardando imagen", e);
            Toast.makeText(this, getStringIdentifier("exportacion_fallida_imagen"), Toast.LENGTH_SHORT).show();
        }
    }

    private String getStringIdentifier(String name) {
        try {
            return getString(getResources().getIdentifier(name, "string", getPackageName()));
        } catch (Exception e) {
            Log.e("WinnersActivity", "String resource no encontrado: " + name);
            return name; // Fallback al nombre del recurso
        }
    }
}
