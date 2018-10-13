package com.pratamalabs.furqan.repository;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.pratamalabs.furqan.FurqanApp;
import com.pratamalabs.furqan.FurqanSettings;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by pratamalabs on 9/6/13.
 */
@EBean(scope = EBean.Scope.Singleton)
public class FurqanDatabase extends SQLiteOpenHelper {

    public final static String DATABASE_PATH = "/data/data/com.pratamalabs.furqan/databases/";
    private static final int DATABASE_VERSION = 4;
    private static String DATABASE_NAME = "furqan.sqlite";
    private static String DATABASE_VERSION_TAG = "dbversion.txt";
    private final Context dbContext;

    FurqanSettings settings = FurqanSettings.get();
    private SQLiteDatabase dataBase;


    public static FurqanDatabase get() {
        return FurqanDatabase_.getInstance_(FurqanApp.instance);
    }

    public FurqanDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.dbContext = context;
        // checking database and open it if exists
        if (checkDataBase()) {
            openDataBase();
        } else {
            try {
                this.getReadableDatabase();
                copyDataBase();
                this.close();
                openDataBase();

            } catch (IOException e) {
                throw new Error("Error copying database");
            }
            Toast.makeText(context, "Initial database is created", Toast.LENGTH_LONG).show();
        }
    }

    void upgrade1DbLink(SQLiteDatabase sqLiteDatabase) {
        String update1 = "UPDATE Sources SET DownloadLink = replace( DownloadLink, 'http://andikapratama.com/data/', 'https://raw.githubusercontent.com/andikapratama/furqan/master/app/QuranData/ExtraTranslations/' ), UpdateLink = replace( UpdateLink, 'http://andikapratama.com/data/', 'https://raw.githubusercontent.com/andikapratama/furqan/master/app/QuranData/ExtraTranslations/' ) WHERE Id IN (45,108,109,110,111)";
        sqLiteDatabase.execSQL(update1);
    }

    void addedIndonesianTransliteration(SQLiteDatabase sqLiteDatabase) {
        String update1 = "INSERT INTO Sources (Id,Type,Name,Author,DownloadLink,UpdateLink,LastModifiedDate,Language,ProviderName,Status,Ordering,TanzilId) " +
                "VALUES (111,?,?,?,?,?,?,?,?,?,111,?)";


        sqLiteDatabase.execSQL(update1, new Object[]{
                "Translation",
                "Transliterasi Indonesia",
                "Unknown",
                "https://raw.githubusercontent.com/andikapratama/furqan/master/app/QuranData/ExtraTranslations/transliterasiIndonesia.txt",
                "https://raw.githubusercontent.com/andikapratama/furqan/master/app/QuranData/ExtraTranslations/transliterasiIndonesia.txt",
                "2016-2-15",
                "Indonesian",
                "Unknown",
                "notdownloaded",
                "pratama.id.transliterasi"
        });
    }

    private void setDbVersion() throws IOException {
        String dbVersionPath = DATABASE_PATH + DATABASE_VERSION_TAG;
        FileUtils.write(new File(dbVersionPath), String.valueOf(DATABASE_VERSION));
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = dbContext.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();

        setDbVersion();

        //set default folding
        PreferenceManager.getDefaultSharedPreferences(dbContext.getApplicationContext())
                .edit()
                .putBoolean("pratama.eng.jalalain", true)
                .putBoolean("pratama.eng.asbabalnuzul", true)
                .commit();
    }

    public void openDataBase() throws SQLException {
        String dbPath = DATABASE_PATH + DATABASE_NAME;
        dataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        checkAndUpdateVersion(dataBase);
    }

    private void checkAndUpdateVersion(SQLiteDatabase sqLiteDatabase) {

        String dbVersionPath = DATABASE_PATH + DATABASE_VERSION_TAG;
        int dbLocalVersion;
        try {
            File file = new File(dbVersionPath);
            String version = FileUtils.readFileToString(file);
            dbLocalVersion = Integer.parseInt(version);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (dbLocalVersion < DATABASE_VERSION) {
            upgrade1DbLink(sqLiteDatabase);
            if (dbLocalVersion < 3) {
                addedIndonesianTransliteration(sqLiteDatabase);
            }
            try {
                setDbVersion();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;

        boolean exist = false;
        try {
            String dbPath = DATABASE_PATH + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(dbPath, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.v("db log", "database does't exist");
        }

        if (checkDB != null) {
            exist = true;
            checkDB.close();
        }
        return exist;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
    }
}
