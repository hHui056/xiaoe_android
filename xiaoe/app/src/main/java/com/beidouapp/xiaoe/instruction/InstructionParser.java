package com.beidouapp.xiaoe.instruction;

import android.util.Log;

import com.beidouapp.xiaoe.utils.HexUtil;

import java.util.Arrays;

/**
 * @author hHui
 *         解析指令。
 */
public class InstructionParser {
    private static final String TAG = Instruction.class.getSimpleName();

    public InstructionParser() {
    }

    /**
     * 解析成一条指令。
     *
     * @param content 指令的字节数组
     * @return 解析后的指令，或者null。
     */
    public Instruction parseInstruction(byte[] content) {
        Log.i("接受", "解析消息");
        if (content == null) {
            Log.e(TAG, "content is null");
            return null;
        }

        // 校验header -> 校验length -> 校验bcc ->
        // 解析cmd -> 解析type -> 解析seq -> 解析data

        Instruction instruction = new Instruction();
        // verify header
        if (content[0] != (byte) 0xFF || content[1] != (byte) 0xFF) {
            Log.e(TAG, "header is wrong");
            return null;
        }

        // verify length
        int length = (content[2] << 8) ^ content[3];
        if (length != content.length - 4) {
            Log.e(TAG, "length is wrong");
            return null;
        }

        // verify bcc
        byte bcc = content[2];
        for (int i = 3; i < content.length - 1; i++) {
            bcc ^= content[i];
        }
        if (bcc != content[content.length - 1]) {
            Log.e(TAG, "bcc is wrong" + HexUtil.byteToHexStr(bcc));

            return null;
        }

        if (!parseCmd(content[4], instruction)) {
            Log.e(TAG, "cmd field is wrong");
            return null;
        }


        instruction.seq = content[5];
        if (instruction.seq <= 0) {
            Log.e(TAG, "seq field is below 0");
            return null;

        }

        byte[] data = Arrays.copyOfRange(content, 6, content.length - 1);
        if (!parseData(data, instruction)) {
            Log.e(TAG, "data field is invalid");
            return null;
        }
        return instruction;

    }

    protected boolean parseCmd(byte cmd, Instruction instruction) {
        if (!Instruction.Cmd.verify(cmd)) {
            return false;
        }
        instruction.cmd = cmd;
        return true;
    }


    protected boolean parseData(byte[] data, Instruction instruction) {
        Body body = null;
        switch (instruction.getCmd()) {
            case Instruction.Cmd.QUERY_BACK://查询报文回复
                if (data[0] == Instruction.DATA0.TEMPERA_HUM.BOTH) {
                    Log.i(TAG, "解析温湿度查询应答报文");
                    if (data[1] == 0x00) {
                        body = new TemperatureAndHumidityResBody();
                        body.parseContent(data);
                        instruction.body = body;
                        body.setIsAvailable(true);
                    } else {
                        Log.w(TAG, "发送报文不正确" + data[1]);
                    }
                } else if (data[0] == Instruction.DATA0.AIRPRESS) {
                    Log.i(TAG, "解析大气压查询应答报文");
                    if (data[1] == 0x00) {
                        body = new AirResBody();
                        body.parseContent(data);
                        instruction.body = body;
                        body.setIsAvailable(true);
                    } else {
                        Log.w(TAG, "发送报文不正确" + data[1]);
                    }
                }
                break;
            case Instruction.Cmd.CONTROL_BACK://控制报文回复
                if (data[0] == Instruction.DATA0.RGB) {//RGB控制反馈

                } else if (data[0] == Instruction.DATA0.LED) {//LED控制反馈

                }
                break;
        }
        return body != null && body.isAvailable();
    }
}
