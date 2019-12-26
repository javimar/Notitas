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
    private String notaTitulo;

    @ColumnInfo(name = "notaCuerpo")
    private String notaCuerpo;

    @ColumnInfo(name = "notaEtiqueta")
    private String notaEtiqueta;

    @ColumnInfo(name = "notaColor")
    private String notaColor;

    @ColumnInfo(name = "notaUriImage")
    private String notaUriImage;

    @ColumnInfo(name = "notaUriAudio")
    private String notaUriAudio;

    @ColumnInfo(name = "notaReminderOn")
    private int notaReminderOn;

    @ColumnInfo(name = "notaReminderDate")
    private String notaReminderDate;


    public Nota(int notaId, String notaTitulo, String notaCuerpo,
                String notaEtiqueta, String notaColor,
                String notaUriImage, String notaUriAudio,
                int notaReminderOn, String notaReminderDate)
    {
        this.notaId = notaId;
        this.notaTitulo = notaTitulo;
        this.notaCuerpo = notaCuerpo;
        this.notaEtiqueta = notaEtiqueta;
        this.notaColor = notaColor;
        this.notaUriImage = notaUriImage;
        this.notaUriAudio = notaUriAudio;
        this.notaReminderOn = notaReminderOn;
        this.notaReminderDate = notaReminderDate;
    }

    @Ignore
    public Nota(String notaTitulo, String notaCuerpo,
                String notaEtiqueta, String notaColor, String notaUriImage,
                String notaUriAudio, int notaReminderOn, String notaReminderDate)
    {
        this.notaTitulo = notaTitulo;
        this.notaCuerpo = notaCuerpo;
        this.notaEtiqueta = notaEtiqueta;
        this.notaColor = notaColor;
        this.notaUriImage = notaUriImage;
        this.notaUriAudio = notaUriAudio;
        this.notaReminderOn = notaReminderOn;
        this.notaReminderDate = notaReminderDate;
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

    public void setNotaCuerpo(String notaCuerpo) {
        this.notaCuerpo = notaCuerpo;
    }

    public void setNotaEtiqueta(String notaEtiqueta) {
        this.notaEtiqueta = notaEtiqueta;
    }

    public void setNotaColor(String notaColor) {
        this.notaColor = notaColor;
    }

    public void setNotaUriImage(String notaUriImage) {
        this.notaUriImage = notaUriImage;
    }

    public void setNotaUriAudio(String notaUriAudio) {
        this.notaUriAudio = notaUriAudio;
    }

    public void setNotaTitulo(String notaTitulo) {
        this.notaTitulo= notaTitulo;
    }

    public int getNotaReminderOn() {
        return notaReminderOn;
    }

    public void setNotaReminderOn(int notaReminderOn) {
        this.notaReminderOn = notaReminderOn;
    }

    public String getNotaReminderDate() {
        return notaReminderDate;
    }

    public void setNotaReminderDate(String notaReminderDate) {
        this.notaReminderDate = notaReminderDate;
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
        dest.writeInt(this.notaReminderOn);
        dest.writeString(this.notaReminderDate);
    }

    protected Nota(Parcel in) {
        this.notaId = in.readInt();
        this.notaTitulo = in.readString();
        this.notaCuerpo = in.readString();
        this.notaEtiqueta = in.readString();
        this.notaColor = in.readString();
        this.notaUriImage = in.readString();
        this.notaUriAudio = in.readString();
        this.notaReminderOn= in.readInt();
        this.notaReminderDate= in.readString();
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
