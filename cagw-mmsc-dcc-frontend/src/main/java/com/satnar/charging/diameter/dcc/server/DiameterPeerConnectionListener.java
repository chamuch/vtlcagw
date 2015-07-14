package com.satnar.charging.diameter.dcc.server;

import com.ericsson.pps.diameter.rfcapi.base.PeerConnectionListener;

public class DiameterPeerConnectionListener implements PeerConnectionListener {
    
    
    @Override
    public void peerAdded(String arg0, boolean arg1) {
        // TODO: Just log for the moment... we will talk about load balancer later.
    }

    @Override
    public void peerConnected(String arg0) {
        // TODO: Just log for the moment... we will talk about load balancer later.
    }

    @Override
    public void peerDisconnected(String arg0, int arg1, int arg2) {
        // TODO: Just log for the moment... we will talk about load balancer later.
    }

    @Override
    public void peerRemoved(String arg0) {
        // TODO: Just log for the moment... we will talk about load balancer later.
    }
    
}
