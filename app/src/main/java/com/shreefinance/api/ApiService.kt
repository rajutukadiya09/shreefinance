package com.shreefinance.api

import android.app.Activity
import android.app.AlertDialog
import android.widget.ProgressBar
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("api/login")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>
    @POST("api/logoutUser")
    fun logoutUser(
        @Header("Authorization") token: String
    ): Call<LogoutResponse>
}

data class LoginResponse(
    val status: Boolean,
    val error: String?,
    val message: String,
    val data: TokenData?,
    val statusCode: Int
)
data class LogoutResponse(
    val status: Boolean,
    val error: String?,
    val message: String,
    val data: List<Any>,
    val statusCode: Int
)
data class TokenData(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)

 var loadingDialog: AlertDialog? = null

fun showLoading(activity: Activity) {
    if (activity.isFinishing || activity.isDestroyed) return

    if (loadingDialog == null) {
        val progress = ProgressBar(activity).apply {
            isIndeterminate = true
        }

        val builder = AlertDialog.Builder(activity)
            .setView(progress)
            .setCancelable(false)

        loadingDialog = builder.create()
    }

    if (!loadingDialog!!.isShowing) {
        loadingDialog?.show()
    }
}

fun hideLoading() {
    loadingDialog?.let {
        if (it.isShowing) {
            it.dismiss()
        }
    }
    loadingDialog = null
}