package com.jk.operatorperformance.tools

import kotlin.math.roundToLong

enum class UnitSystem { LB, KG }

data class PlateInventoryItem(
    val weight: Double,
    val pairs: Int,
) {
    init {
        require(weight.isFinite() && weight > 0.0) { "Plate weight must be positive and finite." }
        require(pairs > 0) { "Plate pair count must be positive." }
    }
}

data class PlateLoad(
    val totalLoad: Double,
    val perSide: List<Pair<Double, Int>>,
)

data class PlateMathResult(
    val exact: PlateLoad?,
    val lower: PlateLoad?,
    val higher: PlateLoad?,
)

object PlateMath {
    private const val SCALE = 1000L

    fun calculate(
        targetLoad: Double,
        equipmentEffectiveWeight: Double,
        collarPairWeight: Double?,
        inventory: List<PlateInventoryItem>,
    ): PlateMathResult {
        require(targetLoad.isFinite() && targetLoad > 0.0)
        require(equipmentEffectiveWeight.isFinite() && equipmentEffectiveWeight >= 0.0)
        require(collarPairWeight == null || (collarPairWeight.isFinite() && collarPairWeight >= 0.0))

        val target = scaled(targetLoad)
        val base = scaled(equipmentEffectiveWeight + (collarPairWeight ?: 0.0))
        val normalized = inventory
            .groupBy { scaled(it.weight) }
            .map { (weight, rows) -> weight to rows.sumOf { it.pairs } }
            .filter { it.first > 0L && it.second > 0 }
            .sortedByDescending { it.first }

        val reachable = linkedMapOf<Long, Map<Long, Int>>()
        reachable[0L] = emptyMap()
        normalized.forEach { (weight, count) ->
            val snapshot = reachable.toMap()
            for ((sum, selection) in snapshot) {
                for (used in 1..count) {
                    val next = sum + weight * used
                    val candidate = selection + (weight to used)
                    reachable.putIfAbsent(next, candidate)
                }
            }
        }

        val loads = reachable.entries.map { (perSideScaled, selection) ->
            val total = base + 2L * perSideScaled
            total to PlateLoad(
                totalLoad = unscaled(total),
                perSide = selection.entries
                    .sortedByDescending { it.key }
                    .map { unscaled(it.key) to it.value },
            )
        }.sortedBy { it.first }

        val exact = loads.firstOrNull { it.first == target }?.second
        val lower = if (exact == null) loads.lastOrNull { it.first < target }?.second else null
        val higher = if (exact == null) loads.firstOrNull { it.first > target }?.second else null
        return PlateMathResult(exact = exact, lower = lower, higher = higher)
    }

    private fun scaled(value: Double): Long = (value * SCALE).roundToLong()
    private fun unscaled(value: Long): Double = value.toDouble() / SCALE.toDouble()
}
