package cn.ac.iscas.util.struct;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

public abstract class StructInputStream extends InputStream {
    DataInput dataInput;

    public StructInputStream() {
    }

    protected void init(InputStream var1, ByteOrder var2) {
        if (var2 == ByteOrder.LITTLE_ENDIAN) {
            this.dataInput = new LEDataInputStream(var1);
        } else {
            this.dataInput = new DataInputStream(var1);
        }

    }

    public abstract void readObject(Object var1) throws StructException;

    public void readField(Field var1, Method var2, Method var3, Object var4) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException, StructException {
        String var5 = var1.getType().getName();
        Constants.Primitive var6;
        if (!var1.getType().isArray()) {
            var6 = Constants.getPrimitive(var5);
            switch (var6) {
                case BOOLEAN:
                    if (var3 != null) {
                        var3.invoke(var4, this.readBoolean());
                    } else {
                        var1.setBoolean(var4, this.readBoolean());
                    }
                    break;
                case BYTE:
                    if (var3 != null) {
                        var3.invoke(var4, this.readByte());
                    } else {
                        var1.setByte(var4, this.readByte());
                    }
                    break;
                case SHORT:
                    if (var3 != null) {
                        var3.invoke(var4, this.readShort());
                    } else {
                        var1.setShort(var4, this.readShort());
                    }
                    break;
                case INT:
                    if (var3 != null) {
                        var3.invoke(var4, this.readInt());
                    } else {
                        var1.setInt(var4, this.readInt());
                    }
                    break;
                case LONG:
                    if (var3 != null) {
                        var3.invoke(var4, this.readLong());
                    } else {
                        var1.setLong(var4, this.readLong());
                    }
                    break;
                case CHAR:
                    if (var3 != null) {
                        var3.invoke(var4, this.readChar());
                    } else {
                        var1.setChar(var4, this.readChar());
                    }
                    break;
                case FLOAT:
                    if (var3 != null) {
                        var3.invoke(var4, this.readFloat());
                    } else {
                        var1.setFloat(var4, this.readFloat());
                    }
                    break;
                case DOUBLE:
                    if (var3 != null) {
                        var3.invoke(var4, this.readDouble());
                    } else {
                        var1.setDouble(var4, this.readDouble());
                    }
                    break;
                default:
                    if (var3 != null) {
                        Object var7 = var2.invoke(var4, (Object[]) null);
                        if (var7 == null) {
                            if (var1.getName().endsWith("CString")) {
                                throw new StructException("CString objects should be initialized :" + var1.getName());
                            }

                            var7 = var1.getType().newInstance();
                        }

                        this.readObject(var7);
                        var3.invoke(var4, var7);
                    } else {
                        this.handleObject(var1, var4);
                    }
            }
        }
        //扩展二维数组类型，var2取决于属性的修饰符
        else if (var5.startsWith("[[")) {
            var1.setAccessible(true);
            if (var2 != null && var1.get(var4) == null) {
                throw new StructException("Arrays ca not be null : " + var1.getName());
            }
            var6 = Constants.getPrimitive(var5.charAt(var5.length() - 1));
            switch (var6) {
                case BOOLEAN:
                    for (int i = 0; i < ((boolean[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readBooleanArray(((boolean[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readBooleanArray(((boolean[][]) var1.get(var4))[i]);
                        }
                    }
                    break;
                case BYTE:
                    for (int i = 0; i < ((byte[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readByteArray(((byte[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readByteArray(((byte[][]) var1.get(var4))[i]);
                        }
                    }
                    break;
                case SHORT:
                    for (int i = 0; i < ((short[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readShortArray(((short[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readShortArray(((short[][]) var1.get(var4))[i]);
                        }
                    }
                    break;
                case INT:
                    for (int i = 0; i < ((int[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readIntArray(((int[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readIntArray(((int[][]) var1.get(var4))[i]);
                        }
                    }
                    break;
                case LONG:
                    for (int i = 0; i < ((long[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readLongArray(((long[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readLongArray(((long[][]) var1.get(var4))[i]);
                        }
                    }
                    break;
                case CHAR:
                    for (int i = 0; i < ((char[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readCharArray(((char[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readCharArray(((char[][]) var1.get(var4))[i]);
                        }
                    }
                    break;
                case FLOAT:
                    for (int i = 0; i < ((float[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readFloatArray(((float[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readFloatArray(((float[][]) var1.get(var4))[i]);
                        }
                    }
                    break;
                case DOUBLE:
                    for (int i = 0; i < ((double[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readDoubleArray(((double[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readDoubleArray(((double[][]) var1.get(var4))[i]);
                        }
                    }
                    break;
                default:
                    for (int i = 0; i < ((Object[][]) var1.get(var4)).length; i++) {
                        if (var2 != null) {
                            this.readObjectArray(((Object[][]) var2.invoke(var4, (Object[]) null))[i]);
                        } else {
                            this.readObjectArray(((Object[][]) var1.get(var4))[i]);
                        }
                    }
            }
        } else {
            var6 = Constants.getPrimitive(var5.charAt(1));
            if (var2 != null && var2.invoke(var4, (Object[]) null) == null) {
                throw new StructException("Arrays ca not be null : " + var1.getName());
            }

            switch (var6) {
                case BOOLEAN:
                    if (var2 != null) {
                        this.readBooleanArray((boolean[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readBooleanArray((boolean[]) var1.get(var4));
                    }
                    break;
                case BYTE:
                    if (var2 != null) {
                        this.readByteArray((byte[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readByteArray((byte[]) var1.get(var4));
                    }
                    break;
                case SHORT:
                    if (var2 != null) {
                        this.readShortArray((short[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readShortArray((short[]) var1.get(var4));
                    }
                    break;
                case INT:
                    if (var2 != null) {
                        this.readIntArray((int[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readIntArray((int[]) var1.get(var4));
                    }
                    break;
                case LONG:
                    if (var2 != null) {
                        this.readLongArray((long[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readLongArray((long[]) var1.get(var4));
                    }
                    break;
                case CHAR:
                    if (var2 != null) {
                        this.readCharArray((char[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readCharArray((char[]) var1.get(var4));
                    }
                    break;
                case FLOAT:
                    if (var2 != null) {
                        this.readFloatArray((float[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readFloatArray((float[]) var1.get(var4));
                    }
                    break;
                case DOUBLE:
                    if (var2 != null) {
                        this.readDoubleArray((double[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readDoubleArray((double[]) var1.get(var4));
                    }
                    break;
                default:
                    if (var2 != null) {
                        this.readObjectArray((Object[]) var2.invoke(var4, (Object[]) null));
                    } else {
                        this.readObjectArray((Object[]) var1.get(var4));
                    }
            }
        }

    }

    public void handleObject(Field var1, Object var2) throws IllegalArgumentException, StructException, IOException, InstantiationException, IllegalAccessException {
        if (var1.get(var2) == null) {
            if (var1.getType().getName().endsWith("CString")) {
                throw new StructException("CString objects should be initialized before unpacking :" + var1.getName());
            }

            var1.set(var2, var1.getType().newInstance());
        }

        this.readObject(var1.get(var2));
    }

    public void close() throws IOException {
    }

    public int read() throws IOException {
        return -1;
    }

    protected boolean readBoolean() throws IOException {
        return this.dataInput.readBoolean();
    }

    protected byte readByte() throws IOException {
        return this.dataInput.readByte();
    }

    protected short readShort() throws IOException {
        return this.dataInput.readShort();
    }

    protected int readInt() throws IOException {
        return this.dataInput.readInt();
    }

    protected long readLong() throws IOException {
        return this.dataInput.readLong();
    }

    protected char readChar() throws IOException {
        return this.dataInput.readChar();
    }

    protected float readFloat() throws IOException {
        return this.dataInput.readFloat();
    }

    protected double readDouble() throws IOException {
        return this.dataInput.readDouble();
    }

    protected void readBooleanArray(boolean[] var1) throws IOException {
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = this.readBoolean();
        }

    }

    protected void readByteArray(byte[] var1) throws IOException, StructException {
        this.dataInput.readFully(var1);
    }

    protected void readCharArray(char[] var1) throws IOException {
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = this.readChar();
        }

    }

    protected void readShortArray(short[] var1) throws IOException {
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = this.readShort();
        }

    }

    protected void readIntArray(int[] var1) throws IOException {
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = this.readInt();
        }

    }

    protected void readLongArray(long[] var1) throws IOException {
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = this.readLong();
        }

    }

    protected void readFloatArray(float[] var1) throws IOException {
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = this.readFloat();
        }

    }

    protected void readDoubleArray(double[] var1) throws IOException {
        for (int var2 = 0; var2 < var1.length; ++var2) {
            var1[var2] = this.readDouble();
        }

    }

    protected void readObjectArray(Object[] var1) throws IOException, IllegalAccessException, InvocationTargetException, StructException {
        for (int var2 = 0; var2 < var1.length; ++var2) {
            this.readObject(var1[var2]);
        }

    }
}
