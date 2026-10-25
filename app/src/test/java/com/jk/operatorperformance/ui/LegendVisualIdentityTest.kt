package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramV72
import com.jk.operatorperformance.program.V74SurfaceText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale
import kotlin.math.pow

class LegendVisualIdentityTest {
    @Test fun phasePurposeCopyStaysNeutral() {
        val blocks = ProgramV72.blocks
        assertEquals(listOf("Draft", "Build", "Backstop", "Live"), blocks.map { it.name })
        assertEquals(listOf("Draft", "Build", "Backstop", "Live"), blocks.map { it.godName })
        assertEquals(listOf(1 to 8, 9 to 16, 17 to 24, 25 to 32), blocks.map { it.startWeek to it.endWeek })
        assertEquals(
            listOf(
                "Foundation, movement quality, and early work capacity.",
                "Progressive strength, hypertrophy, and conditioning development.",
                "Sustained strength, fatigue control, and repeatable work capacity.",
                "Advanced conditioning, maintained hypertrophy, and practical performance expression.",
            ),
            blocks.map { it.purpose },
        )
        assertEquals("LEGEND", ProgramV72.PROGRAM_NAME)
        assertEquals("Built to hold.", ProgramV72.PROGRAM_TAGLINE)
        blocks.forEach { block ->
            val lower = "${block.name} ${block.purpose}".lowercase()
            assertFalse(lower.contains("final test"))
            assertFalse(lower.contains("goal-pace"))
            assertFalse(lower.contains("taper"))
            assertFalse(lower.contains("event"))
        }
    }

    @Test fun fieldLabelsLeadWithThePlainPhaseName() {
        val labels = ProgramV72.blocks.map(::phaseFieldLabel)
        assertEquals(
            listOf(
                "Phase 1 · Draft",
                "Phase 2 · Build",
                "Phase 3 · Backstop",
                "Phase 4 · Live",
            ),
            labels,
        )
    }

    @Test fun paletteMeetsWcagAa() {
        textPairs().forEach { (foreground, background) ->
            assertTrue(
                "text ${foreground.toHex()} on ${background.toHex()} is ${contrast(foreground, background)}",
                contrast(foreground, background) >= 4.5,
            )
        }
        val controlPairs = listOf(
            LegendPalette.Outline to LegendPalette.Surface,
            LegendPalette.Outline to LegendPalette.Background,
            LegendPalette.OutlineVariant to LegendPalette.Surface,
            LegendPalette.Brass to LegendPalette.SurfaceVariant,
            LegendPalette.Olive to LegendPalette.Background,
            LegendPalette.Olive to LegendPalette.Surface,
            LegendPalette.Brass to LegendPalette.Oxblood,
        )
        controlPairs.forEach { (foreground, background) ->
            assertTrue(
                "control ${foreground.toHex()} on ${background.toHex()} is ${contrast(foreground, background)}",
                contrast(foreground, background) >= 3.0,
            )
        }
    }

    @Test fun boneOnBackgroundIs14_2To1() = assertTextRatio(LegendPalette.OnSurface, LegendPalette.Background, "14.2:1")

    @Test fun boneOnSurfaceIs13_0To1() = assertTextRatio(LegendPalette.OnSurface, LegendPalette.Surface, "13.0:1")

    @Test fun boneOnOxbloodBandIs8_2To1() = assertTextRatio(LegendPalette.OnSurface, LegendPalette.Oxblood, "8.2:1")

    @Test fun secondaryOnSurfaceIs7_3To1() = assertTextRatio(LegendPalette.OnSurfaceVariant, LegendPalette.Surface, "7.3:1")

    @Test fun secondaryOnBackgroundIs8_0To1() = assertTextRatio(LegendPalette.OnSurfaceVariant, LegendPalette.Background, "8.0:1")

    @Test fun brassTextOnBackgroundIs6_1To1() = assertTextRatio(LegendPalette.Brass, LegendPalette.Background, "6.1:1")

    @Test fun brassTextOnSurfaceIs5_6To1() = assertTextRatio(LegendPalette.Brass, LegendPalette.Surface, "5.6:1")

    @Test fun brassTextOnSelectedTabIs4_7To1() = assertTextRatio(LegendPalette.Brass, LegendPalette.SurfaceVariant, "4.7:1")

    @Test fun brassTextOnSurfaceContainerHighestIs4_6To1() = assertTextRatio(LegendPalette.Brass, LegendPalette.SurfaceContainerHighest, "4.6:1")

    @Test fun darkButtonTextOnBrassIs6_1To1() = assertTextRatio(LegendPalette.OnBrass, LegendPalette.Brass, "6.1:1")

    @Test fun oliveOnBackgroundIs3_8To1NonText() = assertNonTextRatio(LegendPalette.Olive, LegendPalette.Background, "3.8:1")

    @Test fun oliveOnSurfaceIs3_4To1NonText() = assertNonTextRatio(LegendPalette.Olive, LegendPalette.Surface, "3.4:1")

    @Test fun brassRuleOnOxbloodIs3_5To1NonText() {
        val ratio = contrast(LegendPalette.Brass, LegendPalette.Oxblood)
        assertTrue(
            "non-text brass on oxblood is ${"%.3f".format(Locale.US, ratio)}:1; the band rules need 3.0:1",
            ratio >= 3.0 && "%.1f:1".format(Locale.US, ratio) == "3.5:1",
        )
    }

    @Test fun bundledSerifIsEbGaramondUnderOfl() {
        val font = repoFile("src/main/res/font/eb_garamond.ttf")
        val italic = repoFile("src/main/res/font/eb_garamond_italic.ttf")
        val license = repoFile("src/main/assets/licenses/EBGaramond-OFL.txt")
        assertTrue(font.length() > 100_000)
        assertTrue(italic.length() > 100_000)
        val magic = font.inputStream().use { it.readNBytes(4) }
        assertTrue(magic.contentEquals(byteArrayOf(0, 1, 0, 0)) || magic.decodeToString() == "OTTO")
        val ofl = license.readText()
        assertTrue(ofl.contains("SIL OPEN FONT LICENSE Version 1.1"))
        assertTrue(ofl.contains("EB Garamond"))
        assertEquals("EB Garamond, SIL Open Font License 1.1.", V74SurfaceText.FONT_LICENSE_LINE)
        assertFalse(V74SurfaceText.FONT_LICENSE_LINE.contains("!"))
        assertTrue(V74SurfaceText.chrome().contains(V74SurfaceText.FONT_LICENSE_LINE))
        val tools = repoFile("src/main/java/com/jk/operatorperformance/ui/OperatorPerformanceApp.kt").readText()
        assertTrue(tools.contains("V74SurfaceText.FONT_LICENSE_LINE"))
    }

    @Test fun launcherIsEngravedMonogramInABrassRing() {
        val foreground = repoFile("src/main/res/drawable/ic_legend_launcher_foreground.xml").readText()
        val monochrome = repoFile("src/main/res/drawable/ic_legend_monogram.xml").readText()
        val background = repoFile("src/main/res/values/colors.xml").readText()
        assertTrue(foreground.contains("#B08D57"))
        assertTrue(foreground.contains("#E8DFCE"))
        assertTrue(foreground.contains("a28,28"))
        assertTrue(monochrome.contains("a28,28"))
        assertTrue(background.contains("#15110D"))
        val themed = repoFile("src/main/res/mipmap-anydpi-v33/ic_launcher.xml").readText()
        assertTrue(themed.contains("@drawable/ic_legend_monogram"))
    }
}

private fun textPairs() = listOf(
    LegendPalette.OnSurface to LegendPalette.Background,
    LegendPalette.OnSurface to LegendPalette.Surface,
    LegendPalette.OnSurface to LegendPalette.SurfaceVariant,
    LegendPalette.OnSurface to LegendPalette.SurfaceContainer,
    LegendPalette.OnSurface to LegendPalette.SurfaceContainerLow,
    LegendPalette.OnSurface to LegendPalette.SurfaceContainerLowest,
    LegendPalette.OnSurface to LegendPalette.SurfaceContainerHigh,
    LegendPalette.OnSurface to LegendPalette.SurfaceContainerHighest,
    LegendPalette.OnSurface to LegendPalette.Oxblood,
    LegendPalette.OnSurfaceVariant to LegendPalette.Background,
    LegendPalette.OnSurfaceVariant to LegendPalette.Surface,
    LegendPalette.OnSurfaceVariant to LegendPalette.SurfaceVariant,
    LegendPalette.OnSurfaceVariant to LegendPalette.SurfaceContainerHigh,
    LegendPalette.OnSurfaceVariant to LegendPalette.SurfaceContainerHighest,
    LegendPalette.Brass to LegendPalette.Background,
    LegendPalette.Brass to LegendPalette.Surface,
    LegendPalette.Brass to LegendPalette.SurfaceVariant,
    LegendPalette.Brass to LegendPalette.SurfaceContainerHigh,
    LegendPalette.Brass to LegendPalette.SurfaceContainerHighest,
    LegendPalette.OnBrass to LegendPalette.Brass,
    LegendPalette.OnBrassContainer to LegendPalette.BrassContainer,
    LegendPalette.Warning to LegendPalette.Background,
    LegendPalette.Warning to LegendPalette.Surface,
    LegendPalette.Warning to LegendPalette.SurfaceContainerHighest,
    LegendPalette.OnWarning to LegendPalette.Warning,
    LegendPalette.OnErrorContainer to LegendPalette.ErrorContainer,
    LegendPalette.DisabledContent to LegendPalette.DisabledContainer,
    LegendPalette.InverseOnSurface to LegendPalette.InverseSurface,
)

private fun assertTextRatio(foreground: Long, background: Long, reported: String) {
    val ratio = contrast(foreground, background)
    assertTrue(
        "text ${foreground.toHex()} on ${background.toHex()} is ${"%.3f".format(Locale.US, ratio)}:1, reported $reported; AA normal text requires 4.5:1",
        ratio >= 4.5 && "%.1f:1".format(Locale.US, ratio) == reported,
    )
}

private fun assertNonTextRatio(foreground: Long, background: Long, reported: String) {
    val ratio = contrast(foreground, background)
    assertTrue(
        "non-text ${foreground.toHex()} on ${background.toHex()} is ${"%.3f".format(Locale.US, ratio)}:1, reported $reported; olive is not a text color",
        ratio >= 3.0 && ratio < 4.5 && "%.1f:1".format(Locale.US, ratio) == reported,
    )
}

private fun repoFile(relative: String): File =
    listOf(File(relative), File("app/$relative"), File("../$relative")).first { it.isFile }

private fun Long.toHex(): String = "0x${toString(16)}"

private fun contrast(foreground: Long, background: Long): Double {
    val lighter = maxOf(luminance(foreground), luminance(background))
    val darker = minOf(luminance(foreground), luminance(background))
    return (lighter + 0.05) / (darker + 0.05)
}

private fun luminance(color: Long): Double {
    fun channel(shift: Int): Double {
        val value = ((color shr shift) and 0xFF).toInt() / 255.0
        return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
}
