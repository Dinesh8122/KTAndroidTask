package com.example.ktandroidtask.sharedPreference

import android.content.Context
import android.content.SharedPreferences

class SharedPreference {

    fun save(context: Context, key: String?, text: String?) {
        val editor: SharedPreferences.Editor

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        val settings: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) //1
        editor = settings.edit() //2
        editor.putString(key, text) //3
        editor.apply() //4
    }

    fun getValue(context: Context, key: String?): String? {
        val settings: SharedPreferences
        val text: String?
        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        text = settings.getString(key, null)
        return text
    }

    fun clearSharedPreference(context: Context) {
        val settings: SharedPreferences
        val editor: SharedPreferences.Editor
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editor = settings.edit()
        editor.clear()
        editor.apply()
    }



    fun saveBool(context: Context, key: String?, text: Boolean?) {
        val settings: SharedPreferences
        val editor: SharedPreferences.Editor

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) //1
        editor = settings.edit() //2
        editor.putBoolean(key, text!!) //3
        editor.apply() //4
    }

    fun getBooleanValue(context: Context, key: String?): Boolean {
        val settings: SharedPreferences
        val text: Boolean

        //settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        text = settings.getBoolean(key, false)
        return text
    }

    companion object {
        const val PREFS_NAME = "com.example.ktandroidtask"
    }
}