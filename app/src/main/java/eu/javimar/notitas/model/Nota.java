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

    @ColumnInfo(name = "notaUriImage")
    private final String notaUriImage;

    @ColumnInfo(name = "notaUriAudio")
    private final String notaUriAudio;

    public Nota(int notaId, String notaTitulo, String notaCuerpo,
                String notaEtiqueta, String notaColor, String notaUriImage,
                String notaUriAudio)
    {
        this.notaId = notaId;
        this.notaTitulo = notaTitulo;
        this.notaCuerpo = notaCuerpo;
        this.notaEtiqueta = notaEtiqueta;
        this.notaColor = notaColor;
        this.notaUriImage = notaUriImage;
        this.notaUriAudio = notaUriAudio;
    }

    @Ignore
    public Nota(String notaTitulo, String notaCuerpo,
                String notaEtiqueta, String notaColor, String notaUriImage,
                String notaUriAudio)
    {
        this.notaTitulo = notaTitulo;
        this.notaCuerpo = notaCuerpo;
        this.notaEtiqueta = notaEtiqueta;
        this.notaColor = notaColor;
        this.notaUriImage = notaUriImage;
        this.notaUriAudio = notaUriAudio;
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

    public String getNotaUriImage() {
        return notaUriImage;
    }

    public String getNotaUriAudio() {
        return notaUriAudio;
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
        dest.writeString(this.notaUriImage);
        dest.writeString(this.notaUriAudio);
    }

    protected Nota(Parcel in) {
        this.notaId = in.readInt();
        this.notaTitulo = in.readString();
        this.notaCuerpo = in.readString();
        this.notaEtiqueta = in.readString();
        this.notaColor = in.readString();
        this.notaUriImage = in.readString();
        this.notaUriAudio = in.readString();
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
