package com.example.techchallengedeloitte.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context,DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                COD_DISTRITO + " INTEGER," +
                COD_CONCELHO + " INTEGER," +
                COD_LOCALIDADE + " INTEGER," +
                NOME_LOCALIDADE + " TEXT," +
                COD_ARTERIA + " TEXT," +
                TIPO_ARTERIA + " TEXT," +
                PREP1 + " TEXT," +
                TITULO_ARTERIA + " TEXT," +
                PREP2 + " TEXT," +
                NOME_ARTERIA + " TEXT," +
                LOCAL_ARTERIA + " TEXT," +
                TROCO + " TEXT," +
                PORTA + " TEXT," +
                CLIENTE + " TEXT," +
                NUM_COD_POSTAL + " INTEGER," +
                EXT_COD_POSTAL + " INTEGER," +
                DESIG_POSTAL + " TEXT" + ")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // this method is to check if table already exists
        if (p0 != null) {
            p0.execSQL("DROP TABLE IF EXISTS " + "POSTCODES")
            onCreate(p0)
        }
    }

    fun getNumData() : Int {
        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null).count
    }

    companion object{
        /**
         * here we have defined variables for our database
         */

        //region Data

        private val DATABASE_NAME = "TECH_CHALLENGE_DB"
        private val DATABASE_VERSION = 1
        val TABLE_NAME = "postcodes"

        //endregion

        //region Fields

        val ID_COL = "id"
        val COD_DISTRITO = "cod_distrito"
        val COD_CONCELHO = "cod_concelho"
        val COD_LOCALIDADE = "cod_localidade"
        val NOME_LOCALIDADE = "nome_localidade"
        val COD_ARTERIA = "cod_arteria"
        val TIPO_ARTERIA = "tipo_arteria"
        val PREP1 = "prep1"
        val TITULO_ARTERIA = "titulo_arteria"
        val PREP2 = "prep2"
        val NOME_ARTERIA = "nome_arteria"
        val LOCAL_ARTERIA = "local_arteria"
        val TROCO = "troco"
        val PORTA = "porta"
        val CLIENTE = "cliente"
        val NUM_COD_POSTAL = "num_cod_postal"
        val EXT_COD_POSTAL = "ext_cod_postal"
        val DESIG_POSTAL = "desig_postal"

        //endregion

    }
}