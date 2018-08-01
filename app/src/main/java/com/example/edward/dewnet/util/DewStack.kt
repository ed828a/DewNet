package com.example.edward.dewnet.util

import android.util.Log

/**
 * Created by Edward on 7/7/2018.
 */
class DewStack<T>(val size: Int) {
    private var deep = 0
    private var counter = -1
    private var cursor = 0
    private var previousPosition = 0
    private val box = Array<Any>(size) { 0 }

    fun push(item: T) {
        previousPosition = cursor
        ++counter
        cursor = (counter % size)
        box[cursor] = item as Any
        if (deep < size) ++deep

        Log.d("DewStack", "push: deep = $deep")
    }

    fun pop(): T? {
        return if (deep == 1) {
            Log.d("DewStack", "pop: deep = $deep")
            null
        } else {
            --deep
            previousPosition = cursor
            --counter
            cursor = (counter % size)
            if (cursor == -1) cursor = 0
            Log.d("DewStack", "pop: deep = $deep")
            box[cursor] as T
        }
    }
}