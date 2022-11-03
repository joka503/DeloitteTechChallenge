package com.example.techchallengedeloitte.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.techchallengedeloitte.custom.PostalCodes

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context,DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        /*val query = ("CREATE TABLE " + TABLE_NAME + " ("
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
                DESIG_POSTAL + " TEXT," +
                SEARCH_TEXT + " TEXT" +
                ")")

        db.execSQL(query)*/

        val queryVirtual = ("CREATE VIRTUAL TABLE " + TABLE_NAME_VIRTUAL + " USING fts3("
                +NOME_LOCALIDADE + " TEXT," +
                NUM_COD_POSTAL + " INTEGER," +
                EXT_COD_POSTAL + " INTEGER," +
                DESIG_POSTAL + " TEXT," +
                SEARCH_TEXT + " TEXT" +
                ")")

        db.execSQL(queryVirtual)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // this method is to check if table already exists
        if (p0 != null) {
            /**p0.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")**/
            p0.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_VIRTUAL")
            onCreate(p0)
        }
    }

    /**
     * Get number of data saved in database
     */
    fun getNumData() : Int {
        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        return db.rawQuery("SELECT * FROM $TABLE_NAME_VIRTUAL", null).count
    }

    /**
     * Insert all data to database
     */
    fun addData(listPostCodes : List<PostalCodes>) : Boolean{
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (postalCode in listPostCodes){
                var valuesVirtual = ContentValues()
                /*var values = ContentValues()
                values.put(COD_DISTRITO, postalCode.cod_distrito)
                values.put(COD_CONCELHO, postalCode.cod_concelho)
                values.put(COD_LOCALIDADE, postalCode.cod_localidade)
                values.put(NOME_LOCALIDADE, postalCode.nome_localidade)
                values.put(COD_ARTERIA, postalCode.cod_arteria)
                values.put(TIPO_ARTERIA, postalCode.tipo_arteria)
                values.put(PREP1, postalCode.prep1)
                values.put(TITULO_ARTERIA, postalCode.titulo_arteria)
                values.put(PREP2, postalCode.prep2)
                values.put(NOME_ARTERIA, postalCode.nome_arteria)
                values.put(LOCAL_ARTERIA, postalCode.local_arteria)
                values.put(TROCO, postalCode.troco)
                values.put(PORTA, postalCode.porta)
                values.put(CLIENTE, postalCode.cliente)
                values.put(NUM_COD_POSTAL, postalCode.num_cod_postal)
                values.put(EXT_COD_POSTAL, postalCode.ext_cod_postal)
                values.put(DESIG_POSTAL, postalCode.desig_postal)
                values.put(SEARCH_TEXT, postalCode.search_text)
                db.insert(TABLE_NAME,null,values)*/

                valuesVirtual.put(NOME_LOCALIDADE, postalCode.nome_localidade)
                valuesVirtual.put(NUM_COD_POSTAL, postalCode.num_cod_postal)
                valuesVirtual.put(EXT_COD_POSTAL, postalCode.ext_cod_postal)
                valuesVirtual.put(DESIG_POSTAL, postalCode.desig_postal)
                valuesVirtual.put(SEARCH_TEXT, postalCode.search_text)
                db.insert(TABLE_NAME_VIRTUAL,null,valuesVirtual)
            }
            db.setTransactionSuccessful()
        }
        catch (e:java.lang.Exception){
            db.endTransaction()
            return false;
        }
        finally {
            db.endTransaction()
        }
        return true;
    }

    /**
     * Return data filtered by the user input
     */
    fun getData(queryString : String) : List<PostalCodes>?{
        val db = this.readableDatabase
        var cursor = db.rawQuery("SELECT * FROM $TABLE_NAME_VIRTUAL WHERE $SEARCH_TEXT MATCH '$queryString'",null)
        if (cursor.moveToFirst()) {
            var listReturn : List<PostalCodes> = listOf<PostalCodes>()
            do{
                var postalCode = PostalCodes(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4))

                listReturn+=postalCode
            }
                while(cursor.moveToNext())

                return listReturn
        }
        else{
            return null
        }
    }

    companion object{
        /**
         * here we have defined variables for our database
         */

        //region Data

        private val DATABASE_NAME = "TECH_CHALLENGE_DB"
        private val DATABASE_VERSION = 1
        val TABLE_NAME = "postcodes"
        val TABLE_NAME_VIRTUAL = "postcodes_virtual"

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
        val SEARCH_TEXT = "search_text"

        //endregion

    }
}