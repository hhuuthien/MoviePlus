package com.thien.movieplus

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VER) {

    companion object {
        private const val DATABASE_NAME = "movie_plus.db"
        private const val DATABASE_VER = 1
        private const val TABLE_NAME = "cinema"
    }

    val allCinema: List<Cinema>
        get() {
            val listCinema = ArrayList<Cinema>()
            val selectQuery = "SELECT * FROM $TABLE_NAME"
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val cinema =
                        Cinema("", "", "", "", "", "", "")
                    cinema.cumrap = cursor.getString(cursor.getColumnIndex("cumrap"))
                    cinema.tenrap = cursor.getString(cursor.getColumnIndex("tenrap"))
                    cinema.diachi = cursor.getString(cursor.getColumnIndex("diachi"))
                    cinema.thanhpho = cursor.getString(cursor.getColumnIndex("thanhpho"))
                    cinema.quan = cursor.getString(cursor.getColumnIndex("quan"))
                    cinema.gioithieu = cursor.getString(cursor.getColumnIndex("gioithieu"))
                    cinema.hinh = cursor.getString(cursor.getColumnIndex("hinh"))

                    listCinema.add(cinema)
                } while (cursor.moveToNext())
            }
            db.close()
            cursor.close()
            return listCinema
        }

    fun getCinemaALL(cumrap: String, thanhpho: String): ArrayList<Cinema> {
        val list = ArrayList<Cinema>()
        val selectQuery =
            "SELECT * FROM $TABLE_NAME WHERE cumrap=\"$cumrap\" AND thanhpho=\"$thanhpho\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val cinema =
                    Cinema("", "", "", "", "", "", "")
                cinema.cumrap = cursor.getString(cursor.getColumnIndex("cumrap"))
                cinema.tenrap = cursor.getString(cursor.getColumnIndex("tenrap"))
                cinema.diachi = cursor.getString(cursor.getColumnIndex("diachi"))
                cinema.thanhpho = cursor.getString(cursor.getColumnIndex("thanhpho"))
                cinema.quan = cursor.getString(cursor.getColumnIndex("quan"))
                cinema.gioithieu = cursor.getString(cursor.getColumnIndex("gioithieu"))
                cinema.hinh = cursor.getString(cursor.getColumnIndex("hinh"))

                list.add(cinema)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return list
    }

    fun getCinemaALLbyCum(cumrap: String): ArrayList<Cinema> {
        val list = ArrayList<Cinema>()
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE cumrap=\"$cumrap\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val cinema =
                    Cinema("", "", "", "", "", "", "")
                cinema.cumrap = cursor.getString(cursor.getColumnIndex("cumrap"))
                cinema.tenrap = cursor.getString(cursor.getColumnIndex("tenrap"))
                cinema.diachi = cursor.getString(cursor.getColumnIndex("diachi"))
                cinema.thanhpho = cursor.getString(cursor.getColumnIndex("thanhpho"))
                cinema.quan = cursor.getString(cursor.getColumnIndex("quan"))
                cinema.gioithieu = cursor.getString(cursor.getColumnIndex("gioithieu"))
                cinema.hinh = cursor.getString(cursor.getColumnIndex("hinh"))

                list.add(cinema)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return list
    }

    fun getCinemaALLbyCity(thanhpho: String): ArrayList<Cinema> {
        val list = ArrayList<Cinema>()
        val selectQuery =
            "SELECT * FROM $TABLE_NAME WHERE thanhpho=\"$thanhpho\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val cinema =
                    Cinema("", "", "", "", "", "", "")
                cinema.cumrap = cursor.getString(cursor.getColumnIndex("cumrap"))
                cinema.tenrap = cursor.getString(cursor.getColumnIndex("tenrap"))
                cinema.diachi = cursor.getString(cursor.getColumnIndex("diachi"))
                cinema.thanhpho = cursor.getString(cursor.getColumnIndex("thanhpho"))
                cinema.quan = cursor.getString(cursor.getColumnIndex("quan"))
                cinema.gioithieu = cursor.getString(cursor.getColumnIndex("gioithieu"))
                cinema.hinh = cursor.getString(cursor.getColumnIndex("hinh"))

                list.add(cinema)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return list
    }

    override fun onCreate(p0: SQLiteDatabase?) {
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }
}