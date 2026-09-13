package com.jk.operatorperformance.tools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PlateMathTest {
    @Test
    fun exactLoadUsesConfiguredFiniteInventory() {
        val result = PlateMath.calculate(
            targetLoad = 135.0,
            equipmentEffectiveWeight = 45.0,
            collarPairWeight = null,
            inventory = listOf(PlateInventoryItem(45.0, 1)),
        )
        assertEquals(135.0, requireNotNull(result.exact).totalLoad, 0.001)
        assertNull(result.lower)
        assertNull(result.higher)
    }

    @Test
    fun impossibleLoadReturnsNearestChoicesWithoutSelectingOne() {
        val result = PlateMath.calculate(
            targetLoad = 132.0,
            equipmentEffectiveWeight = 45.0,
            collarPairWeight = null,
            inventory = listOf(
                PlateInventoryItem(25.0, 1),
                PlateInventoryItem(10.0, 1),
                PlateInventoryItem(5.0, 1),
                PlateInventoryItem(2.5, 2),
            ),
        )
        assertNull(result.exact)
        assertEquals(130.0, requireNotNull(result.lower).totalLoad, 0.001)
        assertEquals(135.0, requireNotNull(result.higher).totalLoad, 0.001)
    }

    @Test
    fun collarsAreIncludedOnlyWhenConfigured() {
        val result = PlateMath.calculate(
            targetLoad = 137.5,
            equipmentEffectiveWeight = 45.0,
            collarPairWeight = 2.5,
            inventory = listOf(PlateInventoryItem(45.0, 1)),
        )
        assertNotNull(result.exact)
        assertEquals(137.5, result.exact?.totalLoad ?: 0.0, 0.001)
    }
}
