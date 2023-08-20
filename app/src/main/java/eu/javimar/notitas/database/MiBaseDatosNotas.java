package eu.javimar.notitas.database;

import android.content.Context;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import eu.javimar.notitas.model.Nota;

@Database(entities = {Nota.class}, version = 3, exportSchema = false)
public abstract class MiBaseDatosNotas extends RoomDatabase {
    public abstract NotasDao notasDao();

    // Make the DB a singleton to prevent having multiple instances of the database opened at the same time.
    private static volatile MiBaseDatosNotas INSTANCE;

    public static MiBaseDatosNotas getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MiBaseDatosNotas.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MiBaseDatosNotas.class, "notitas.db")
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Migrate from:
     * version 1 - using Room
     * to
     * version 2 - using Room where the table has 2 extra fields
     */
    @VisibleForTesting
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'notas' "
                    + " ADD COLUMN 'notaUriImage' TEXT ");
            database.execSQL("ALTER TABLE 'notas' "
                    + " ADD COLUMN 'notaUriAudio' TEXT ");
        }
    };

    /**
     * Migrate from:
     * version 2 - using Room
     * to
     * version 3 - using Room where the table has 2 extra fields
     */
    @VisibleForTesting
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'notas' "
                    + " ADD COLUMN 'notaReminderOn' INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE 'notas' "
                    + " ADD COLUMN 'notaReminderDate' TEXT");
        }
    };
}