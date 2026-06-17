package com.questlog.core.data

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.questlog.core.domain.model.DriveAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"

@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DRIVE_APPDATA_SCOPE))
        .build()

    val client = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent = client.signInIntent

    fun getSignedInAccount(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)

    fun toDriveAccount(account: GoogleSignInAccount) = DriveAccount(
        email = account.email ?: "",
        displayName = account.displayName ?: account.email ?: "",
    )

    // 액세스 토큰 반환 — Play Services 내부적으로 만료 시 자동 갱신
    suspend fun getAccessToken(account: GoogleSignInAccount): String? = withContext(Dispatchers.IO) {
        runCatching {
            GoogleAuthUtil.getToken(context, account.account!!, "oauth2:$DRIVE_APPDATA_SCOPE")
        }.onFailure { Timber.e(it, "Drive 액세스 토큰 획득 실패") }.getOrNull()
    }

    suspend fun handleSignInResult(data: Intent?): GoogleSignInAccount? = runCatching {
        GoogleSignIn.getSignedInAccountFromIntent(data).await()
    }.onFailure { Timber.e(it, "Google 로그인 처리 실패") }.getOrNull()

    suspend fun signOut() {
        runCatching { client.signOut().await() }
            .onFailure { Timber.e(it, "Google 로그아웃 실패") }
    }
}
