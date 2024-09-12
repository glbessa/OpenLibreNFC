package me.cominixo.openlibrenfc;

import android.app.Activity;
import android.nfc.tech.NfcV;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LibreNfcUtils {
    public static byte[] sendCmd(NfcV handle, byte[] cmd) throws IOException {
        if (!handle.isConnected()) {
            handle.connect();
        }
        return handle.transceive(cmd);
    }

    public static String bytesToHexStr(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (Byte b : bytes) {
            out.append(String.format("%02X", b)).append(" ");
        }
        return out.toString();
    }

    public static byte[] hexStrToBytes(String string) {
        byte[] bytes = new byte[360];

        List<String> cleanString = new ArrayList<>();
        for (String s : string.split(" ")) {
            if (!s.trim().isEmpty())
                cleanString.add(s.trim());
        }

        if (cleanString.size() != 360) {
            return null;
        }

        for (int i = 0; i < cleanString.size(); i++) {
            int byteInt = Integer.parseInt(cleanString.get(i), 16);
            bytes[i] = (byte) byteInt;
        }

        return bytes;
    }

    public static int crc16 (int[] data) {
        int crc = 0x0000FFFF;
        for (int datum : data) {
            crc = ((crc >> 8) & 0x0000ffff) | ((crc << 8) & 0x0000ffff);
            crc ^= bitRev((byte) datum);
            crc ^= (((crc & 0xff) >> 4) & 0x0000ffff);
            crc ^= ((crc << 12) & 0x0000ffff);
            crc ^= (((crc & 0xff) << 5) & 0x0000ffff);
        }
        return crc;
    }

    public static int bitRev(byte data) {
        return ((data << 7) & 0x80) | ((data << 5) & 0x40) | (data << 3) & 0x20 | (data << 1) & 0x10 | (data >> 7) & 0x01 | (data >> 5) & 0x02 | (data >> 3) & 0x04 | (data >> 1) & 0x08;
    }

    public static void unlock(NfcV handler, byte[] password) throws IOException {
        byte[] cmdWithPassword = Arrays.copyOf(LibreCommands.UNLOCK, LibreCommands.UNLOCK.length + LibreConstants.PASSWORD.length);
        System.arraycopy(LibreConstants.PASSWORD, 0, cmdWithPassword, LibreCommands.UNLOCK.length, LibreConstants.PASSWORD.length);
        sendCmd(handler, cmdWithPassword);
    }

    public static void lock(NfcV handler, byte[] password) throws IOException {
        byte[] cmdWithPassword = Arrays.copyOf(LibreCommands.LOCK, LibreCommands.LOCK.length + LibreConstants.PASSWORD.length);
        System.arraycopy(LibreConstants.PASSWORD, 0, cmdWithPassword, LibreCommands.LOCK.length, LibreConstants.PASSWORD.length);
        sendCmd(handler, cmdWithPassword);
    }
}
