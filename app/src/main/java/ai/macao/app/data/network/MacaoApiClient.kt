package ai.macao.app.data.network

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MacaoApiClient {

    // Toggle between Render backend and Android emulator localhost (10.0.2.2)
    private const val BASE_URL = "https://macaoai-backend.onrender.com/api/v1/"
    // private const val BASE_URL = "http://10.0.2.2:10000/api/v1/"

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val requestBuilder = originalRequest.newBuilder()
        try {
            // Retrieve Firebase ID Token synchronously on the OkHttp background thread.
            // forceRefresh = false preserves cache. Firebase SDK auto-refreshes if expired.
            val tokenResult = Tasks.await(currentUser.getIdToken(false))
            val token = tokenResult.token
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: MacaoApiService = retrofit.create(MacaoApiService::class.java)
}
