package cn.ac.iscas.util.struct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StructFieldData {
    private Field field;
    private boolean requiresGetterSetter = false;
    private Method getter;
    private Method setter;
    private boolean arrayLengthMarker = false;
    private Field lengthField;
    private Constants.Primitive type;

    public StructFieldData(Field var1, boolean var2, Method var3, Method var4, Constants.Primitive var5, boolean var6, Field var7) {
        this.field = var1;
        this.requiresGetterSetter = var2;
        this.getter = var3;
        this.setter = var4;
        this.type = var5;
    }

    public StructFieldData(Field var1) {
        this.field = var1;
    }

    public Field getField() {
        return this.field;
    }

    public void setField(Field var1) {
        this.field = var1;
    }

    public boolean requiresGetterSetter() {
        return this.requiresGetterSetter;
    }

    public void setRequiresGetterSetter(boolean var1) {
        this.requiresGetterSetter = var1;
    }

    public Method getGetter() {
        return this.getter;
    }

    public void setGetter(Method var1) {
        this.getter = var1;
    }

    public Method getSetter() {
        return this.setter;
    }

    public void setSetter(Method var1) {
        this.setter = var1;
    }

    public boolean isArrayLengthMarker() {
        return this.arrayLengthMarker;
    }

    public void setArrayLengthMarker(boolean var1) {
        this.arrayLengthMarker = var1;
    }

    public Constants.Primitive getType() {
        return this.type;
    }

    public void setType(Constants.Primitive var1) {
        this.type = var1;
    }

    public boolean isRequiresGetterSetter() {
        return this.requiresGetterSetter;
    }

    public Field getLengthField() {
        return this.lengthField;
    }

    public void setLengthField(Field var1) {
        this.lengthField = var1;
    }
}
