package com.shreefinance.api

import android.app.Activity
import android.app.AlertDialog
import android.widget.ProgressBar
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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

    @GET("api/get-loan-id")
    fun getLoanId(
        @Header("Authorization") token: String,
        @Query("loan_type") loanType: Int
    ): Call<LoanIdResponse>

    @GET("api/get-references")
    fun getReferences(
        @Header("Authorization") token: String
    ): Call<ReferenceResponse>

    @GET("api/get-productType")
    fun getProductTypes(
        @Header("Authorization") token: String
    ): Call<ProductTypeResponse>
    @GET("api/get-brands")
    fun getBrands(
        @Header("Authorization") token: String,
        @Query("product_type") product_type: Int
    ): Call<BrandResponse>
    @Multipart
    @POST("api/apply-loan")
    fun applyLoan(
        @Header("Authorization") token: String,  // ✅ pass token here
        @Part("loan_type") loantype: RequestBody,
        @Part("loan_id") loanId: RequestBody,
        @Part("agent_id") agentId: RequestBody,
        @Part("ref_id") refId: RequestBody,
        @Part("first_name") firstName: RequestBody,
        @Part("product_type") productType: RequestBody,
        @Part("brand_id") brandId: RequestBody,
        @Part("model_name") modelName: RequestBody,
        @Part("imei_number") imeiNumber: RequestBody,
        @Part("amount") amount: RequestBody,
        @Part("mobile_number") mobileNumber: RequestBody,
        @Part("no_of_emi") noOfEmi: RequestBody,
        @Part("emi_amount") emiAmount: RequestBody,
        @Part("reg_date") regDate: RequestBody,
        @Part("emi_start_date") emiStartDate: RequestBody,
        @Part("emi_end_date") emienddate: RequestBody,
        @Part("given_down_payment") givenDownPayment: RequestBody,
        @Part("total_down_payment") totalDownPayment: RequestBody,
        @Part("aadhar_card_number") aadharcardnumber: RequestBody,
        @Part("pan_card_number") pancardnumber: RequestBody,
        @Part("down_payment_due_date") downpaymentduedate: RequestBody,
        @Part("pending_down_payment") pendingdownpayment: RequestBody,
        @Part("processing_fees") processingfees: RequestBody,
        @Part("city") city: RequestBody,
        @Part("alternate_number") alternatenumber: RequestBody,
        @Part aadharCardFront: MultipartBody.Part?,
        @Part aadharCardBack: MultipartBody.Part?,
        @Part panCardImage: MultipartBody.Part?,
        @Part userPhoto: MultipartBody.Part?
    ): Call<ResponseBody>
}
data class BrandResponse(
    val status: Boolean,
    val error: String?,
    val message: String,
    val data: List<BrandItem>,
    val statusCode: Int
)

data class BrandItem(
    val id: Int,
    val brand_name: String
)
data class ProductTypeResponse(
    val status: Boolean,
    val error: String?,
    val message: String,
    val data: List<ProductTypeItem>,
    val statusCode: Int
)

data class ProductTypeItem(
    val id: Int,
    val type: String
)
data class LoanIdResponse(
    val status: Boolean,
    val error: String?,
    val message: String,
    val data: String,
    val statusCode: Int
)


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
data class ReferenceResponse(
    val status: Boolean,
    val message: String?,
    val data: List<ReferenceItem>?,
    val statusCode: Int
)

data class ReferenceItem(
    val id: Int,
    val name: String,
    val mobile_number: String
)

data class ProductType(
    val id: Int,
    val type: String
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