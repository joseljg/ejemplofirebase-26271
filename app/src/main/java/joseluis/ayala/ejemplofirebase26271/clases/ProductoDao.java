package joseluis.ayala.ejemplofirebase26271.clases;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProductoDao {

    @Query("SELECT * FROM productos")
    List<Producto> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Producto> productos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Producto producto);

    @Delete
    void delete(Producto producto);

    @Query("DELETE FROM productos")
    void deleteAll();
}