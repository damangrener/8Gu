package cn.ac.iscas.util.struct;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

public class JavaStruct {
    public JavaStruct() {
    }

    public static final byte[] pack(Object var0) throws StructException {
        return pack(var0, ByteOrder.BIG_ENDIAN);
    }

    public static final byte[] pack(Object var0, ByteOrder var1) throws StructException {
        StructPacker var2 = new StructPacker(var1);
        return var2.pack(var0);
    }

    public static StructPacker getPacker(OutputStream var0, ByteOrder var1) {
        return new StructPacker(var0, var1);
    }

    public static final void unpack(Object var0, byte[] var1) throws StructException {
        unpack(var0, var1, ByteOrder.BIG_ENDIAN);
    }

    public static final void unpack(Object var0, byte[] var1, ByteOrder var2) throws StructException {
        StructUnpacker var3 = new StructUnpacker(var1, var2);
        var3.unpack(var0);
    }

    public static StructUnpacker getUnpacker(InputStream var0, ByteOrder var1) {
        return new StructUnpacker(var0, var1);
    }
}
