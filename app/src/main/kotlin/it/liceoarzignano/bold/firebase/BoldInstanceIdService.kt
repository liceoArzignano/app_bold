package it.liceoarzignano.bold.firebase

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.messaging.FirebaseMessaging
import it.liceoarzignano.bold.settings.AppPrefs


class BoldInstanceIdService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        FirebaseInstanceId.getInstance().token
        val prefs = AppPrefs(baseContext)
        val topic: String

        topic = if (prefs.get(AppPrefs.KEY_IS_TEACHER)) {
            ADDR6_TOPIC
        } else {
            when (prefs.get(AppPrefs.KEY_ADDRESS, "0")) {
                "1" -> ADDR1_TOPIC
                "2" -> ADDR2_TOPIC
                "3" -> ADDR3_TOPIC
                "4" -> ADDR4_TOPIC
                "5" -> ADDR5_TOPIC
                else -> ADDR6_TOPIC
            }
        }
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }

    companion object {
        private const val ADDR1_TOPIC = "Scientifico"
        private const val ADDR2_TOPIC = "ScApplicate"
        private const val ADDR3_TOPIC = "Linguistico"
        private const val ADDR4_TOPIC = "ScUmane"
        private const val ADDR5_TOPIC = "Economico"
        private const val ADDR6_TOPIC = "Docente"
    }
}
