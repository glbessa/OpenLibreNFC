package me.cominixo.openlibrenfc;

public class LibreCommands {
    static final byte[] GET_ID = {
            (byte) 0x02, // Flag for un-addressed communication
            (byte) 0xa1, // Get Patch Info
            (byte) 0x07  // Vendor Identifier
    };
    static final byte[] GET_UID = {
            (byte) 0x26,
            (byte) 0x01,
            (byte) 0x00
    };
    static final byte[] ACTIVATE = {
            (byte) 0x02,
            (byte) 0xA0,
            (byte) 0x07,
            (byte) 0XC2,
            (byte) 0xAD,
            (byte) 0x75,
            (byte) 0x21
    };
    static final byte[] LOCK = {
            (byte) 0x02, // Flag for un-addressed communication
            (byte) 0XA2, // Lock
            (byte) 0x07  // Vendor identifier
    };
    static final byte[] UNLOCK = {
            (byte) 0x02, // Flag for un-addressed communication
            (byte) 0xA4, // Unlock
            (byte) 0x07  // Vendor identifier
    };
    static final byte[] READ_BLOCKS = {
            (byte) 0x02, // Flag for un-addressed communication
            (byte) 0x23, // Read Multiple Blocks
    };
    static final byte[] WRITE_BLOCKS = {
            (byte) 2, // Flags
            (byte) 0x21, // Write single block
    };
}
