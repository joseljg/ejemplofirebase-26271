package joseluis.ayala.ejemplofirebase26271.clases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Producto.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract ProductoDao productoDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "productos_db"
                    ).allowMainThreadQueries() // Nota: En desarrollo académico
                    .build();
        }
        return INSTANCE;
    }
}