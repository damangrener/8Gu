package cn.ac.iscas.util.struct;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class StructData {
    Field[] fields = null;
    Method[] methods = null;
    HashMap<String, Field> lengthedArrayFields = new HashMap();
    HashMap<String, StructFieldData> fieldDataMap = new HashMap();
    static int ACCEPTED_MODIFIERS = 7;

    public StructData(Field[] var1, Method[] var2) throws StructException {
        this.fields = var1;
        this.methods = var2;
        Field[] var3 = var1;
        int var4 = var1.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Field var6 = var3[var5];
            if ((var6.getModifiers() & ~ACCEPTED_MODIFIERS) != 0 || (var6.getModifiers() | ACCEPTED_MODIFIERS) == 0) {
                throw new StructException("Field type should be public, private or protected : " + var6.getName());
            }

            StructFieldData var7 = new StructFieldData(var6);
            ArrayLengthMarker var8 = (ArrayLengthMarker)var6.getAnnotation(ArrayLengthMarker.class);
            if (var8 != null) {
                var7.setArrayLengthMarker(true);

                int var9;
                for(var9 = 0; var9 < var1.length; ++var9) {
                    if (var8.fieldName().equals(var1[var9].getName())) {
                        this.lengthedArrayFields.put(var1[var9].getName(), var6);
                        break;
                    }
                }

                if (var9 == var1.length) {
                    throw new StructException("Lenght Marker Fields target is not found: " + var8.fieldName());
                }
            }

            if (StructUtils.requiresGetterSetter(var6.getModifiers())) {
                var7.setGetter(getGetterName(var2, var6));
                var7.setSetter(getSetterName(var2, var6));
                var7.setRequiresGetterSetter(true);
            }

            var7.setType(Constants.getPrimitive(var6));
            this.fieldDataMap.put(var6.getName(), var7);
        }

    }

    public StructFieldData getFieldData(String var1) {
        return (StructFieldData)this.fieldDataMap.get(var1);
    }

    private static final Method getGetterName(Method[] var0, Field var1) throws StructException {
        String var2 = "get" + var1.getName();
        String var3 = "is" + var1.getName();

        int var4;
        for(var4 = 0; var4 < var0.length; ++var4) {
            if (var0[var4].getName().equalsIgnoreCase(var2)) {
                return var0[var4];
            }
        }

        if (var1.getType().getName().equals("boolean")) {
            for(var4 = 0; var4 < var0.length; ++var4) {
                if (var0[var4].getName().equalsIgnoreCase(var3)) {
                    return var0[var4];
                }
            }
        }

        throw new StructException("The field needs a getter method, but none supplied. Field: " + var1.getName());
    }

    private static final Method getSetterName(Method[] var0, Field var1) throws StructException {
        String var2 = "set" + var1.getName();

        for(int var3 = 0; var3 < var0.length; ++var3) {
            if (var0[var3].getName().equalsIgnoreCase(var2)) {
                return var0[var3];
            }
        }

        throw new StructException("The field needs a setter method, but none supplied. Field: " + var1.getName());
    }

    public Field[] getFields() {
        return this.fields;
    }

    public Method[] getMethods() {
        return this.methods;
    }

    public boolean isLenghtedArray(Field var1) {
        return this.lengthedArrayFields.get(var1.getName()) != null;
    }

    public Field getLenghtedArray(String var1) {
        return (Field)this.lengthedArrayFields.get(var1);
    }
}
