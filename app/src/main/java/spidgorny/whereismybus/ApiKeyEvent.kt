package spidgorny.whereismybus

import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@Serializable
data class ApiKeyEvent(
    public val apiId: String,
    public val apiName: String,
    public val apiSecret: String
) {
    fun toJson(): String {
        return Json.encodeToString(ApiKeyEvent.serializer(), this)
    }

    companion object {
        @JvmStatic
        fun fromJson(input: String): ApiKeyEvent {
            return Json.decodeFromString<ApiKeyEvent>(input)
        }
    }
}
