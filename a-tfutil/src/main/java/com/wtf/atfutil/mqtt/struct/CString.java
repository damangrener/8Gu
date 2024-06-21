package cn.ac.iscas.util.struct;

import java.io.Serializable;

@StructClass
public class CString implements Serializable {
    private static final long serialVersionUID = -3393948411351663341L;
    @StructField(
            order = 0
    )
    private byte[] buffer = null;

    public CString(int var1) {
        this.buffer = new byte[var1];
    }

    public CString(String var1, int var2) {
        this.buffer = new byte[var2];
        this.copyData(var1.getBytes(), var2);
    }

    public CString(byte[] var1, int var2) {
        this.buffer = new byte[var2];
        this.copyData(var1, var2);
    }

    public CString(String var1, char var2, int var3) {
        if (var1 == null) {
            var1 = "";
        }

        this.buffer = new byte[var3];

        for(int var4 = 0; var4 < this.buffer.length; ++var4) {
            this.buffer[var4] = (byte)var2;
        }

        this.copyData(var1.getBytes(), var3);
    }

    public CString(byte[] var1, char var2, int var3) {
        this.buffer = new byte[var3];

        for(int var4 = 0; var4 < this.buffer.length; ++var4) {
            this.buffer[var4] = (byte)var2;
        }

        this.copyData(var1, var3);
    }

    private void copyData(byte[] var1, int var2) {
        if (var1.length < var2) {
            System.arraycopy(var1, 0, this.buffer, 0, var1.length);
        } else {
            System.arraycopy(var1, 0, this.buffer, 0, var2);
        }

    }

    public boolean equals(Object var1) {
        CString var2 = (CString)var1;
        return var2.toString().equals(this.toString());
    }

    public void setString(String var1) {
        System.arraycopy(var1.getBytes(), 0, this.buffer, 0, var1.getBytes().length);
    }

    public String toString() {
        return (new String(this.buffer)).trim();
    }

    public String asCString() {
        int var1;
        for(var1 = 0; var1 < this.buffer.length && this.buffer[var1] != 0; ++var1) {
        }

        String var2 = new String(this.buffer, 0, var1);
        return var2;
    }

    public byte[] getBuffer() {
        return this.buffer;
    }

    public void setBuffer(byte[] var1) {
        this.buffer = var1;
    }
}
