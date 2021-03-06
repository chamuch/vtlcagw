package com.satnar.charging.diameter.scap.client;

import com.ericsson.pps.diameter.rfcapi.base.PeerConnectionListener;
import com.satnar.common.LogService;

public class DiameterPeerConnectionListener implements PeerConnectionListener {
    
    
    @Override
    public void peerAdded(String peer, boolean bSomething) {
        LogService.appLog.debug("Peer Added:" + peer + ", boolSomething: " + bSomething);
    }

    @Override
    public void peerConnected(String peer) {
        LogService.appLog.debug("Peer Connected:" + peer);
    }

    @Override
    public void peerDisconnected(String peer, int arg1, int arg2) {
        LogService.appLog.debug("Peer Disconnected:" + peer + ", May be DPR (Cause & Reason): " + arg1 + "," + arg2);
    }

    @Override
    public void peerRemoved(String peer) {
        LogService.appLog.debug("Peer Removed:" + peer);
    }
    
}
