package com.example.aicb

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

class ChatitemView: LinearLayout {
    constructor(context: Context) : super(context)

    lateinit var chat1: TextView
    lateinit var chat2: TextView

    private fun init_user(context: Context)
    {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.user_item, this, true)
        chat1 = findViewById<View>(R.id.comment) as TextView
    }

    private fun init_bot(context: Context)
    {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.bot_item, this, true)
        chat2 = findViewById<View>(R.id.comment) as TextView
    }

    fun setChat(chat: String, check: Int, context: Context) {
        //check이 0이면 from user, 1이면 from chatbot
        if (check == 0) {
            init_user(context!!)
            val layoutParams: RelativeLayout.LayoutParams
            if (chat.length < 18) {
                layoutParams = RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
                chat1.layoutParams = layoutParams
            }
            else {
                layoutParams = RelativeLayout.LayoutParams(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f,resources.displayMetrics
                    ).toInt(), LayoutParams.WRAP_CONTENT
                )
                chat1.layoutParams = layoutParams
            }
            layoutParams.setMargins(0, 0, 0, 15) // llp.setMargins(left, top, right, bottom);
            chat1.gravity = Gravity.RIGHT
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            chat1.text = chat
        } else {
            init_bot(context!!)
            val layoutParams: RelativeLayout.LayoutParams
            if (chat.length < 18) {
                layoutParams = RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
                chat2.layoutParams = layoutParams
            }
            else {
                layoutParams = RelativeLayout.LayoutParams(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250f,resources.displayMetrics
                    ).toInt(), LayoutParams.WRAP_CONTENT
                )
                chat2.layoutParams = layoutParams
            }
            layoutParams.setMargins(0, 0, 0, 15) // llp.setMargins(left, top, right, bottom);
            chat2.text = chat
        }
    }
}

