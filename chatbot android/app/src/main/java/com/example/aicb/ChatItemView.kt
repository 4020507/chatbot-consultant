package com.example.aicb

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView

class ChatItemView(context: Context?) : LinearLayout(context) {
    var chat1: TextView? = null
    var chat2: TextView? = null

    //user일 때와 bot일 때 서로 다른 layout을 받음
    private fun init_user(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.user_item, this, true)
        chat1 = findViewById(R.id.comment)
    }

    private fun init_bot(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.bot_item, this, true)
        chat2 = findViewById(R.id.comment)
    }

    @SuppressLint("RtlHardcoded")
    fun setChat(chat: String, check: Int, context: Context) {

        //check가 0이면 user, 1이면 bot
        if (check == 0) {
            init_user(context)
            val layoutParams: RelativeLayout.LayoutParams

            //길이가 길면 여러 줄, 짧으면 한 줄로 set
            if (chat.length < 18) {
                layoutParams = RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                )

                chat1!!.layoutParams = layoutParams
            }
            else {
                layoutParams = RelativeLayout.LayoutParams(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 250f, resources.displayMetrics
                    ).toInt(), LayoutParams.WRAP_CONTENT
                )
                chat1!!.layoutParams = layoutParams
            }
            layoutParams.setMargins(0, 0, 0, 15) // llp.setMargins(left, top, right, bottom);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            chat1!!.apply{
                gravity = Gravity.RIGHT
                text = chat
                setBackgroundResource(R.drawable.yellow)
                setTextColor(Color.BLACK)
            }
        }
        else {
            init_bot(context)
            val layoutParams: RelativeLayout.LayoutParams
            if (chat.length < 18) {
                layoutParams = RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                )
               chat2!!.layoutParams = layoutParams
            } else {
                layoutParams = RelativeLayout.LayoutParams(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 250f,
                        resources.displayMetrics
                    ).toInt(), LayoutParams.WRAP_CONTENT
                )
                chat2!!.layoutParams = layoutParams
            }
            layoutParams.setMargins(0, 0, 0, 15) // llp.setMargins(left, top, right, bottom);

            chat2!!.apply{
                gravity = Gravity.LEFT
                text = chat
                setBackgroundResource(R.drawable.purple)
                setTextColor(Color.WHITE)
            }
        }
    }
}