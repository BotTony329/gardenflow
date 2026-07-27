package com.tony.gardenflow.security

interface AppIntegrityService {
    suspend fun checkIntegrity(): AppIntegrityVerdict
}

data class AppIntegrityVerdict(
    val isDebuggable: Boolean,
    val installerTrusted: Boolean,
    val signatureTrusted: Boolean,
    val rootedDeviceLikely: Boolean,
    val playIntegrityTokenRequested: Boolean,
    val playIntegrityTokenAvailable: Boolean
) {
    val hasLocalRisk: Boolean
        get() = isDebuggable || !installerTrusted || !signatureTrusted || rootedDeviceLikely
}
