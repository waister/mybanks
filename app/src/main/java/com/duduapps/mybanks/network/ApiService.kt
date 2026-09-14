package com.duduapps.mybanks.network

import com.duduapps.mybanks.models.AccountsResponse
import com.duduapps.mybanks.models.BanksResponse
import com.duduapps.mybanks.models.BaseApiResponse
import com.duduapps.mybanks.models.EmailCodeResponse
import com.duduapps.mybanks.models.IdentifyResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("banks")
    suspend fun getBanks(): Response<BanksResponse>

    @GET("accounts")
    suspend fun getAccounts(): Response<AccountsResponse>

    @FormUrlEncoded
    @POST("account/register")
    suspend fun registerAccount(
        @Field("id") id: Long,
        @Field("pix_code") pixCode: String,
        @Field("bank_id") bankId: Int,
        @Field("label") label: String,
        @Field("type") type: String,
        @Field("agency") agency: String,
        @Field("account") account: String,
        @Field("operation") operation: String,
        @Field("holder") holder: String,
        @Field("document") document: String,
        @Field("legal_account") legalAccount: Boolean,
        @Field("deleted_at") deletedAt: String?,
    ): Response<AccountsResponse>

    @GET("identify")
    suspend fun identify(
        @Query("token") token: String,
    ): Response<IdentifyResponse>

    @FormUrlEncoded
    @POST("email-send-code")
    suspend fun sendEmailCode(
        @Field("email") email: String,
    ): Response<EmailCodeResponse>

    @FormUrlEncoded
    @POST("message/send")
    suspend fun sendFeedback(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("comments") comments: String,
    ): Response<BaseApiResponse>
}
