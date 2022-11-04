package com.example.techchallengedeloitte.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.techchallengedeloitte.custom.PostalCodes

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val queryVirtual = ("CREATE VIRTUAL TABLE " + TABLE_NAME_VIRTUAL + " USING fts3("
                + NOME_LOCALIDADE + " TEXT," +
                NUM_COD_POSTAL + " INTEGER," +
                EXT_COD_POSTAL + " INTEGER," +
                DESIG_POSTAL + " TEXT," +
                SEARCH_TEXT + " TEXT" +
                ")")

        db.execSQL(queryVirtual)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        if (p0 != null) {
            /**p0.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")**/
            p0.execSQL("DROP TABLE IF EXISTS $TABLE_NAME_VIRTUAL")
            onCreate(p0)
        }
    }

    /**
     * Get number of data saved in database
     */
    fun getNumData(): Int {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME_VIRTUAL", null).count
    }

    /**
     * Insert all data to database
     */
    fun addData(listPostCodes: List<PostalCodes>): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            for (postalCode in listPostCodes) {
                var valuesVirtual = ContentValues()
                valuesVirtual.put(NOME_LOCALIDADE, postalCode.nome_localidade)
                valuesVirtual.put(NUM_COD_POSTAL, postalCode.num_cod_postal)
                valuesVirtual.put(EXT_COD_POSTAL, postalCode.ext_cod_postal)
                valuesVirtual.put(DESIG_POSTAL, postalCode.desig_postal)
                valuesVirtual.put(SEARCH_TEXT, postalCode.search_text)
                db.insert(TABLE_NAME_VIRTUAL, null, valuesVirtual)
            }
            db.setTransactionSuccessful()
        } catch (e: java.lang.Exception) {
            db.endTransaction()
            return false;
        } finally {
            db.endTransaction()
        }
        return true;
    }

    /**
     * Return data filtered by the user input
     */
    fun getData(queryString: String): List<PostalCodes>? {
        val db = this.readableDatabase
        var cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME_VIRTUAL WHERE $SEARCH_TEXT MATCH '$queryString'",
            null
        )
        if (cursor.moveToFirst()) {
            var listReturn: List<PostalCodes> = listOf<PostalCodes>()
            do {
                var postalCode = PostalCodes(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getString(4)
                )

                listReturn += postalCode
            } while (cursor.moveToNext())

            return listReturn
        } else {
            return null
        }
    }

    companion object {
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