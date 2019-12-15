package eu.javimar.notitas.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import eu.javimar.notitas.model.Nota;

@Dao
public interface NotasDao
{
    @Insert
    long insert(Nota nota);

    @Query("SELECT * FROM notas ORDER BY notaTitulo ASC")
    LiveData<List<Nota>> getAllNotasSorted();

    @Query("SELECT * FROM notas WHERE notaId = :id")
    Nota findNota(int id);

    @Query("DELETE FROM notas WHERE notaId = :id")
    void deleteNota(int id);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateNote(Nota nota);

    @Query("SELECT * FROM notas WHERE notaTitulo LIKE :query " +
            "OR notaCuerpo LIKE :query ORDER BY notaTitulo ASC")
    List<Nota> getSearchResults(String query);
}
