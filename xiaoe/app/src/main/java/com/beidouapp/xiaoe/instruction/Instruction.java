package com.beidouapp.xiaoe.instruction;


import java.io.Serializable;

/**
 * @author hHui
 *         <p>
 *         与设备通信的私有协议。
 *         <p/>
 *         封装温湿度数据。
 */
public class Instruction implements Serializable {
    private static final String TAG = Instruction.class.getSimpleName();
    // ===========================================================================================================
    //  filed             length               description
    //  start_flag          2               固定头部，0xFF,0xFF
    //  length              2               cmd 开始到整个数据包结束所占用的字节数
    //  cmd                 1               表示具体的命令含义，详见命令数据详解
    //  seq                 1               发送者给出的序号，回复者必须把相应序号返回发送者
    //  data                n               具体数据，详见命令数据详解说明
    //  bcc                 1               数据校验，length到data数据校验和（异或）
    // ===========================================================================================================
    // 2 bytes
    protected byte[] header = new byte[]{(byte) 0xFF, (byte) 0xFF};
    // 2 bytes
    protected byte[] length = new byte[2];
    // 1 byte
    protected byte cmd;
    // 1 byte
    protected byte seq;
    // n bytes
    protected Body body;
    // 1 byte
    protected byte bcc;

    public int getLength() {
        return (this.length[0] << 8) ^ this.length[1];
    }

    public byte getCmd() {
        return cmd;
    }
    public byte getSeq() {
        return seq;
    }

    public Body getBody() {
        return body;
    }

    public byte[] toByteArray() {
        int i = 0;
        byte[] input = new byte[getLength() + 4];
        input[i++] = this.header[0];
        input[i++] = this.header[1];
        input[i++] = this.length[0];
        input[i++] = this.length[1];
        input[i++] = this.cmd;
        input[i++] = this.seq;
        if (this.body != null) {
            byte[] data = this.body.toByteArray();
            for (int j = 0; j < data.length; j++) {
                input[i++] = data[j];
            }
        }
        input[i++] = this.bcc;
        return input;
    }
    /**
     * Cmd：指令
     */
    public static class Cmd {
        public static final byte CONTROL = 0X70;//控制
        public static final byte CONTROL_BACK = 0x7f;//控制反馈
        public static final byte QUERY = (byte) 0x80;//查询
        public static final byte QUERY_BACK = (byte) 0x8f;//查询反馈

        public static boolean verify(byte cmd) {
            if (cmd != CONTROL && cmd != CONTROL_BACK &&
                    cmd != QUERY && cmd != QUERY_BACK) {
                return false;
            }
            return true;
        }
    }

    /**
     * Data: 数据字段，第一个字节data[0]表示设备类型
     */
    public static class DATA0 {
        public static final byte RGB = 0X10;//Rgb灯
        public static final byte AIRPRESS = 0x30;//气压传感器
        public static final byte LED = 0x40;//LED显示
        public class TEMPERA_HUM {
            public static final byte BOTH = 0X20;//温湿度
        }
    }
    /**
     * 生成指令
     */
    public static class Builder {
        // 2 bytes
        private static final byte[] header = new byte[]{(byte) 0xFF, (byte) 0xFF};
        // 1 byte
        private static byte seq = 0;
        // 2 bytes
        private byte[] length = new byte[2];
        // 1 byte
        private byte cmd;
        // n bytes
        private Body body;
        // 1 byte
        private byte bcc;
        public Builder() {

        }

        /**
         * 全局唯一的序列号
         *
         * @return
         */
        private static byte generateSeqNumber() {
            return seq++ < 0 ? 0 : seq;
        }

        public Builder setCmd(byte cmd) {
            this.cmd = cmd;
            return this;
        }

        public Builder setBody(Body body) {
            this.body = body;
            return this;
        }
        /**
         * 根据设置的字段值，生成一个条新指令。
         *
         * @return 生成的指令，或者返回null。
         */
        public Instruction createInstruction() {
            if (!Cmd.verify(this.cmd)) {
                return null;
            }
            Instruction instruction = new Instruction();
            instruction.header = this.header;
            calculateLength();
            instruction.length = this.length;
            instruction.cmd = this.cmd;
            instruction.seq = generateSeqNumber();
            instruction.body = this.body;
            instruction.bcc = calculateBcc();
            return instruction;
        }

        private void calculateLength() {
            int length = 1 + 1 + (this.body == null ? 0 : this.body.getLength()) + 1;
            this.length[0] = (byte) (length >> 8);
            this.length[1] = (byte) length;
        }
        private byte calculateBcc() {
            // 封装从length到data的字节数组
            int length = 2 + 1 + 1 + 1 + (this.body == null ? 0 : this.body.getLength());
            int i = 0;
            byte[] input = new byte[length];
            input[i++] = this.length[0];
            input[i++] = this.length[1];
            input[i++] = this.cmd;
            input[i++] = Builder.seq;
            if (this.body != null) {
                byte[] data = this.body.toByteArray();
                for (int j = 0; j < data.length; j++) {
                    input[i++] = data[j];
                }
            }
            // 计算异或校验码
            byte xor = input[0];
            for (int k = 1; k < input.length; k++) {
                xor ^= input[k];
            }
            return xor;
        }
    }
}
