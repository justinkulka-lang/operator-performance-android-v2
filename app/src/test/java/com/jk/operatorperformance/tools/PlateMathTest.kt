package com.jk.operatorperformance.tools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class PlateMathTest {
    @Test fun oversizedAndNonFiniteInputsAreRejectedBeforeSearch() {
        listOf(0, 21, Int.MAX_VALUE).forEach { pairs ->
            assertThrows(IllegalArgumentException::class.java) { PlateInventoryItem(45.0, pairs) }
        }
        listOf(Double.NaN, Double.POSITIVE_INFINITY, 0.0, 0.0001, 201.0).forEach { weight ->
            assertThrows(IllegalArgumentException::class.java) { PlateInventoryItem(weight, 1) }
        }
        assertThrows(IllegalArgumentException::class.java) { PlateMath.calculate(10_001.0, 45.0, null, emptyList()) }
        assertThrows(IllegalArgumentException::class.java) { PlateMath.calculate(100.0, 1_001.0, null, emptyList()) }
        assertThrows(IllegalArgumentException::class.java) {
            PlateMath.calculate(100.0, 45.0, null, List(13) { PlateInventoryItem((it+1).toDouble(), 1) })
        }
        assertThrows(IllegalArgumentException::class.java) {
            PlateMath.calculate(100.0, 45.0, null, listOf(PlateInventoryItem(5.0, 20), PlateInventoryItem(5.0, 1)))
        }
    }

    @Test fun fineGrainedInventoriesCannotExceedTheSearchBudget() {
        assertThrows(IllegalArgumentException::class.java) {
            PlateMath.calculate(2_000.0, 45.0, null, listOf(PlateInventoryItem(100.0, 20), PlateInventoryItem(99.999, 20)))
        }
    }

    @Test(timeout = 5_000) fun largestAcceptedSearchIsBoundedAndCancellable() {
        var checks = 0
        val result = PlateMath.calculate(300.001, 0.0, null,
            listOf(PlateInventoryItem(100.0, 20), PlateInventoryItem(0.001, 20))) { checks++ }
        assertEquals(200.04, requireNotNull(result.lower).totalLoad, 0.001)
        assertEquals(400.0, requireNotNull(result.higher).totalLoad, 0.001)
        assertTrue(checks in 2..1000)
        var cancelledAt = 0
        assertThrows(InterruptedException::class.java) {
            PlateMath.calculate(300.001, 0.0, null,
                listOf(PlateInventoryItem(100.0, 20), PlateInventoryItem(0.001, 20))) {
                if (++cancelledAt == 5) throw InterruptedException("cancelled")
            }
        }
        assertEquals(5, cancelledAt)
    }

    @Test fun boundedSearchMatchesExhaustiveSmallInventoryAndNeverReusesPairs() {
        val inventories = listOf(
            listOf(PlateInventoryItem(7.0, 2), PlateInventoryItem(3.0, 3), PlateInventoryItem(0.5, 2)),
            listOf(PlateInventoryItem(2.5, 3), PlateInventoryItem(1.25, 2)),
        )
        for (inventory in inventories) {
            var totals = setOf(20.0)
            for (item in inventory) totals = totals.flatMap { sum -> (0..item.pairs).map { sum + 2*item.weight*it } }.toSet()
            for (target in (1..240).map { it*0.5 }) {
                val actual = PlateMath.calculate(target, 20.0, null, inventory)
                val expectedExact = totals.find { it == target }
                assertEquals(expectedExact, actual.exact?.totalLoad)
                assertEquals(if (expectedExact != null) null else totals.filter { it < target }.maxOrNull(), actual.lower?.totalLoad)
                assertEquals(if (expectedExact != null) null else totals.filter { it > target }.minOrNull(), actual.higher?.totalLoad)
                listOfNotNull(actual.exact, actual.lower, actual.higher).forEach { load ->
                    assertEquals(load.totalLoad, 20.0 + load.perSide.sumOf { it.first*it.second*2 }, 0.001)
                    load.perSide.forEach { (weight, count) -> assertTrue(count <= inventory.single { it.weight == weight }.pairs) }
                }
            }
        }
    }
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
