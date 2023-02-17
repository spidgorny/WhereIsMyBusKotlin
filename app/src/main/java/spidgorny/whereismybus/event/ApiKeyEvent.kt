package spidgorny.whereismybus.event

import android.content.SharedPreferences
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class ApiKeyEvent(
    public val apiId: String,
    public val apiName: String,
    public val apiSecret: String
) {
    val klass = "ApiKeyEvent"


    fun toJson(): String {
        return Json.encodeToString(ApiKeyEvent.serializer(), this)
    }

    companion object {
        @JvmStatic
        fun fromJson(input: String): ApiKeyEvent {
            return Json.decodeFromString<ApiKeyEvent>(input)
        }

        fun fromSharedPreferences(sharedPreferences: SharedPreferences): ApiKeyEvent? {
            val apiKeyJson = sharedPreferences.getString("apiKey", "");
            Log.d("ApiKeyEvent", "stored apiKeyJson ${apiKeyJson}")
            apiKeyJson?.let {
                if (apiKeyJson.isNotEmpty()) {
                    return fromJson(apiKeyJson)
                }
            }
            return null;
        }
    }

    fun save(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putString("apiKey", this.toJson())
        editor.apply()
    }

    fun reset(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putString("apiKey", "")
        editor.apply()
    }
}
