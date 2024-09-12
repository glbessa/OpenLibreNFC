package me.cominixo.openlibrenfc;

import static me.cominixo.openlibrenfc.LibreNfcUtils.bytesToHexStr;

import android.nfc.tech.NfcV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class OpenLibre {
    public String id;
    public String uid;
    public String type;
    public float age;
    public int region;
    public String status;
    public double temperature;
    private byte[] memory;

    public OpenLibre() {
        this.memory = new byte[LibreConstants.MEMORY_SIZE];
    }

    public void update(NfcV handler) throws IOException, Exception {
        this.getInfo(handler);
        this.readMemory(handler);
        this.age = this.getAgeFromMemory();
        this.region = this.getRegionFromMemory();
        this.status = this.getStatusFromMemory();
        this.temperature = this.getTemperatureFromMemory();
    }

    public void getInfo(NfcV handler) throws IOException {
        byte[] receivedId = LibreNfcUtils.sendCmd(handler, LibreCommands.GET_ID);
        byte[] receivedUid = LibreNfcUtils.sendCmd(handler, LibreCommands.GET_UID);

        if (receivedId.length != 0 && receivedUid.length != 0) {
            this.id = bytesToHexStr(Arrays.copyOfRange(receivedId, 1, receivedId.length));
            this.uid = bytesToHexStr(Arrays.copyOfRange(receivedUid, 2, receivedUid.length - 2));

            byte[] typeIdentifier = Arrays.copyOfRange(receivedId, 0, 3);
            this.type = LibreTypes.types.get(LibreNfcUtils.bytesToHexStr(typeIdentifier));
            if (this.type == null)
                this.type = "Unsupported";
        }
    }

    public float getAgeFromMemory() {
        return (float) (256 * (memory[317] & 0xFF) + (memory[316] & 0xFF)) / 1440;
    }

    public int getRegionFromMemory() {
        return memory[323];
    }

    public String getStatusFromMemory() {
        String statusMessage = LibreConstants.statuses.get((int) this.memory[4]);
        if (statusMessage == null)
            statusMessage = "Unknown";
        return statusMessage;
    }

    public double getTemperatureFromMemory() {
        int trendIndex = memory[26];
        int index = trendIndex - 1;
        if (index < 0)
            index += 16;
        float temp = (256 * memory[index * 6 + 32] + memory[index * 6 + 31]) & 0x3fff;
        // https://type1tennis.blogspot.com/2017/09/libre-other-bytes-well-some-of-them-at.html
        return Math.round((temp * 0.0027689 + 9.53) * 100.0) / 100.0;
    }

    public void activate(NfcV handler) throws IOException {
        LibreNfcUtils.sendCmd(handler, LibreCommands.ACTIVATE);
    }

    public void resetAge(NfcV handler) throws Exception {
        this.memory[317] = 0;
        this.memory[316] = 0;

        int[] memoryInt = new int[this.memory.length];
        for (int i = 0; i < this.memory.length; i++) {
            memoryInt[i] = this.memory[i] & 0xff;
        }

        int out = LibreNfcUtils.crc16(Arrays.copyOfRange(memoryInt, 26, 294 + 26));
        byte[] crc = ByteBuffer.allocate(4).putInt(out).array();

        this.memory[24] = crc[3];
        this.memory[25] = crc[2];

        LibreNfcUtils.unlock(handler, LibreConstants.PASSWORD);
        this.writeMemory(handler, this.memory);
        LibreNfcUtils.lock(handler, LibreConstants.PASSWORD);

        this.readMemory(handler);
    }

    public void readMemory(NfcV handler) throws Exception {
        this.memory = new byte[LibreConstants.MEMORY_SIZE];
        int blockStep = 3;
        // Loop through all blocks, 3 at a time
        for (int i = 0; i < this.memory.length / blockStep; i += blockStep) {
            byte[] cmd = {
                    (byte) 0x02, // Flag for un-addressed communication
                    (byte) 0x23, // Read Multiple Blocks
                    (byte) i, // Start block
                    (byte) 0x02 // Number of blocks to read (starts at 0 apparently, 2 is actually 3)
            };

            byte[] response = LibreNfcUtils.sendCmd(handler, cmd);
            if (response[0] != 0x00)
                throw new Exception("Error while reading from memory!");
            // ignore first 0 to get 24 bytes (8 per block)
            System.arraycopy(response, 1, this.memory, i * 8, response.length - 1);
        }
    }

    public void writeMemory(NfcV handler, byte[] newMemory) throws IOException {
        LibreNfcUtils.unlock(handler, LibreConstants.PASSWORD);
        for (int index = 0; index < 43; index++) {
            byte[] newData = new byte[8];
            System.arraycopy(newMemory, index * 8, newData, 0, 8);
            byte[] cmd = {
                (byte) 2, // Flags
                (byte) 0x21, // Write single block
                (byte) index, // Block to write
            };
            byte[] cmdWithBlocks = Arrays.copyOf(cmd, cmd.length + newData.length);
            System.arraycopy(newData, 0, cmdWithBlocks, cmd.length, newData.length);
            LibreNfcUtils.sendCmd(handler, cmdWithBlocks);
        }
        LibreNfcUtils.lock(handler, LibreConstants.PASSWORD);
    }

    public void loadMemoryFromFile(String filepath) {

    }

    public void dumpMemoryToFile(String filepath) {

    }

    /*
    public void loadDump(NfcV handler) throws Exception {
        StringBuilder text = new StringBuilder();

        File file = null;
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();

        byte[] newMemory = LibreNfcUtils.hexStrToBytes(text.toString());

        if (newMemory == null) {
            throw new Exception("The memory dump length was not the expected one! Check your memory_dump.txt");
        }
        this.memory = newMemory;

        LibreNfcUtils.unlock(handler);
        LibreNfcUtils.writeMemory(handler, this.memory);
        LibreNfcUtils.lock(handler);
    }
    */
}
