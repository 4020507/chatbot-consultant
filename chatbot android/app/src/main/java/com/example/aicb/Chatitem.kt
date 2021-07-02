package com.example.aicb

class ChatItem(var chat: String, var type: Int, var action: Int) {

    override fun toString(): String {
        return "ChatItem{" +
                "chat='" + chat + '\'' +
                ", type=" + type +
                ", action=" + action +
                '}'
    }
}