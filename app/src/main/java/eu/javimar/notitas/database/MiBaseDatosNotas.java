package eu.javimar.notitas.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import eu.javimar.notitas.model.Nota;

@Database(entities = {Nota.class}, version = 1, exportSchema = false)
public abstract class MiBaseDatosNotas extends RoomDatabase
{
    public abstract NotasDao notasDao();

    // Make the DB a singleton to prevent having multiple instances of the database opened at the same time.
    private static volatile MiBaseDatosNotas INSTANCE;
    public static MiBaseDatosNotas getDatabase(final Context context)
    {
        if (INSTANCE == null)
        {
            synchronized (MiBaseDatosNotas.class)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MiBaseDatosNotas.class, "notitas.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}