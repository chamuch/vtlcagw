package com.satnar.smpp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.satnar.common.LogService;
import com.satnar.smpp.transport.SmppSessionState;

public enum CommandId {
    BIND_RECEIVER           (0x00000001, false, true, true, false, new SmppSessionState[]{SmppSessionState.OPEN}),
    BIND_RECEIVER_RESP      (0x80000001, false, true, false, true, new SmppSessionState[]{SmppSessionState.OPEN}),
    BIND_TRANSMITTER        (0x00000002, true, false, true, false, new SmppSessionState[]{SmppSessionState.OPEN}),
    BIND_TRANSMITTER_RESP   (0x80000002, true, false, false, true, new SmppSessionState[]{SmppSessionState.OPEN}),
    BIND_TRANSCEIVER        (0x00000009, true, false, true, false, new SmppSessionState[]{SmppSessionState.OPEN}),
    BIND_TRANSCEIVER_RESP   (0x80000009, true, false, false, true, new SmppSessionState[]{SmppSessionState.OPEN}),
    UNBIND                  (0x00000006, true, true, true, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    UNBIND_RESP             (0x80000006, true, true, true, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    ENQUIRE_LINK            (0x00000015, true, true, true, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    ENQUIRE_LINK_RESP       (0x80000015, true, true, true, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    GENERIC_NACK            (0x80000000, true, true, true, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    DATA_SM                 (0x00000103, true, true, true, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    DATA_SM_RESP            (0x80000103, true, true, true, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    SUBMIT_SM               (0x00000004, true, false, true, false, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    SUBMIT_SM_RESP          (0x80000004, true, false, false, true, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    SUBMIT_SM_MULTI         (0x00000021, true, false, true, false, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    SUBMIT_SM_MULTI_RESP    (0x80000021, true, false, false, true, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    QUERY_SM                (0x00000003, true, false, true, false, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    QUERY_SM_RESP           (0x80000003, true, false, false, true, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    CANCEL_SM               (0x00000008, true, false, true, false, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    CANCEL_SM_RESP          (0x80000008, true, false, false, true, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    REPLACE_SM              (0x00000007, true, false, true, false, new SmppSessionState[]{SmppSessionState.BOUND_TX}), // as per Issue v1.2 of SMPP3.4
    REPLACE_SM_RESP         (0x80000007, true, false, false, true, new SmppSessionState[]{SmppSessionState.BOUND_TX}), // as per Issue v1.2 of SMPP3.4
    DELIVER_SM              (0x00000005, false, true, false, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TRX}),
    DELIVER_SM_RESP         (0x80000005, false, true, true, false, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TRX}),
    ALERT_NOTIFICATION      (0x00000102, false, true, false, true, new SmppSessionState[]{SmppSessionState.BOUND_RX, SmppSessionState.BOUND_TRX}),
    EXTENDED                (0xffffffff, false, false, false, false, new SmppSessionState[]{}),
    //29-June-2015: HuaWei Extended commands
    AUTH_ACC                (0x00000004, true, false, true, false, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    AUTH_ACC_RESP           (0x80000004, true, false, false, true, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    SM_RESULT_NOTIFY        (0x00000004, true, false, true, false, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX}),
    SM_RESULT_NOTIFY_RESP   (0x80000004, true, false, false, true, new SmppSessionState[]{SmppSessionState.BOUND_TX, SmppSessionState.BOUND_TRX});
    
    
    private int     commandId    = 0;
    private boolean txCompatible = false;
    private boolean rxCompatible = false;
    private boolean esmeInitiated = false;
    private boolean smscInitiated = false;
    private List<SmppSessionState> requiredStates = null;
    
    private static final Map<Integer, CommandId> lookup = new Hashtable<Integer, CommandId>();
    private static final List<Integer> reserved = new ArrayList<Integer>();
    
    
    private CommandId(int command, boolean transmitterSupported, boolean receiverSupported, boolean esmeOriginated, boolean smscOriginated, SmppSessionState[] validStates) {
        this.commandId = command;
        this.txCompatible = transmitterSupported;
        this.rxCompatible = receiverSupported;
        this.esmeInitiated = esmeOriginated;
        this.smscInitiated = smscOriginated;
        this.requiredStates = new ArrayList<SmppSessionState>();
        for (SmppSessionState state: validStates)
            this.requiredStates.add(state);
    }
    
    static {
        for (CommandId commandId: CommandId.values())
            lookup.put(commandId.commandId, commandId);
        
        // pack reserved
        reserved.add(0x0000000A);
        reserved.add(0x8000000A);
        for (int i = 0x0000000C; i <= 0x00000014; i++)
            reserved.add(i);
        for (int i = 0x8000000B; i <= 0x80000014; i++)
            reserved.add(i);
        for (int i = 0x00000016; i <= 0x00000020; i++)
            reserved.add(i);
        for (int i = 0x80000016; i <= 0x80000020; i++)
            reserved.add(i);
        
        //TODO: assert the hashtable & reserved list are loaded
        LogService.appLog.info("SMPP-CommandId: Hash table loaded successfully !!");
    }
    
    public static CommandId valueOf(int commandId) {
        CommandId result = lookup.get(commandId);
        if (result == null) {
            if (reserved.contains(commandId)) {
                return EXTENDED;
            }
        }
        return result;
    }
    
    public int getLength() {
        return 4; // as per SMPP Specs 3.4 Issue 1.2
    }

    public int getId() {
        return commandId;
    }

    public void setId(int commandId) {
        if (this == EXTENDED)
            this.commandId = commandId;
    }

    public boolean isTxCompatible() {
        return txCompatible;
    }

    public void setTxCompatible(boolean txCompatible) {
        if (this == EXTENDED)
            this.txCompatible = txCompatible;
    }

    public boolean isRxCompatible() {
        return rxCompatible;
    }

    public void setRxCompatible(boolean rxCompatible) {
        if (this == EXTENDED)
            this.rxCompatible = rxCompatible;
    }

    public boolean isEsmeInitiated() {
        return esmeInitiated;
    }

    public void setEsmeInitiated(boolean esmeInitiated) {
        if (this == EXTENDED)
            this.esmeInitiated = esmeInitiated;
    }

    public boolean isSmscInitiated() {
        return smscInitiated;
    }

    public void setSmscInitiated(boolean smscInitiated) {
        if (this == EXTENDED)
            this.smscInitiated = smscInitiated;
    }

    public List<SmppSessionState> getRequiredStates() {
        return requiredStates;
    }

    public void setRequiredStates(List<SmppSessionState> requiredStates) {
        if (this == EXTENDED)
            this.requiredStates = requiredStates;
    }
    
    
}
