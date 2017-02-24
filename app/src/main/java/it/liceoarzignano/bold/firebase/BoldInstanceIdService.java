package it.liceoarzignano.bold.firebase;

import android.content.Context;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import it.liceoarzignano.bold.Utils;


public class BoldInstanceIdService extends FirebaseInstanceIdService {
    private static final String ADDR1_TOPIC = "Scientifico";
    private static final String ADDR2_TOPIC = "ScApplicate";
    private static final String ADDR3_TOPIC = "Linguistico";
    private static final String ADDR4_TOPIC = "ScUmane";
    private static final String ADDR5_TOPIC = "Economico";
    private static final String ADDR6_TOPIC = "Docente";

    @Override
    public void onTokenRefresh() {
        FirebaseInstanceId.getInstance().getToken();
        Context context = getApplicationContext();
        String topic;

        if (Utils.isTeacher(context)) {
            topic = ADDR6_TOPIC;
        } else {
            switch (Utils.getAddress(context)) {
                case "1":
                    topic = ADDR1_TOPIC;
                    break;
                case "2":
                    topic = ADDR2_TOPIC;
                    break;
                case "3":
                    topic = ADDR3_TOPIC;
                    break;
                case "4":
                    topic = ADDR4_TOPIC;
                    break;
                case "5":
                    topic = ADDR5_TOPIC;
                    break;
                default:
                    topic = ADDR6_TOPIC;
                    break;
            }
        }
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }
}
