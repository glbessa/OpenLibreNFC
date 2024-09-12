package me.cominixo.openlibrenfc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LibreConstants {
    static final int MEMORY_SIZE = 360;

    // Password to unlock/lock the chip
    static final byte[] PASSWORD = {
            (byte) 194,
            (byte) 173,
            (byte) 117,
            (byte) 33
    };

    static final byte[] OK_RESPONSE = {
            (byte) 0x00
    };

    static final byte[] ERROR_RESPONSE = {
            (byte) 0x01,
            (byte) 0x01
    };

    static final HashMap<Integer, String> statuses = new HashMap<>();
    static {
        statuses.put(1, "New (Not activated)");
        statuses.put(2, "In warmup");
        statuses.put(3, "Activated");
        statuses.put(5, "Expired");
        statuses.put(6, "Error");
    }
}
