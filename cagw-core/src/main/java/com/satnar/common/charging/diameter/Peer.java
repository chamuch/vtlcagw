package com.satnar.common.charging.diameter;

public class Peer {
    
    private String address = null;
    private String hostId  = null;
    private String realm   = null;
    private String siteId  = null;
    
    
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getHostId() {
        return hostId;
    }
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
    public String getRealm() {
        return realm;
    }
    public void setRealm(String realm) {
        this.realm = realm;
    }
    public String getSiteId() {
        return siteId;
    }
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
    
    @Override
    public String toString() {
        return String.format("Peer [address=%s, hostId=%s, realm=%s, siteId=%s]", address, hostId, realm, siteId);
    }

    
    
}
