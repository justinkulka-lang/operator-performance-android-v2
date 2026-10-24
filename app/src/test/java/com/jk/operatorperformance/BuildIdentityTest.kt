package com.jk.operatorperformance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BuildIdentityTest {
    @Test fun buildConfigAndManifestPackageMatchTheCandidate() {
        assertEquals("0.40.0", BuildConfig.VERSION_NAME)
        assertEquals(49, BuildConfig.VERSION_CODE)
        val manifest = listOf(
            File("build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml"),
            File("app/build/intermediates/merged_manifests/debug/processDebugManifest/AndroidManifest.xml"),
        ).first { it.isFile }.readText()
        assertTrue(manifest.contains("package=\"com.jk.operatorperformance\""))
    }
}
