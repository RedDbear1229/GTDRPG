package com.questlog.core.data.repository

import android.content.Context
import com.questlog.core.data.GoogleAuthManager
import com.questlog.core.data.db.QuestLogDatabase
import com.questlog.core.domain.model.DriveAccount
import com.questlog.core.domain.model.SyncResult
import com.questlog.core.domain.repository.DriveBackupRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val DB_NAME = "questlog.db"
private const val BACKUP_FILENAME = "questlog-backup.db"
private const val DRIVE_FILES_BASE = "https://www.googleapis.com/drive/v3/files"
private const val DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"

@Singleton
class DriveBackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: GoogleAuthManager,
    private val database: QuestLogDatabase,
    private val driveHttpClient: OkHttpClient,
) : DriveBackupRepository {

    override fun getSignedInAccount(): DriveAccount? =
        authManager.getSignedInAccount()?.let { authManager.toDriveAccount(it) }

    override suspend fun uploadBackup(): SyncResult = withContext(Dispatchers.IO) {
        val account = authManager.getSignedInAccount()
            ?: return@withContext SyncResult.NotSignedIn()
        val token = authManager.getAccessToken(account)
            ?: return@withContext SyncResult.Error("액세스 토큰을 가져올 수 없습니다")

        runCatching {
            checkpointWal()
            val dbFile = context.getDatabasePath(DB_NAME)

            val metadata = JSONObject().apply {
                put("name", BACKUP_FILENAME)
                put("parents", JSONArray().put("appDataFolder"))
            }.toString()

            val body = MultipartBody.Builder()
                .setType("multipart/related".toMediaType())
                .addPart(metadata.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .addPart(dbFile.asRequestBody("application/octet-stream".toMediaType()))
                .build()

            val request = Request.Builder()
                .url(DRIVE_UPLOAD_URL)
                .addHeader("Authorization", "Bearer $token")
                .post(body)
                .build()

            val response = driveHttpClient.newCall(request).execute()
            // 업로드 성공 확인 후에만 구 파일 삭제 — 먼저 삭제하면 업로드 실패 시 복원 지점 소실
            if (response.isSuccessful) {
                deleteExistingBackupExcept(token, newFileId = response.body?.string()
                    ?.let { runCatching { org.json.JSONObject(it).getString("id") }.getOrNull() })
                SyncResult.Success("백업 완료")
            } else {
                SyncResult.Error("업로드 실패 (HTTP ${response.code})")
            }
        }.getOrElse { e ->
            Timber.e(e, "Drive 업로드 실패")
            SyncResult.Error(e.message ?: "알 수 없는 오류")
        }
    }

    override suspend fun downloadLatestBackup(): SyncResult = withContext(Dispatchers.IO) {
        val account = authManager.getSignedInAccount()
            ?: return@withContext SyncResult.NotSignedIn()
        val token = authManager.getAccessToken(account)
            ?: return@withContext SyncResult.Error("액세스 토큰을 가져올 수 없습니다")

        runCatching {
            val fileId = findBackupFileId(token)
                ?: return@runCatching SyncResult.NoBackupFound

            val request = Request.Builder()
                .url("$DRIVE_FILES_BASE/$fileId?alt=media")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = driveHttpClient.newCall(request).execute()
            if (!response.isSuccessful)
                return@runCatching SyncResult.Error("다운로드 실패 (HTTP ${response.code})")

            val bytes = response.body?.bytes()
                ?: return@runCatching SyncResult.Error("빈 응답")

            // Room 닫기 → 파일 교체 → 앱 재시작 시 새 DB 로드
            database.close()
            val dbFile = context.getDatabasePath(DB_NAME)
            dbFile.parentFile?.mkdirs()
            dbFile.writeBytes(bytes)
            File("${dbFile.path}-wal").delete()
            File("${dbFile.path}-shm").delete()

            SyncResult.Success("복원 완료")
        }.getOrElse { e ->
            Timber.e(e, "Drive 다운로드 실패")
            SyncResult.Error(e.message ?: "알 수 없는 오류")
        }
    }

    override suspend fun signOut() = authManager.signOut()

    private fun checkpointWal() = runCatching {
        database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
    }.onFailure { Timber.w(it, "WAL checkpoint 실패 (무시 가능)") }

    private fun findBackupFileId(token: String): String? {
        val url = DRIVE_FILES_BASE.toHttpUrl().newBuilder()
            .addQueryParameter("spaces", "appDataFolder")
            .addQueryParameter("q", "name='$BACKUP_FILENAME'")
            .addQueryParameter("fields", "files(id)")
            .build()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        return runCatching {
            val response = driveHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val files = JSONObject(response.body?.string() ?: "{}").getJSONArray("files")
            if (files.length() > 0) files.getJSONObject(0).getString("id") else null
        }.getOrNull()
    }

    // 업로드 성공 후 호출 — 새 파일(newFileId)은 보존하고 나머지 동명 파일만 삭제
    private fun deleteExistingBackupExcept(token: String, newFileId: String?) {
        val url = DRIVE_FILES_BASE.toHttpUrl().newBuilder()
            .addQueryParameter("spaces", "appDataFolder")
            .addQueryParameter("q", "name='$BACKUP_FILENAME'")
            .addQueryParameter("fields", "files(id)")
            .build()
        val list = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        val ids = runCatching {
            val resp = driveHttpClient.newCall(list).execute()
            val arr = JSONObject(resp.body?.string() ?: "{}").getJSONArray("files")
            (0 until arr.length()).map { arr.getJSONObject(it).getString("id") }
        }.getOrElse { return }

        ids.filter { it != newFileId }.forEach { id ->
            val del = Request.Builder()
                .url("$DRIVE_FILES_BASE/$id")
                .addHeader("Authorization", "Bearer $token")
                .delete()
                .build()
            runCatching { driveHttpClient.newCall(del).execute() }
                .onFailure { Timber.w(it, "구 백업 삭제 실패 (무시 가능): $id") }
        }
    }
}
