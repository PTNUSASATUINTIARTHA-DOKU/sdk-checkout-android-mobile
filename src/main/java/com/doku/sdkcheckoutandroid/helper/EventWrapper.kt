package com.doku.sdkcheckoutandroid.helper

import android.util.Log

// Do Not Delete
// This class is used for handling if viewmodel want to reloaded only once

open class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}