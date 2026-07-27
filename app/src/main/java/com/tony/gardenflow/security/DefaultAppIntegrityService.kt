package com.tony.gardenflow.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.tony.gardenflow.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DefaultAppIntegrityService @Inject constructor(
    @ApplicationContext private val context: Context
) : AppIntegrityService {
    override suspend fun checkIntegrity(): AppIntegrityVerdict = withContext(Dispatchers.IO) {
        val installerTrusted = BuildConfig.DEBUG || installerPackage() in trustedInstallers
        val signatureTrusted = BuildConfig.DEBUG || signatureMatches()
        val rootLikely = rootIndicatorsPresent()
        val playTokenAvailable = requestPlayIntegrityTokenIfEnabled()
        val verdict = AppIntegrityVerdict(
            isDebuggable = isDebuggable(),
            installerTrusted = installerTrusted,
            signatureTrusted = signatureTrusted,
            rootedDeviceLikely = rootLikely,
            playIntegrityTokenRequested = BuildConfig.ENABLE_PLAY_INTEGRITY,
            playIntegrityTokenAvailable = playTokenAvailable
        )
        if (verdict.hasLocalRisk) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Local integrity risk detected: $verdict")
            } else {
                Log.w(TAG, "Local integrity risk detected.")
            }
        } else if (BuildConfig.DEBUG) {
            Log.i(TAG, "Local integrity check passed.")
        }
        verdict
    }

    private fun isDebuggable(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    private fun installerPackage(): String? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstallerPackageName(context.packageName)
        }
    }.getOrNull()

    private fun signatureMatches(): Boolean {
        val expected = BuildConfig.RELEASE_CERT_SHA256.trim()
        if (expected.isBlank()) return true
        val actual = signingCertSha256() ?: return false
        return actual.equals(expected, ignoreCase = true)
    }

    private fun signingCertSha256(): String? = runCatching {
        val flags = PackageManager.GET_SIGNING_CERTIFICATES
        val info = context.packageManager.getPackageInfo(context.packageName, flags)
        val signatures = info.signingInfo?.apkContentsSigners.orEmpty()
        val digest = MessageDigest.getInstance("SHA-256")
        signatures.firstOrNull()?.toByteArray()?.let { cert ->
            digest.digest(cert).joinToString(":") { "%02X".format(it) }
        }
    }.getOrNull()

    private fun rootIndicatorsPresent(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return Build.TAGS?.contains("test-keys") == true || paths.any { File(it).exists() }
    }

    private fun requestPlayIntegrityTokenIfEnabled(): Boolean {
        if (!BuildConfig.ENABLE_PLAY_INTEGRITY) return false
        return runCatching {
            val nonce = ByteArray(32).also { SecureRandom().nextBytes(it) }
            val request = IntegrityTokenRequest.builder()
                .setNonce(Base64.encodeToString(nonce, Base64.NO_WRAP))
                .build()
            Tasks.await(IntegrityManagerFactory.create(context).requestIntegrityToken(request))
            if (BuildConfig.DEBUG) Log.i(TAG, "Play Integrity token acquired.")
            true
        }.onFailure {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Play Integrity token request failed.", it)
            } else {
                Log.w(TAG, "Play Integrity token request failed.")
            }
        }.getOrDefault(false)
    }

    private companion object {
        const val TAG = "GardenIntegrity"
        val trustedInstallers = setOf(
            "com.android.vending",
            "com.google.android.feedback",
            "com.sec.android.app.samsungapps"
        )
    }
}
