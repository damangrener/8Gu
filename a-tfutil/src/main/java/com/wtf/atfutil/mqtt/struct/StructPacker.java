package cn.ac.iscas.util.struct;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

public class StructPacker extends StructOutputStream {
    protected ByteArrayOutputStream bos;

    public StructPacker() {
        this(new ByteArrayOutputStream(), ByteOrder.BIG_ENDIAN);
    }

    public StructPacker(ByteOrder var1) {
        this(new ByteArrayOutputStream(), var1);
    }

    public StructPacker(OutputStream var1, ByteOrder var2) {
        super.init(var1, var2);
        this.bos = (ByteArrayOutputStream)var1;
    }

    public byte[] pack(Object var1) throws StructException {
        this.writeObject(var1);
        return this.bos.toByteArray();
    }

    public void writeObject(Object var1) throws StructException {
        if (var1 == null) {
            throw new StructException("Struct classes cant be null. ");
        } else {
            StructData var2 = StructUtils.getStructInfo(var1);
            boolean var3 = false;
            boolean var4 = false;
            Field[] var5 = var2.getFields();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                Field var8 = var5[var7];
                StructFieldData var9 = var2.getFieldData(var8.getName());
                if (var9 == null) {
                    throw new StructException("Field Data not found for field: " + var8.getName());
                }

                var3 = false;
                int var12 = 0;

                try {
                    if (var9.isArrayLengthMarker()) {
                        if (var9.requiresGetterSetter()) {
                            var12 = ((Number)var9.getGetter().invoke(var1, (Object[])null)).intValue();
                        } else {
                            var12 = ((Number)var9.getField().get(var1)).intValue();
                        }

                        var3 = true;
                    }

                    if (var9.requiresGetterSetter()) {
                        if (var3 && var12 >= 0) {
                            this.writeField(var8, var9.getGetter(), var1, var12);
                        } else {
                            this.writeField(var8, var9.getGetter(), var1, -1);
                        }
                    } else if (var3 && var12 >= 0) {
                        this.writeField(var8, (Method)null, var1, var12);
                    } else {
                        this.writeField(var8, (Method)null, var1, -1);
                    }
                } catch (Exception var11) {
                    throw new StructException(var11);
                }
            }

        }
    }
}
