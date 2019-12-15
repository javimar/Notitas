package eu.javimar.notitas.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Update;

@Entity(tableName = "notas")
public class Nota implements Parcelable
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notaId")
    @Update(onConflict = OnConflictStrategy.REPLACE)
    private int notaId;

    @ColumnInfo(name = "notaTitulo")
    private final String notaTitulo;

    @ColumnInfo(name = "notaCuerpo")
    private final String notaCuerpo;

    @ColumnInfo(name = "notaEtiqueta")
    private final String notaEtiqueta;

    @ColumnInfo(name = "notaColor")
    private final String notaColor;

    public Nota(int notaId, String notaTitulo, String notaCuerpo,
                String notaEtiqueta, String notaColor)
    {
        this.notaId = notaId;
        this.notaTitulo = notaTitulo;
        this.notaCuerpo = notaCuerpo;
        this.notaEtiqueta = notaEtiqueta;
        this.notaColor = notaColor;
    }

    @Ignore
    public Nota(String notaTitulo, String notaCuerpo,
                String notaEtiqueta, String notaColor)
    {
        this.notaTitulo = notaTitulo;
        this.notaCuerpo = notaCuerpo;
        this.notaEtiqueta = notaEtiqueta;
        this.notaColor = notaColor;
    }

    public int getNotaId() {
        return notaId;
    }

    public String getNotaTitulo() {
        return notaTitulo;
    }

    public String getNotaCuerpo() {
        return notaCuerpo;
    }

    public String getNotaColor() {
        return notaColor;
    }

    public String getNotaEtiqueta() {
        return notaEtiqueta;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.notaId);
        dest.writeString(this.notaTitulo);
        dest.writeString(this.notaCuerpo);
        dest.writeString(this.notaEtiqueta);
        dest.writeString(this.notaColor);
    }

    protected Nota(Parcel in) {
        this.notaId = in.readInt();
        this.notaTitulo = in.readString();
        this.notaCuerpo = in.readString();
        this.notaEtiqueta = in.readString();
        this.notaColor = in.readString();
    }

    public static final Parcelable.Creator<Nota> CREATOR = new Parcelable.Creator<Nota>() {
        @Override
        public Nota createFromParcel(Parcel source) {
            return new Nota(source);
        }

        @Override
        public Nota[] newArray(int size) {
            return new Nota[size];
        }
    };
}
