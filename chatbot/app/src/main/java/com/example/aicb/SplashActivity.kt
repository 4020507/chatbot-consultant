package com.example.aicb

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.aicb.R
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
import kr.co.shineware.nlp.komoran.core.Komoran

class SplashActivity : AppCompatActivity() {
    companion object{
        lateinit var model:KomoranViewModel
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = ViewModelProvider(this).get(KomoranViewModel::class.java)

        model.setKomoran(Komoran(DEFAULT_MODEL.FULL))

        Handler().postDelayed({
            val Intent = Intent(this,MainActivity::class.java)
            startActivity(Intent)
            finish()
        },2000)
    }
}