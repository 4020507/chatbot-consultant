package com.example.aicb

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pedro.library.AutoPermissions
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var classification: Classification
    private lateinit var reply: Array<Array<String?>>
    private var reply_length = 0
    private lateinit var answer: Array<String?>
    private lateinit var map:Fragment
    companion object{
        var hospital: List<hospital_info>? = null
    }


    override fun onStart() {
        super.onStart()
        Log.d("onStart", "running successfully")

        //사전 준비
        classification.load()

        //답변 준비
        //komoran = new Komoran(DEFAULT_MODEL.FULL);

        //답변 준비
        reply = Array<Array<String?>>(600) { arrayOfNulls(2) }
        try {
            load_reply(applicationContext.assets)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("reply", "replay failed")
        }

        answer = arrayOfNulls<String>(10)

        map = MapFragment()

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AutoPermissions.loadAllPermissions((applicationContext as Activity?)!!, 101)
            return
        }

    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        classification = Classification(applicationContext)

    }

    @Throws(IOException::class)
    fun load_reply(assetManager: AssetManager) {
        reply_length = 0
        Log.d("load_reply", "loading replying.txt")

        assetManager.open("reply.txt").use { ins ->
            BufferedReader(InputStreamReader(ins)).use { reader ->
                while (reader.ready()) {
                    val line = reader.readLine().split("\t").toTypedArray()
                    reply[reply_length][0] = line[0]
                    reply[reply_length][1] = line[1]
                    reply_length++
                }
            }
        }
    }

    class hospital_info(var name: String,var latitdue: Double,var longitude: Double,var internet_address: String,var number: String)
}