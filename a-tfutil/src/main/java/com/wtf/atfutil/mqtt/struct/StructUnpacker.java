package cn.ac.iscas.util.struct;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

public class StructUnpacker extends StructInputStream {
    public StructUnpacker(byte[] var1) {
        this((InputStream)(new ByteArrayInputStream(var1)), ByteOrder.BIG_ENDIAN);
    }

    public StructUnpacker(byte[] var1, ByteOrder var2) {
        this((InputStream)(new ByteArrayInputStream(var1)), var2);
    }

    public StructUnpacker(InputStream var1, ByteOrder var2) {
        //bugfix,源代码var2参数无效
//        super.init(var1, ByteOrder.BIG_ENDIAN);
        super.init(var1, var2);
    }

    public void unpack(Object var1) throws StructException {
        this.readObject(var1);
    }

    @Override
    public void readObject(Object var1) throws StructException {
        if (var1 == null) {
            throw new StructException("Struct objects cannot be null.");
        } else {
            StructData var2 = StructUtils.getStructInfo(var1);
            Field[] var3 = var2.getFields();
            Field[] var4 = var3;
            int var5 = var3.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Field var7 = var4[var6];
                StructFieldData var8 = var2.getFieldData(var7.getName());
                if (var8 == null) {
                    throw new StructException("Field Data not found for field: " + var7.getName());
                }

                int var9 = -1;
                boolean var10 = false;

                try {
                    if (var2.isLenghtedArray(var7)) {
                        Field var11 = var2.getLenghtedArray(var7.getName());
                        StructFieldData var12 = var2.getFieldData(var11.getName());
                        if (var12.requiresGetterSetter()) {
                            var9 = ((Number)var12.getGetter().invoke(var1, (Object[])null)).intValue();
                        } else {
                            var9 = ((Number)var12.getField().get(var1)).intValue();
                        }

                        var10 = true;
                    }

                    if (var8.requiresGetterSetter()) {
                        Method var18 = var8.getGetter();
                        Method var20 = var8.getSetter();
                        if (var18 == null || var20 == null) {
                            throw new StructException(" getter/setter required for : " + var7.getName());
                        }

                        if (var10 && var9 >= 0) {
                            Object var21 = Array.newInstance(var7.getType().getComponentType(), var9);
                            var20.invoke(var1, var21);
                            if (!var7.getType().getComponentType().isPrimitive()) {
                                Object[] var14 = (Object[])((Object[])var21);

                                for(int var15 = 0; var15 < var9; ++var15) {
                                    var14[var15] = var7.getType().getComponentType().newInstance();
                                }
                            }
                        }

                        if (!var10 && var7.getType().isArray() && var18.invoke(var1, (Object[])null) == null) {
                            throw new StructException("Arrays can not be null :" + var7.getName());
                        }

                        this.readField(var7, var18, var20, var1);
                    } else {
                        if (var10 && var9 >= 0) {
                            Object var17 = Array.newInstance(var7.getType().getComponentType(), var9);
                            var7.set(var1, var17);
                            if (!var7.getType().getComponentType().isPrimitive()) {
                                Object[] var19 = (Object[])((Object[])var17);

                                for(int var13 = 0; var13 < var9; ++var13) {
                                    var19[var13] = var7.getType().getComponentType().newInstance();
                                }
                            }
                        }

                        if (!var10 && var7.getType().isArray() && var7.get(var1) == null) {
                            throw new StructException("Arrays can not be null. : " + var7.getName());
                        }

                        if (!var10 || var10 && var9 >= 0) {
                            this.readField(var7, (Method)null, (Method)null, var1);
                        }
                    }
                } catch (Exception var16) {
                    throw new StructException(var16);
                }
            }

        }
    }
}
