package com.satnar.charging.diameter.dcc.server;

import com.ericsson.pps.diameter.rfcapi.base.PeerConnectionListener;
import com.satnar.common.LogService;
import com.satnar.common.alarmlog.AlarmCode;

public class DiameterPeerConnectionListener implements PeerConnectionListener {
    
    
    @Override
    public void peerAdded(String peer, boolean bSomething) {
        LogService.appLog.debug("Peer Added:" + peer + ", boolSomething: " + bSomething);
        LogService.alarm(AlarmCode.MMS_PEER_ADDED, peer);
    }

    @Override
    public void peerConnected(String peer) {
        LogService.appLog.debug("Peer Connected:" + peer);
        LogService.alarm(AlarmCode.MMS_PEER_CONNECTED, peer);
    }

    @Override
    public void peerDisconnected(String peer, int arg1, int arg2) {
        LogService.appLog.debug("Peer Disconnected:" + peer + ", May be DPR (Cause & Reason): " + arg1 + "," + arg2);
        LogService.alarm(AlarmCode.MMS_PEER_DISCONNECTED, peer);
    }

    @Override
    public void peerRemoved(String peer) {
        LogService.appLog.debug("Peer Removed:" + peer);
        LogService.alarm(AlarmCode.MMS_PEER_REMOVED, peer);
    }
    
}
