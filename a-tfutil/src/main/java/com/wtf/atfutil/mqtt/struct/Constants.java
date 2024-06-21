package cn.ac.iscas.util.struct;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Constants {
    private static HashMap<String, Primitive> primitiveTypes = new HashMap();
    private static HashMap<Character, Primitive> signatures = new HashMap();

    public Constants() {
    }

    public static final Primitive getPrimitive(Field var0) {
        return !var0.getType().isArray() ? getPrimitive(var0.getType().getName()) : getPrimitive(var0.getType().getName().charAt(1));
    }

    public static final Primitive getPrimitive(String var0) {
        Primitive var1 = (Primitive)primitiveTypes.get(var0);
        return var1 != null ? var1 : Primitive.OBJECT;
    }

    public static final Primitive getPrimitive(char var0) {
        Primitive var1 = (Primitive)signatures.get(var0);
        return var1 != null ? var1 : Primitive.OBJECT;
    }

    static {
        Primitive[] var0 = Primitive.values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            Primitive var3 = var0[var2];
            primitiveTypes.put(var3.type, var3);
            signatures.put(var3.signature, var3);
        }

    }

    public static enum Primitive {
        BOOLEAN("boolean", 'Z', 0),
        BYTE("byte", 'B', 1),
        CHAR("char", 'C', 2),
        SHORT("short", 'S', 3),
        INT("int", 'I', 4),
        LONG("long", 'J', 5),
        FLOAT("float", 'F', 6),
        DOUBLE("double", 'D', 7),
        OBJECT("object", 'O', 8),
        ;

        String type;
        char signature;
        int order;

        private Primitive(String var3, char var4, int var5) {
            this.type = var3;
            this.signature = var4;
            this.order = var5;
        }
    }
}