package cn.ac.iscas.util.struct;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LEDataOutputStream implements DataOutput {
    protected DataOutputStream d;
    byte[] w;

    public LEDataOutputStream(OutputStream var1) {
        this.d = new DataOutputStream(var1);
        this.w = new byte[8];
    }

    public final void writeShort(int var1) throws IOException {
        this.w[0] = (byte)var1;
        this.w[1] = (byte)(var1 >> 8);
        this.d.write(this.w, 0, 2);
    }

    public final void writeChar(int var1) throws IOException {
        this.w[0] = (byte)var1;
        this.w[1] = (byte)(var1 >> 8);
        this.d.write(this.w, 0, 2);
    }

    public final void writeInt(int var1) throws IOException {
        this.w[0] = (byte)var1;
        this.w[1] = (byte)(var1 >> 8);
        this.w[2] = (byte)(var1 >> 16);
        this.w[3] = (byte)(var1 >> 24);
        this.d.write(this.w, 0, 4);
    }

    public final void writeLong(long var1) throws IOException {
        this.w[0] = (byte)((int)var1);
        this.w[1] = (byte)((int)(var1 >> 8));
        this.w[2] = (byte)((int)(var1 >> 16));
        this.w[3] = (byte)((int)(var1 >> 24));
        this.w[4] = (byte)((int)(var1 >> 32));
        this.w[5] = (byte)((int)(var1 >> 40));
        this.w[6] = (byte)((int)(var1 >> 48));
        this.w[7] = (byte)((int)(var1 >> 56));
        this.d.write(this.w, 0, 8);
    }

    public final void writeFloat(float var1) throws IOException {
        this.writeInt(Float.floatToIntBits(var1));
    }

    public final void writeDouble(double var1) throws IOException {
        this.writeLong(Double.doubleToLongBits(var1));
    }

    public final void writeChars(String var1) throws IOException {
        int var2 = var1.length();

        for(int var3 = 0; var3 < var2; ++var3) {
            this.writeChar(var1.charAt(var3));
        }

    }

    public final synchronized void write(int var1) throws IOException {
        this.d.write(var1);
    }

    public final synchronized void write(byte[] var1, int var2, int var3) throws IOException {
        this.d.write(var1, var2, var3);
    }

    public void flush() throws IOException {
        this.d.flush();
    }

    public final void writeBoolean(boolean var1) throws IOException {
        this.d.writeBoolean(var1);
    }

    public final void writeByte(int var1) throws IOException {
        this.d.writeByte(var1);
    }

    public final void writeBytes(String var1) throws IOException {
        this.d.writeBytes(var1);
    }

    public final void writeUTF(String var1) throws IOException {
        this.d.writeUTF(var1);
    }

    public final int size() {
        return this.d.size();
    }

    public final void write(byte[] var1) throws IOException {
        this.d.write(var1, 0, var1.length);
    }

    public final void close() throws IOException {
        this.d.close();
    }
}

