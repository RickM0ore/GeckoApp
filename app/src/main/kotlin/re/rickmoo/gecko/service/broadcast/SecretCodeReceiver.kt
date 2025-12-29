package re.rickmoo.gecko.service.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import re.rickmoo.gecko.activity.HiddenConfigActivity

class SecretCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SECRET_CODE") {
            val i = Intent(context, HiddenConfigActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                Intent.FLAG_ACTIVITY_NO_HISTORY

            context.startActivity(i)
        }
    }
}