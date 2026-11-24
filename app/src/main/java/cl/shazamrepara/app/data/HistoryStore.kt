package cl.shazamrepara.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class HistoryStore(context: Context) {

    private val file = File(context.filesDir, "diagnosticos.json")

    fun load(): List<HistoryEntry> {
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                HistoryEntry(
                    createdAt = item.optLong("createdAt"),
                    equipment = item.optString("equipment"),
                    title = item.optString("title"),
                    safety = item.optString("safety"),
                    probability = item.optInt("probability"),
                    totalMinClp = item.optInt("totalMinClp"),
                    totalMaxClp = item.optInt("totalMaxClp")
                )
            }
        } catch (t: Throwable) {
            emptyList()
        }
    }

    fun add(result: DiagnosisResult): List<HistoryEntry> {
        val entry = HistoryEntry(
            createdAt = result.createdAt,
            equipment = result.equipmentLabel,
            title = result.top.fault.name,
            safety = result.safety.label,
            probability = result.top.probability,
            totalMinClp = result.totalMinClp,
            totalMaxClp = result.totalMaxClp
        )
        val current = load().toMutableList()
        current.add(0, entry)
        while (current.size > 50) current.removeAt(current.lastIndex)

        try {
            val array = JSONArray()
            current.forEach { item ->
                array.put(
                    JSONObject().apply {
                        put("createdAt", item.createdAt)
                        put("equipment", item.equipment)
                        put("title", item.title)
                        put("safety", item.safety)
                        put("probability", item.probability)
                        put("totalMinClp", item.totalMinClp)
                        put("totalMaxClp", item.totalMaxClp)
                    }
                )
            }
            file.writeText(array.toString())
        } catch (t: Throwable) {
            // Si no se puede guardar, la app sigue funcionando sin historial.
        }
        return current
    }
}
