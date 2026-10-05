package com.jk.operatorperformance.tools

import kotlin.math.roundToLong

enum class UnitSystem { LB, KG }

data class PlateInventoryItem(
    val weight: Double,
    val pairs: Int,
) {
    init {
        require(PlateMath.validPlateWeight(weight)) { "Plate weight must be 0.001–200." }
        require(pairs in 1..PlateMath.MAX_PAIRS_PER_WEIGHT) { "Plate pair count must be 1–20." }
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
    const val MAX_PAIRS_PER_WEIGHT = 20
    const val MAX_PLATE_TYPES = 12
    const val MAX_TARGET_LOAD = 10_000.0
    const val MAX_EQUIPMENT_WEIGHT = 1_000.0
    const val MAX_SEARCH_UNITS = 250_000

    fun validPlateWeight(value: Double) = value.isFinite() && value in 0.001..200.0
    fun validEquipmentWeight(value: Double) = value.isFinite() && value in 0.0..MAX_EQUIPMENT_WEIGHT
    fun validTargetLoad(value: Double) = value.isFinite() && value > 0.0 && value <= MAX_TARGET_LOAD

    fun calculate(
        targetLoad: Double,
        equipmentEffectiveWeight: Double,
        collarPairWeight: Double?,
        inventory: List<PlateInventoryItem>,
        checkCancelled: () -> Unit = {},
    ): PlateMathResult {
        require(validTargetLoad(targetLoad)) { "Target load must be positive and at most 10000." }
        require(validEquipmentWeight(equipmentEffectiveWeight)) { "Equipment weight must be 0–1000." }
        require(collarPairWeight == null || validEquipmentWeight(collarPairWeight)) { "Collar pair weight must be 0–1000." }
        require(inventory.size <= MAX_PLATE_TYPES) { "Use at most 12 plate types." }
        checkCancelled()

        val target = scaled(targetLoad)
        val base = scaled(equipmentEffectiveWeight + (collarPairWeight ?: 0.0))
        val normalized = inventory
            .groupBy { scaled(it.weight) }
            .map { (weight, rows) ->
                val pairs = rows.sumOf { it.pairs.toLong() }
                require(pairs <= MAX_PAIRS_PER_WEIGHT) { "Use at most 20 pairs of each plate weight." }
                weight to pairs.toInt()
            }
            .sortedByDescending { it.first }

        fun load(selection: List<Pair<Long, Int>>) = PlateLoad(
            unscaled(base + 2L * selection.sumOf { (weight, count) -> weight * count }),
            selection.filter { it.second > 0 }.map { (weight, count) -> unscaled(weight) to count },
        )
        val empty = load(emptyList())
        if (target <= base) return if (target == base) PlateMathResult(empty, null, null)
            else PlateMathResult(null, null, empty)
        val maximum = base + 2L * normalized.sumOf { (weight, count) -> weight * count }
        if (target >= maximum) {
            val full = load(normalized)
            return if (target == maximum) PlateMathResult(full, null, null)
                else PlateMathResult(null, full, null)
        }

        val divisor = normalized.map { it.first }.reduce(::gcd)
        val required = (target - base + 2L * divisor - 1) / (2L * divisor)
        val totalUnits = (maximum - base) / (2L * divisor)
        // A minimal above-target subset overshoots by less than its largest pair.
        val upper = minOf(totalUnits, required + normalized.first().first / divisor - 1)
        require(upper <= MAX_SEARCH_UNITS) {
            "These fine plate increments need too many combinations. Use fewer plate types or coarser weights."
        }
        val limit = upper.toInt()
        val reachable = BooleanArray(limit + 1)
        reachable[0] = true
        val choices = mutableListOf<IntArray>()
        normalized.forEach { (scaledWeight, count) ->
            checkCancelled()
            val weight = (scaledWeight / divisor).toInt()
            val remaining = IntArray(limit + 1) { -1 }
            val used = IntArray(limit + 1)
            for (sum in 0..limit) {
                if (sum % 4096 == 0) checkCancelled()
                if (reachable[sum]) {
                    remaining[sum] = count
                } else if (sum >= weight && remaining[sum - weight] > 0) {
                    reachable[sum] = true
                    remaining[sum] = remaining[sum - weight] - 1
                    used[sum] = used[sum - weight] + 1
                }
            }
            choices += used
        }

        fun loadAt(value: Int): PlateLoad {
            var sum = value
            val selection = normalized.map { it.first to 0 }.toMutableList()
            for (index in normalized.indices.reversed()) {
                val count = choices[index][sum]
                selection[index] = normalized[index].first to count
                sum -= (normalized[index].first / divisor).toInt() * count
            }
            check(sum == 0)
            return load(selection)
        }
        var lower: Int? = null
        for (sum in 0..limit) {
            if (sum % 4096 == 0) checkCancelled()
            if (!reachable[sum]) continue
            val total = base + 2L * divisor * sum
            if (total == target) return PlateMathResult(loadAt(sum), null, null)
            if (total < target) lower = sum
            else return PlateMathResult(null, lower?.let(::loadAt), loadAt(sum))
        }
        return PlateMathResult(null, lower?.let(::loadAt), null)
    }

    private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
    private fun scaled(value: Double): Long = (value * SCALE).roundToLong()
    private fun unscaled(value: Long): Double = value.toDouble() / SCALE.toDouble()
}
