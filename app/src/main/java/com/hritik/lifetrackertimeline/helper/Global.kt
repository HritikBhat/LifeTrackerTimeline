package com.hritik.lifetrackertimeline.helper

import android.util.Log

object Global {
    const val isAppOnTest = true
    
    fun log(tag: String, message: String, throwable: Throwable? = null) {
        if (isAppOnTest) {
            if (throwable != null) {
                Log.e("LTT_$tag", message, throwable)
            } else {
                Log.d("LTT_$tag", message)
            }
        }
    }
}
