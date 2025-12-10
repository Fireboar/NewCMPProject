package ch.hslu.newcmpproject

import android.content.Context

object ContextHolder {
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}

