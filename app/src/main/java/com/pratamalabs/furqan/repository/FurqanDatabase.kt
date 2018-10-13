package com.pratamalabs.furqan.repository

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.pratamalabs.furqan.FurqanApp
import com.pratamalabs.furqan.FurqanSettings
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by pratamalabs on 9/6/13.
 */
class FurqanDatabase(private val dbContext: Context) : SQLiteOpenHelper(dbContext, DATABASE_NAME, null, DATABASE_VERSION) {

    internal var settings = FurqanSettings.get()
    private var dataBase: SQLiteDatabase? = null

    init {
        // checking database and open it if exists
        if (checkDataBase()) {
            openDataBase()
        } else {
            try {
                this.readableDatabase
                copyDataBase()
                this.close()
                openDataBase()

            } catch (e: IOException) {
                throw Error("Error copying database")
            }

            Toast.makeText(dbContext, "Initial database is created", Toast.LENGTH_LONG).show()
        }
    }

    internal fun upgrade1DbLink(sqLiteDatabase: SQLiteDatabase?) {
        val update1 = "UPDATE Sources SET DownloadLink = replace( DownloadLink, 'http://andikapratama.com/data/', 'https://raw.githubusercontent.com/andikapratama/furqan/master/app/QuranData/ExtraTranslations/' ), UpdateLink = replace( UpdateLink, 'http://andikapratama.com/data/', 'https://raw.githubusercontent.com/andikapratama/furqan/master/app/QuranData/ExtraTranslations/' ) WHERE Id IN (45,108,109,110,111)";
        sqLiteDatabase?.execSQL(update1);
    }

    internal fun addedIndonesianTransliteration(sqLiteDatabase: SQLiteDatabase?) {
        val update1 = "INSERT INTO Sources (Id,Type,Name,Author,DownloadLink,UpdateLink,LastModifiedDate,Language,ProviderName,Status,Ordering,TanzilId) " + "VALUES (111,?,?,?,?,?,?,?,?,?,111,?)"


        sqLiteDatabase!!.execSQL(update1, arrayOf<Any>("Translation", "Transliterasi Indonesia", "Unknown", "https://raw.githubusercontent.com/andikapratama/furqan/master/app/QuranData/ExtraTranslations/transliterasiIndonesia.txt", "https://raw.githubusercontent.com/andikapratama/furqan/master/app/QuranData/ExtraTranslations/transliterasiIndonesia.txt", "2016-2-15", "Indonesian", "Unknown", "notdownloaded", "pratama.id.transliterasi"))
    }

    @Throws(IOException::class)
    private fun setDbVersion() {
        val dbVersionPath = DATABASE_PATH + DATABASE_VERSION_TAG
        FileUtils.write(File(dbVersionPath), DATABASE_VERSION.toString())
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        val myInput = dbContext.assets.open(DATABASE_NAME)
        val outFileName = DATABASE_PATH + DATABASE_NAME
        val myOutput = FileOutputStream(outFileName)

        val buffer = ByteArray(1024)
        var length = myInput.read(buffer)
        while (length > 0) {
            length = myInput.read(buffer)
            myOutput.write(buffer, 0, length)
        }

        myOutput.flush()
        myOutput.close()
        myInput.close()

        setDbVersion()

        //set default folding
        PreferenceManager.getDefaultSharedPreferences(dbContext.applicationContext)
                .edit()
                .putBoolean("pratama.eng.jalalain", true)
                .putBoolean("pratama.eng.asbabalnuzul", true)
                .commit()
    }

    @Throws(SQLException::class)
    fun openDataBase() {
        val dbPath = DATABASE_PATH + DATABASE_NAME
        dataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        checkAndUpdateVersion(dataBase)
    }

    private fun checkAndUpdateVersion(sqLiteDatabase: SQLiteDatabase?) {

        val dbVersionPath = DATABASE_PATH + DATABASE_VERSION_TAG
        val dbLocalVersion: Int
        try {
            val file = File(dbVersionPath)
            val version = FileUtils.readFileToString(file)
            dbLocalVersion = Integer.parseInt(version)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        if (dbLocalVersion < DATABASE_VERSION) {
            upgrade1DbLink(sqLiteDatabase)
            if (dbLocalVersion < 3) {
                addedIndonesianTransliteration(sqLiteDatabase)
            }
            try {
                setDbVersion()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null

        var exist = false
        try {
            val dbPath = DATABASE_PATH + DATABASE_NAME
            checkDB = SQLiteDatabase.openDatabase(dbPath, null,
                    SQLiteDatabase.OPEN_READONLY)
        } catch (e: SQLiteException) {
            Log.v("db log", "database does't exist")
        }

        if (checkDB != null) {
            exist = true
            checkDB.close()
        }
        return exist
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {

    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i2: Int) {}

    companion object {

        val DATABASE_PATH = "/data/data/com.pratamalabs.furqan/databases/"
        private val DATABASE_VERSION = 4
        private val DATABASE_NAME = "furqan.sqlite"
        private val DATABASE_VERSION_TAG = "dbversion.txt"


        fun get(): FurqanDatabase {
            return FurqanDatabase(FurqanApp.instance)
        }
    }
}
