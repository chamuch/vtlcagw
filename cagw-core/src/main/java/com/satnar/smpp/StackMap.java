package com.satnar.smpp;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.satnar.smpp.client.Esme;

public abstract class StackMap {
    
    private static Map<String, Esme> stackIndex = new Hashtable<String, Esme>();
    private static Map<String, String> messageIndex = new Hashtable<String, String>();
    private static List<String> inProgress = new ArrayList<String>();

    
    private StackMap() {}
    
    public static void addSession(String label, Esme stack) {
        stackIndex.put(label, stack);
    }
    
    public static void removeSession(String label) {
        if (label != null)
            stackIndex.remove(label);
    }
    
    public static Esme getStack(String label) {
        return stackIndex.get(label);
    }

    public static void addMessageIndex(String sequence, String esmeLabel) {
        messageIndex.put(sequence, esmeLabel);
    }

    public static void removeMessageIndex(String sequence) {
        if (sequence != null)
            messageIndex.remove(sequence);
    }
    
    public static String getEsmeLabel(String sequence) {
        return messageIndex.get(sequence);
    }

    public static Set<String> getSmppSessions() {
        return stackIndex.keySet();
    }
    
    public static void setInProgress(String esme) {
        if (!inProgress.contains(esme))
            inProgress.add(esme);
    }
    
    public static void unsetInProgress(String esme) {
        inProgress.remove(esme);
    }
    
    public static boolean checkInProgress(String esme) {
        return inProgress.contains(esme);
    }
}
