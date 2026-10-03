package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.state.ProgramUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProgramSearchOfflineTest {
    private val state = ProgramUiState(initialized = true, authority = ProgramAuthority.V72, v72ActivationProven = true)

    @Test fun all224SessionsResolveFromTheLocalSource() {
        val found = mutableSetOf<Pair<Int, Int>>()
        for (week in 1..32) {
            for (day in 1..7) {
                val hits = searchProgram("week $week day $day", state, limit = 8)
                val match = hits.single { it.session.week == week && it.session.day == day }
                assertEquals(ProgramAuthority.V72, match.session.authority)
                assertTrue(match.session.rows.isNotEmpty())
                found += week to day
            }
        }
        assertEquals(224, found.size)
        assertTrue(searchProgram("week 33", state).isEmpty())
        assertTrue(searchProgram("week 32 day 8", state).isEmpty())
    }

    @Test fun movementNamesSubstitutionsAndConditioningAreLocal() {
        val squat = searchProgram("Back or front squat", state, limit = 30)
        assertTrue(squat.isNotEmpty())
        assertTrue(squat.all { hit -> hit.session.rows.any { it.title.contains("squat", ignoreCase = true) || it.alternatives.any { alt -> alt.contains("squat", ignoreCase = true) } } })

        val curl = searchProgram("Behind-body cable curl", state, limit = 30)
        assertTrue(curl.any { it.session.week == 1 && it.session.day == 3 })
        assertTrue(curl.any { it.session.week == 9 && it.session.day == 3 })

        val bike = searchProgram("very easy bike", state, limit = 224)
        assertTrue(bike.any { it.session.week == 32 && it.session.day == 2 })
        assertTrue(bike.any { it.session.week == 32 && it.session.day == 6 })

        val local = SessionResolver.resolve(32, 2, ProgramAuthority.V72)
        assertTrue(local.rows.any { it.prescription.contains("very easy bike", ignoreCase = true) })
    }

    @Test fun searchAndManifestHaveNoNetworkDependency() {
        val search = sourceText("src/main/java/com/jk/operatorperformance/ui/ProgramSearch.kt")
        assertFalse(search.contains("http"))
        assertFalse(search.contains("WebView"))
        assertFalse(search.contains("INTERNET"))
        val manifest = sourceText("src/main/AndroidManifest.xml")
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertFalse(manifest.contains("WebView"))
        val kotlin = File(listOf("src/main/java", "app/src/main/java").first { File(it).isDirectory })
        kotlin.walk().filter { it.extension == "kt" }.forEach { file ->
            val text = file.readText()
            assertFalse(file.path, text.contains("WebView"))
            assertFalse(file.path, text.contains("okhttp", ignoreCase = true))
            assertFalse(file.path, text.contains("retrofit", ignoreCase = true))
            assertFalse(file.path, text.contains("firebase", ignoreCase = true))
        }
    }

    private fun sourceText(relative: String): String =
        listOf(File(relative), File("app/$relative"), File("../$relative")).first { it.isFile }.readText()
}
