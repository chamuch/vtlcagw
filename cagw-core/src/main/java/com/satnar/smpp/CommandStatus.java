package com.satnar.smpp;

import java.util.Hashtable;
import java.util.Map;

import com.satnar.common.LogService;

public enum CommandStatus {
    ESME_ROK                (0x00000000, "OK"),
    ESME_RINVMSGLEN         (0x00000001, "Invalid Message Length"),
    ESME_RINVCMDLEN         (0x00000002, "Invalid Command Length"),
    ESME_RINVCMDID          (0x00000003, "Invalid Command ID"),
    ESME_RINVBNDSTS         (0x00000004, "Incorrect BIND Status for the command"),
    ESME_RALYBND            (0x00000005, "ESME Already in Bound State"),
    ESME_RINVPRTFLG         (0x00000006, "Invalid Priority Flag"),
    ESME_RINVREGDLVFLG      (0x00000007, "Invalid Registered Delivery Flag"),
    ESME_RSYSERR            (0x00000008, "System Error"),
    ESME_RINVSRCADR         (0x0000000A, "Invalid Source Address"),
    ESME_RINVDSTADR         (0x0000000B, "Invalid Dest Address"),
    ESME_RINVMSGID          (0x0000000C, "Invalid Message ID"),
    ESME_RBINDFAIL          (0x0000000D, "Bind Failed"),
    ESME_RINVPASWD          (0x0000000E, "Invalid Password"),
    ESME_RINVSYSID          (0x0000000F, "Invalid System ID"),
    ESME_RCANCELFAIL        (0x00000011, "Cancel SM Failed"),
    ESME_RREPLACEFAIL       (0x00000013, "Replace SM Failed"),
    ESME_RMSGQFUL           (0x00000014, "Message Queue Full"),
    ESME_RINVSERTYP         (0x00000015, "Invalid Service Type"),
    ESME_RINVNUMDESTS       (0x00000033, "Invalid Number of Destinations"),
    ESME_RINVDLNAME         (0x00000034, "Invalid Distribution List Name"),
    ESME_RINVDESTFLAG       (0x00000040, "Invalid Destination Flag (submit_multi)"),
    ESME_RINVSUBREP         (0x00000042, "Invalid 'submit with replace' request (replace_if_present flag)"),
    ESME_RINVESMCLASS       (0x00000043, "Invalid esm_class field data"),
    ESME_RCNTSUBDL          (0x00000044, "Cannot submit to Distribution List"),
    ESME_RSUBMITFAIL        (0x00000045, "submit_sm or submit_sm_multi failed"),
    ESME_RINVSRCTON         (0x00000048, "Invalid Source Address TON"),
    ESME_RINVSRCNPI         (0x00000049, "Invalid Source Address NPI"),
    ESME_RINVDSTTON         (0x00000050, "Invalid Destination Address TON"),
    ESME_RINVDSTNPI         (0x00000051, "Invalid Destination Address NPI"),
    ESME_RINVSYSTYP         (0x00000053, "Invalid System Type"),
    ESME_RINVREPFLAG        (0x00000054, "Invalid replace_if_present Flag"),
    ESME_RINVNUMMSGS        (0x00000055, "Invalid Number of Messages"),
    ESME_RTHROTTLED         (0x00000058, "Throttling Error (ESME has exceeded allowed message limits)"),
    ESME_RINVSCHED          (0x00000061, "Invalid Scheduled Delivery Time"),
    ESME_RINVEXPIRY         (0x00000062, "Invalid Message Validity Period (Expiry Time)"),
    ESME_RINVDFTMSGID       (0x00000063, "Predefined Message Invalid or Not Found"),
    ESME_RX_T_APPN          (0x00000064, "ESME Receiver Temporary App Error Code"),
    ESME_RX_P_APPN          (0x00000065, "ESME Receiver Permanent App Error Code"),
    ESME_RX_R_APPN          (0x00000066, "ESME Receiver Reject Message Error Code"),
    ESME_RQUERYFAIL         (0x00000067, "query_sm request failed"),
    ESME_RINVOPTPARSTREAM   (0x000000C0, "Error in Optional Part of PDU"),
    ESME_ROPTPARNOTALLWD    (0x000000C1, "Optional Parameter not Allowed"),
    ESME_RINVPARLEN         (0x000000C2, "Invalid Parameter Length"),
    ESME_RMISSINGOPTPARAM   (0x000000C3, "Expected Optional Parameter Missing"),
    ESME_RINVOPTPARAMVAL    (0x000000C4, "Invalid Optional Parameter Value"),
    ESME_RDELIVERYFAILURE   (0x000000FE, "Delivery Failure (Used for data_sm_resp)"),
    ESME_RUNKNOWNERR        (0x000000FF, "Unknown Error");
    
    private static final Map<Integer, CommandStatus> lookup = new Hashtable<Integer, CommandStatus>();
    
    private int status = 0;
    private String description = null;
    
    private CommandStatus(int code, String description) {
        this.status = code;
        this.description = description;
    }
    
    static {
        for (CommandStatus status: CommandStatus.values())
            lookup.put(status.status, status);
        //TODO: assert the hashtable is loaded
        LogService.appLog.info("SMPP-CommandStatus: Hash table loaded successfully !!");
    }
    
    public static CommandStatus valueOf(int code) {
        return lookup.get(code);
    }
    
    public int getLength() {
        return 4; // as per SMPP Specs 3.4 Issue 1.2
    }

    public int getCode() {
        return status;
    }

    public void setCode(int status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
}
