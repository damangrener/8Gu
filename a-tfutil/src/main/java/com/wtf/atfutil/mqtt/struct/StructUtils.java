package cn.ac.iscas.util.struct;

import java.lang.reflect.Field;
import java.util.HashMap;

public class StructUtils {
    private static HashMap<String, StructData> structInfoCache = new HashMap();

    public StructUtils() {
    }

    public static synchronized StructData getStructInfo(Object var0) throws StructException {
        StructData var1 = (StructData)structInfoCache.get(var0.getClass().getName());
        if (var1 != null) {
            return var1;
        } else if (var0.getClass().getAnnotation(StructClass.class) == null) {
            throw new StructException("No struct Annotation found for " + var0.getClass().getName());
        } else {
            isAccessible(var0);
            Field[] var2 = var0.getClass().getDeclaredFields();
            Field[] var3 = new Field[var2.length];
            int var4 = 0;
            Field[] var5 = var2;
            int var6 = var2.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                Field var8 = var5[var7];
                StructField var9 = (StructField)var8.getAnnotation(StructField.class);
                if (var9 != null) {
                    int var10 = var9.order();
                    if (var10 < 0 || var10 >= var2.length) {
                        throw new StructException("Order is illegal for StructField : " + var8.getName());
                    }

                    ++var4;
                    var3[var10] = var8;
                }
            }

            var5 = new Field[var4];

            for(var6 = 0; var6 < var4; ++var6) {
                if (var3[var6] == null) {
                    throw new StructException("Order error for annotated fields! : " + var0.getClass().getName());
                }

                var5[var6] = var3[var6];
            }

            var1 = new StructData(var5, var0.getClass().getDeclaredMethods());
            structInfoCache.put(var0.getClass().getName(), var1);
            return var1;
        }
    }

    public static void isAccessible(Object var0) throws StructException {
        int var1 = var0.getClass().getModifiers();
        if ((var1 & 1) == 0) {
            throw new StructException("Struct operations are only accessible for public classes. Class: " + var0.getClass().getName());
        } else if ((var1 & 1536) != 0) {
            throw new StructException("Struct operations are not accessible for abstract classes and interfaces. Class: " + var0.getClass().getName());
        }
    }

    public static boolean requiresGetterSetter(int var0) {
        return var0 == 0 || (var0 & 6) != 0;
    }
}
