package se.miun.osgu1400.bathingsites;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class SQLDatabase extends SQLiteOpenHelper {

    static final private String DB_NAME = "Bathing_Sites"; // Name of database
    static final private String DB_TABLE_BATHING_SITE = "Bathing_Site"; // Name of the table that stores bathing site data
    static final private int DB_VER = 1; // Version number of database

    private Context context;
    private SQLiteDatabase myDb;

    public SQLDatabase(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
        context = ctx;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table if it does not exists
        String createDb = "CREATE TABLE " + DB_TABLE_BATHING_SITE +
                " (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,description TEXT,address TEXT,longitude TEXT,latitude TEXT,grade FLOAT,water_temp TEXT,date_for_temp TEXT);";
        db.execSQL(createDb);
        Log.i("Database", "Table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop table and create a new when upgrading
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_BATHING_SITE);
        onCreate(db);
    }

    // Returns number of records (bathing sites) in DB_TABLE_BATHING_SITE
    public int numberOfBathingSites() {
        SQLiteDatabase myDb = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + DB_TABLE_BATHING_SITE;
        Cursor cursor = myDb.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // Add data to DB_TABLE_BATHING_SITE
    public void insertData(String name, String description, String address, String longitude, String latitude, float grade, String waterTemp, String dateForTemp) {
        myDb = getWritableDatabase();

        String insertStr = "INSERT INTO " + DB_TABLE_BATHING_SITE +
                " (name,description,address,longitude,latitude,grade,water_temp,date_for_temp)" +
                " VALUES('" + name + "','" + description + "','" + address + "','" + longitude + "','" +
                latitude + "'," + grade + ",'" + waterTemp + "','" + dateForTemp + "');";
        myDb.execSQL(insertStr);
    }

    // Check if a bathing site with the same coordinates already exists in the database
    public boolean bathingSiteExists(String longitude, String latitude) {

        myDb = getReadableDatabase();
        String query = "SELECT * FROM "
                + DB_TABLE_BATHING_SITE
                + " WHERE longitude='"+longitude+"' AND latitude='"+latitude+"'";
        Cursor cursor = myDb.rawQuery(query, null);

        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    // For database control in settings
    public ArrayList<Cursor> getData(String Query) {
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[]{"message"};
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2 = new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try {
            String maxQuery = Query;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[]{"Success"});

            alc.set(1, Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0, c);
                c.moveToFirst();

                return alc;
            }
            return alc;
        } catch (SQLException sqlEx) {
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + sqlEx.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        } catch (Exception ex) {
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[]{"" + ex.getMessage()});
            alc.set(1, Cursor2);
            return alc;
        }
    }

}
