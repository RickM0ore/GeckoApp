package re.rickmoo.gecko.component

import org.json.JSONObject
import org.mozilla.gecko.util.GeckoBundle

class GeckoBridge {
    fun communicationTest(data: JSONObject): Any {
        return if (data is JSONObject) {
            GeckoBundle().apply {
                data.keys().forEach { key ->
                    putString(key, data[key].toString())
                }
            }
        } else
            data
    }
}