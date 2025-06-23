package com.example.rouletteapp;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;
import java.util.UUID;

public class Participant implements Parcelable {
    private String id;
    private String name;
    private int color;
    private boolean canRepeat;
    private int originalIndex;

    public Participant(String name, int color, int originalIndex) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.color = color;
        this.canRepeat = false;
        this.originalIndex = originalIndex;
    }

    // Constructor para Parcelable
    protected Participant(Parcel in) {
        id = in.readString();
        name = in.readString();
        color = in.readInt();
        canRepeat = in.readByte() != 0; // true if byte is not 0
        originalIndex = in.readInt();
    }

    public static final Creator<Participant> CREATOR = new Creator<Participant>() {
        @Override
        public Participant createFromParcel(Parcel in) {
            return new Participant(in);
        }

        @Override
        public Participant[] newArray(int size) {
            return new Participant[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean canRepeat() {
        return canRepeat;
    }

    public void setCanRepeat(boolean canRepeat) {
        this.canRepeat = canRepeat;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(color);
        dest.writeByte((byte) (canRepeat ? 1 : 0)); //if canRepeat == true, byte == 1
        dest.writeInt(originalIndex);
    }

    // Sobrescribir equals y hashCode para manejar correctamente las comparaciones en listas,
    // especialmente si se quieren eliminar duplicados basados solo en el nombre.
    // En este caso, un ID único diferencia instancias, pero para "eliminar duplicados por nombre"
    // se necesitará lógica adicional en el adapter o actividad.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id); // Comparamos por ID para unicidad de instancia
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", canRepeat=" + canRepeat +
                '}';
    }
}
