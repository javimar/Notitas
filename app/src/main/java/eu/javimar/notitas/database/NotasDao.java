package eu.javimar.notitas.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import eu.javimar.notitas.model.Nota;

@Dao
public abstract class NotasDao
{
    @Insert
    public abstract long insert(Nota nota);

    @Query("SELECT * FROM notas ORDER BY notaId")
    public abstract LiveData<List<Nota>> getAllNotasSorted();

    @Query("SELECT * FROM notas WHERE notaId = :id")
    public abstract Nota findNota(int id);

    @Query("DELETE FROM notas WHERE notaId = :id")
    public abstract void deleteNota(int id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract void updateNote(Nota nota);

    @Query("SELECT * FROM notas WHERE notaTitulo LIKE :query " +
            "OR notaCuerpo LIKE :query OR notaEtiqueta LIKE :query ORDER BY notaTitulo ASC")
    public abstract List<Nota> getSearchResults(String query);

    @Query("SELECT * FROM notas ORDER BY notaId")
    public abstract List<Nota> getAllNotasForWidget();

    @Transaction
    public void swapNotas(int i1, int i2)
    {
        // Anything inside this method runs in a single transaction.
        String temp;
        Nota n1 = findNota(i1);
        Nota n2 = findNota(i2);

        temp = n1.getNotaColor();
        n1.setNotaColor(n2.getNotaColor());
        n2.setNotaColor(temp);
        temp = n1.getNotaTitulo();
        n1.setNotaTitulo(n2.getNotaTitulo());
        n2.setNotaTitulo(temp);
        temp = n1.getNotaCuerpo();
        n1.setNotaCuerpo(n2.getNotaCuerpo());
        n2.setNotaCuerpo(temp);
        temp = n1.getNotaEtiqueta();
        n1.setNotaEtiqueta(n2.getNotaEtiqueta());
        n2.setNotaEtiqueta(temp);
        temp = n1.getNotaUriAudio();
        n1.setNotaUriAudio(n2.getNotaUriAudio());
        n2.setNotaUriAudio(temp);
        temp = n1.getNotaUriImage();
        n1.setNotaUriImage(n2.getNotaUriImage());
        n2.setNotaUriImage(temp);

        updateNote(n1);
        updateNote(n2);
    }
}
