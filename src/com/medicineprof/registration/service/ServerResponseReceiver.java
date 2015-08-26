package com.medicineprof.registration.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by neurons on 8/23/15.
 */
public class ServerResponseReceiver extends BroadcastReceiver {
    ServerReponseListener targetListener;

    public ServerResponseReceiver(ServerReponseListener listener){
        super();
        this.targetListener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        targetListener.onServerResponse(intent);
    }

    public interface ServerReponseListener{
        void onServerResponse(Intent intent);
    }
}
