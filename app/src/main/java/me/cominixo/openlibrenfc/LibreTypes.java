package me.cominixo.openlibrenfc;

import java.util.HashMap;

class LibreTypes {
    static final byte[] LIBRE1_NEW_ID = {
            (byte) 0xA2,
            (byte) 0x08,
            (byte) 0x00
    };

    static final byte[] LIBRE1_OLD_ID = {
            (byte) 0xE9,
            (byte) 0x00,
            (byte) 0x00
    };

    static final byte[] LIBRE1_JAPAN_ID = {
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x04
    };

    static final byte[] LIBRE2_PLUS_ID = {
            (byte) 0x2B,
            (byte) 0x0A,
            (byte) 0x3A
    };

    static final HashMap<String, String> types = new HashMap<>();
    static {
        types.put(LibreNfcUtils.bytesToHexStr(LIBRE1_OLD_ID), "Libre 1 Old");
        types.put(LibreNfcUtils.bytesToHexStr(LIBRE1_NEW_ID), "Libre 1 New");
        types.put(LibreNfcUtils.bytesToHexStr(LIBRE1_JAPAN_ID), "Libre 1 Japan");
        types.put(LibreNfcUtils.bytesToHexStr(LIBRE2_PLUS_ID), "Libre 2 Plus");
    }
}
