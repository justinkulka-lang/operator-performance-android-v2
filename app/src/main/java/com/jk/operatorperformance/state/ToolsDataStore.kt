package com.jk.operatorperformance.state

import android.content.Context
import com.jk.operatorperformance.tools.PlateInventoryItem
import com.jk.operatorperformance.tools.PlateMath
import com.jk.operatorperformance.tools.UnitSystem

data class ToolsDataState(
    val unitSystem: UnitSystem = UnitSystem.LB,
    val equipmentEffectiveWeight: Double? = null,
    val collarPairWeight: Double? = null,
    val plateInventory: List<PlateInventoryItem> = emptyList(),
    val plateInventoryWarning: String? = null,
)

class ToolsDataStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): ToolsDataState {
        val unit = runCatching {
            UnitSystem.valueOf(prefs.getString(KEY_UNIT, UnitSystem.LB.name).orEmpty())
        }.getOrDefault(UnitSystem.LB)
        return loadProfile(unit)
    }

    fun setUnit(unit: UnitSystem): ToolsDataState {
        prefs.edit().putString(KEY_UNIT, unit.name).apply()
        return loadProfile(unit)
    }

    fun save(state: ToolsDataState) {
        val prefix = state.unitSystem.name.lowercase()
        val editor = prefs.edit().putString(KEY_UNIT, state.unitSystem.name)
        state.equipmentEffectiveWeight?.let { editor.putString("$prefix.$KEY_EQUIPMENT_WEIGHT", it.toString()) }
            ?: editor.remove("$prefix.$KEY_EQUIPMENT_WEIGHT")
        state.collarPairWeight?.let { editor.putString("$prefix.$KEY_COLLAR_PAIR_WEIGHT", it.toString()) }
            ?: editor.remove("$prefix.$KEY_COLLAR_PAIR_WEIGHT")
        editor.putStringSet("$prefix.$KEY_PLATE_INVENTORY", encodeInventory(state.plateInventory)).apply()
    }

    private fun loadProfile(unit: UnitSystem): ToolsDataState {
        val prefix = unit.name.lowercase()
        val rawInventory = prefs.getStringSet("$prefix.$KEY_PLATE_INVENTORY", emptySet()).orEmpty()
        val inventory = decodeInventory(rawInventory)
        return ToolsDataState(
            unitSystem = unit,
            equipmentEffectiveWeight = prefs.getString("$prefix.$KEY_EQUIPMENT_WEIGHT", null)?.toDoubleOrNull()?.takeIf(PlateMath::validEquipmentWeight),
            collarPairWeight = prefs.getString("$prefix.$KEY_COLLAR_PAIR_WEIGHT", null)?.toDoubleOrNull()?.takeIf(PlateMath::validEquipmentWeight),
            plateInventory = inventory,
            plateInventoryWarning = if (inventory.size != rawInventory.size)
                "Some stored plate entries exceed supported limits and are excluded. Check and re-enter the affected plates."
                else null,
        )
    }

    private fun encodeInventory(items: List<PlateInventoryItem>): Set<String> = items
        .sortedByDescending { it.weight }
        .mapTo(linkedSetOf()) { "${it.weight}|${it.pairs}" }

    private fun decodeInventory(values: Set<String>): List<PlateInventoryItem> = values.mapNotNull { raw ->
        val pieces = raw.split('|')
        if (pieces.size != 2) return@mapNotNull null
        val weight = pieces[0].toDoubleOrNull() ?: return@mapNotNull null
        val pairs = pieces[1].toIntOrNull() ?: return@mapNotNull null
        runCatching { PlateInventoryItem(weight, pairs) }.getOrNull()
    }.sortedByDescending { it.weight }

    companion object {
        const val PREFS_NAME = "operator_v54_reference_state"
        private const val KEY_UNIT = "plateUnitSystem"
        private const val KEY_EQUIPMENT_WEIGHT = "plateEquipmentEffectiveWeight"
        private const val KEY_COLLAR_PAIR_WEIGHT = "plateCollarPairWeight"
        private const val KEY_PLATE_INVENTORY = "plateInventoryPairs"
    }
}
