package cn.ac.iscas.util.struct;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

public abstract class StructOutputStream extends OutputStream {
    protected DataOutput dataOutput;

    protected StructOutputStream() {
    }

    protected void init(OutputStream var1, ByteOrder var2) {
        if (var2 == ByteOrder.LITTLE_ENDIAN) {
            this.dataOutput = new LEDataOutputStream(var1);
        } else {
            this.dataOutput = new DataOutputStream(var1);
        }

    }

    public abstract void writeObject(Object var1) throws StructException;

    public void writeField(Field var1, Method var2, Object var3, int var4) throws IllegalAccessException, IOException, InvocationTargetException, StructException {
        String var5 = var1.getType().getName();
        Constants.Primitive var6;
        if (!var1.getType().isArray()) {
            var6 = Constants.getPrimitive(var5);
            switch(var6) {
                case BOOLEAN:
                    if (var2 != null) {
                        this.writeBoolean((Boolean)var2.invoke(var3, (Object[])null));
                    } else {
                        this.writeBoolean(var1.getBoolean(var3));
                    }
                    break;
                case BYTE:
                    if (var2 != null) {
                        this.writeByte((Byte)var2.invoke(var3, (Object[])null));
                    } else {
                        this.writeByte(var1.getByte(var3));
                    }
                    break;
                case SHORT:
                    if (var2 != null) {
                        this.writeShort((Short)var2.invoke(var3, (Object[])null));
                    } else {
                        this.writeShort(var1.getShort(var3));
                    }
                    break;
                case INT:
                    if (var2 != null) {
                        this.writeInt((Integer)var2.invoke(var3, (Object[])null));
                    } else {
                        this.writeInt(var1.getInt(var3));
                    }
                    break;
                case LONG:
                    long var7;
                    if (var2 != null) {
                        var7 = (Long)var2.invoke(var3, (Object[])null);
                    } else {
                        var7 = var1.getLong(var3);
                    }

                    this.writeLong(var7);
                    break;
                case CHAR:
                    if (var2 != null) {
                        this.writeChar((Character)var2.invoke(var3, (Object[])null));
                    } else {
                        this.writeChar(var1.getChar(var3));
                    }
                    break;
                case FLOAT:
                    if (var2 != null) {
                        this.writeFloat((Float)var2.invoke(var3, (Object[])null));
                    } else {
                        this.writeFloat(var1.getFloat(var3));
                    }
                    break;
                case DOUBLE:
                    if (var2 != null) {
                        this.writeDouble((Double)var2.invoke(var3, (Object[])null));
                    } else {
                        this.writeDouble(var1.getDouble(var3));
                    }
                    break;
                default:
                    if (var2 != null) {
                        this.handleObject(var1, var2.invoke(var3, (Object[])null));
                    } else {
                        this.handleObject(var1, var3);
                    }
            }
        } else {
            var6 = Constants.getPrimitive(var1.getType().getName().charAt(1));
            switch(var6) {
                case BOOLEAN:
                    if (var2 != null) {
                        this.writeBooleanArray((boolean[])((boolean[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeBooleanArray((boolean[])((boolean[])var1.get(var3)), var4);
                    }
                    break;
                case BYTE:
                    if (var2 != null) {
                        this.writeByteArray((byte[])((byte[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeByteArray((byte[])((byte[])var1.get(var3)), var4);
                    }
                    break;
                case SHORT:
                    if (var2 != null) {
                        this.writeShortArray((short[])((short[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeShortArray((short[])((short[])var1.get(var3)), var4);
                    }
                    break;
                case INT:
                    if (var2 != null) {
                        this.writeIntArray((int[])((int[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeIntArray((int[])((int[])var1.get(var3)), var4);
                    }
                    break;
                case LONG:
                    if (var2 != null) {
                        this.writeLongArray((long[])((long[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeLongArray((long[])((long[])var1.get(var3)), var4);
                    }
                    break;
                case CHAR:
                    if (var2 != null) {
                        this.writeCharArray((char[])((char[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeCharArray((char[])((char[])var1.get(var3)), var4);
                    }
                    break;
                case FLOAT:
                    if (var2 != null) {
                        this.writeFloatArray((float[])((float[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeFloatArray((float[])((float[])var1.get(var3)), var4);
                    }
                    break;
                case DOUBLE:
                    if (var2 != null) {
                        this.writeDoubleArray((double[])((double[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeDoubleArray((double[])((double[])var1.get(var3)), var4);
                    }
                    break;
                default:
                    if (var2 != null) {
                        this.writeObjectArray((Object[])((Object[])var2.invoke(var3, (Object[])null)), var4);
                    } else {
                        this.writeObjectArray((Object[])((Object[])var1.get(var3)), var4);
                    }
            }
        }

    }

    public void handleObject(Field var1, Object var2) throws IllegalArgumentException, StructException, IllegalAccessException, IOException {
        this.writeObject(var1.get(var2));
    }

    public void close() throws IOException {
    }

    public void write(int var1) throws IOException {
    }

    public void writeBoolean(boolean var1) throws IOException {
        this.dataOutput.writeBoolean(var1);
    }

    public void writeByte(byte var1) throws IOException {
        this.dataOutput.writeByte(var1);
    }

    public void writeShort(short var1) throws IOException {
        this.dataOutput.writeShort(var1);
    }

    public void writeInt(int var1) throws IOException {
        this.dataOutput.writeInt(var1);
    }

    public void writeLong(long var1) throws IOException {
        this.dataOutput.writeLong(var1);
    }

    public void writeChar(char var1) throws IOException {
        this.dataOutput.writeChar(var1);
    }

    public void writeFloat(float var1) throws IOException {
        this.dataOutput.writeFloat(var1);
    }

    public void writeDouble(double var1) throws IOException {
        this.dataOutput.writeDouble(var1);
    }

    public void writeBooleanArray(boolean[] var1, int var2) throws IOException {
        if (var2 == -1 || var2 > var1.length) {
            var2 = var1.length;
        }

        for(int var3 = 0; var3 < var2; ++var3) {
            this.dataOutput.writeBoolean(var1[var3]);
        }

    }

    public void writeByteArray(byte[] var1, int var2) throws IOException {
        if (var2 != 0) {
            if (var2 == -1 || var2 > var1.length) {
                var2 = var1.length;
            }

            this.dataOutput.write(var1, 0, var2);
        }
    }

    public void writeCharArray(char[] var1, int var2) throws IOException {
        if (var2 == -1 || var2 > var1.length) {
            var2 = var1.length;
        }

        for(int var3 = 0; var3 < var2; ++var3) {
            this.dataOutput.writeChar(var1[var3]);
        }

    }

    public void writeShortArray(short[] var1, int var2) throws IOException {
        if (var2 == -1 || var2 > var1.length) {
            var2 = var1.length;
        }

        for(int var3 = 0; var3 < var2; ++var3) {
            this.dataOutput.writeShort(var1[var3]);
        }

    }

    public void writeIntArray(int[] var1, int var2) throws IOException {
        if (var2 == -1 || var2 > var1.length) {
            var2 = var1.length;
        }

        for(int var3 = 0; var3 < var2; ++var3) {
            this.dataOutput.writeInt(var1[var3]);
        }

    }

    public void writeLongArray(long[] var1, int var2) throws IOException {
        if (var2 == -1 || var2 > var1.length) {
            var2 = var1.length;
        }

        for(int var3 = 0; var3 < var2; ++var3) {
            this.dataOutput.writeLong(var1[var3]);
        }

    }

    public void writeFloatArray(float[] var1, int var2) throws IOException {
        if (var2 == -1 || var2 > var1.length) {
            var2 = var1.length;
        }

        for(int var3 = 0; var3 < var2; ++var3) {
            this.dataOutput.writeFloat(var1[var3]);
        }

    }

    public void writeDoubleArray(double[] var1, int var2) throws IOException {
        if (var2 == -1 || var2 > var1.length) {
            var2 = var1.length;
        }

        for(int var3 = 0; var3 < var2; ++var3) {
            this.dataOutput.writeDouble(var1[var3]);
        }

    }

    public void writeObjectArray(Object[] var1, int var2) throws IOException, IllegalAccessException, InvocationTargetException, StructException {
        if (var1 != null && var2 != 0) {
            if (var2 == -1 || var2 > var1.length) {
                var2 = var1.length;
            }

            for(int var3 = 0; var3 < var2; ++var3) {
                this.writeObject(var1[var3]);
            }

        }
    }
}
