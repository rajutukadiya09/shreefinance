package com.shreefinance.ui


import android.content.Context
import android.content.SharedPreferences

object PrefsHelper {

    private const val PREF_NAME = "MyAppPrefs"
    private const val ACCESS_TOKEN = "access_token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAccessToken(context: Context, token: String) {
        getPrefs(context).edit().putString(ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(ACCESS_TOKEN, null)
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
