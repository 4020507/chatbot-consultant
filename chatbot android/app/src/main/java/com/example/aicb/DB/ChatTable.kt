package com.example.aicb.DB

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class ChatTable {
    private val Tag = "Chat"
    private var database: SQLiteDatabase? = null
    private val outline = "create table if not exists "
    private val comment = "" +
            "(" +
            "_id integer PRIMARY KEY autoincrement, " +
            "contents text, " +
            "whom integer, " +
            "state integer" +
            ")"
    private var name: String? = null

    fun openDatabase(context: Context?, databaseName: String?) {
        println("ChatTable openDatabase 호출됨")
        name = databaseName
        val helper = commentHelper(context, databaseName, null, 1)
        database = helper.writableDatabase
    }

    fun createTable(tableName: String) {
        println("createTable 호출 됨 : $tableName")
        if (database != null) {
            val table = outline + tableName + comment
            database!!.execSQL(table)
            println("$tableName 테이블 생성 요청됨")
        } else println("데이터베이스를 먼저 오픈하세요")
    }

    fun insert(tableName: String, contents: String, whom: Int, state: Int) {
        println("insert 호출됨")
        if (database != null) {
            val sql = "insert into " + tableName + "(contents, whom, state)" +
                    " values(?, ?, ?)"
            val params = arrayOf<Any>(contents, whom, state)
            database!!.execSQL(sql, params)
            println("채팅 데이터 추가")
        } else println("먼저 데이터 베이스를 오픈하세요")
    }


    fun selectData(tableName: String): Cursor? {
        println("selectData 호출됨")
        var cursor: Cursor? = null
        if (database != null) {
            val sql = "select contents, whom, state from $tableName"
            cursor = database!!.rawQuery(sql, null)
            println("조회된 데이터 개수 : " + cursor.count)
        }
        return cursor
    }

    fun println(data: String?) {
        Log.d(Tag, data!!)
    }

    inner class commentHelper(context: Context?, name: String?, factory: CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
        override fun onCreate(db: SQLiteDatabase) {
            println("onCreate() 호출됨.")
            val sql = "create table if not exists " + name + "(_id integer PRIMARY KEY autoincrement, " +
                    "contents text, " +
                    "whom integer, " +
                    "state integer" +
                    ")"
            db.execSQL(sql)
            println("테이블 생성됨.")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            println("onUpgrade 호출됨 : $oldVersion $newVersion")
            if (newVersion > 1) {
                val tableName: String = name!!
                db.execSQL("drop table if exists $tableName")
                println("테이블 삭제함")
                val sql = "create table if not exists " + tableName + "(_id integer PRIMARY KEY autoincrement, " +
                        "contents text, " +
                        "whom integer, " +
                        "state integer" +
                        ")"
                db.execSQL(sql)
                println("테이블 새로 생성됨.")
            }
        }
    }
}