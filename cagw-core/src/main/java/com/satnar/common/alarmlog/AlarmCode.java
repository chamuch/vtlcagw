package com.satnar.common.alarmlog;


public enum AlarmCode {
    SYSTEM_START_UP (1, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.INFO, "System Started Up"),
    SYSTEM_SHUTDOWN (2, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.WARN, "System Shut Down"),
    SYSTEM_DEAD (3, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.FATAL, "System Process Found Dead"),
    
    SMS_CONNECTED (100, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.INFO, "SMSC: %s with IP(%s) connected"),
    SMS_CONNECTION_BROKE (101, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.ERROR, "SMSC: %s with IP(%s) connection broke with network issues/ server side problem"),
    SMS_DISCONNECTED (102, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.WARN, "SMSC: %s with IP(%s) disconnected, thru internal cleanup; potential response to server side behavior"),
    SMS_BOUND (103, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.INFO, "SMSC: %s with IP(%s) is now in BOUND state: %s"),
    SMS_UNBOUND (104, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.INFO, "SMSC: %s with IP(%s) is now in UNBOUND state: %s"),
    SMS_UNKNOWN_PDU (105, Mode.TRANSIENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.WARN, "SMSC: %s sent an UNKNOWN PDU with commandId: %s and sequence: %s"),
    SMS_THROTTLE_REJECT (106, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.ERROR, "SMSC: %s is being THROTTLED for uncontrolled traffic ingress"),
    SMS_THROTTLE_ABATE (107, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.ERROR, "SMSC: %s THOTTLE is abated for controlled traffic ingress"),
    SMS_CONGESTTION_DROP (108, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.ERROR, "SMSC: %s; PDU with commandId: %s and sequence: %s is dropped owing to congestion. SMSC will not receive response"),
    SMS_CONGESTTION_ABATE (109, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.INFO, "SMSC: %s processing is now resumed!"),
    
    MMS_PEER_CONNECTED (200, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.INFO, "MMSC identified with Peer: %s connected to DCC Service"),
    MMS_PEER_ADDED (201, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.INFO, "MMSC identified with Peer: %s now added to DCC Service PeerTable"),
    MMS_PEER_DISCONNECTED (202, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.WARN, "MMSC identified with Peer: %s disconnected from DCC Service"),
    MMS_PEER_REMOVED (203, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.WARN, "MMSC identified with Peer: %s now removed from DCC Service PeerTable"),
    MMS_UNSUPPORTED_REQUEST (204, Mode.TRANSIENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.ERROR, "MMSC sent a DCC Request: %s that is not supported by this DCC Service"),
    MMS_THROTTLE_REJECT (205, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.ERROR, "MMSC: %s is being THROTTLED for uncontrolled traffic ingress"),
    MMS_THROTTLE_ABATE (206, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.ERROR, "MMSC: %s THOTTLE is abated for controlled traffic ingress"),
    
    BL_SERVICE_UP (300, Mode.PERSISTENT, Type.CLEAR, NodeLocType.SERVICE_APPLICATION, Severity.INFO, "BizLogic service is now up and running"),
    BL_SERVICE_DOWN (301, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.WARN, "BizLogic service is shutdown"),
    BL_SERVICE_DEAD (301, Mode.PERSISTENT, Type.ALARM, NodeLocType.SERVICE_APPLICATION, Severity.FATAL, "BizLogic service is found dead"),
    
    
    ;
    
    private int alarmCode;
    private Mode alarmMode;
    private Type alarmType;
    private NodeLocType nodeLocationType;
    private Severity alarmSeverity;
    private String alarmMessage;
    
    
    
    
    private AlarmCode(int code, Mode mode, Type type, NodeLocType nodeLocationType, Severity severity, String message) {
        this.alarmCode = code;
        this.alarmMode = mode;
        this.alarmType = type;
        this.nodeLocationType = nodeLocationType;
        this.alarmSeverity = severity;
        this.alarmMessage = message;
    }
    
    enum Mode {
        PERSISTENT,
        TRANSIENT;
    }
    
    enum Type {
        ALARM,
        CLEAR,
        TRAP;
    }
    
    enum NodeLocType {
        IP,
        FQDN,
        HOST,
        GUEST,
        ZONE,
        VM,
        SERVICE_APPLICATION;
    }
    
    enum Severity {
        INFO,
        WARN,
        ERROR,
        FATAL;
    }

    public int getAlarmCode() {
        return alarmCode;
    }

    public void setAlarmCode(int alarmCode) {
        this.alarmCode = alarmCode;
    }

    public Mode getAlarmMode() {
        return alarmMode;
    }

    public void setAlarmMode(Mode alarmMode) {
        this.alarmMode = alarmMode;
    }

    public Type getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(Type alarmType) {
        this.alarmType = alarmType;
    }

    public NodeLocType getNodeLocationType() {
        return nodeLocationType;
    }

    public void setNodeLocationType(NodeLocType nodeLocationType) {
        this.nodeLocationType = nodeLocationType;
    }

    public Severity getAlarmSeverity() {
        return alarmSeverity;
    }

    public void setAlarmSeverity(Severity alarmSeverity) {
        this.alarmSeverity = alarmSeverity;
    }

    public String getAlarmMessage() {
        return alarmMessage;
    }

    public void setAlarmMessage(String alarmMessage) {
        this.alarmMessage = alarmMessage;
    }
    
    
    
}
