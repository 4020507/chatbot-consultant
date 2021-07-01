package com.example.aicb

class Chatitem(_chat: String?, _type: Int) {
    var chat = _chat
    var type = _type
    
    override fun toString(): String {
        return "ChatItem{" +
                "chat='" + chat + '\'' +
                ", type=" + type +
                '}'
    }
}