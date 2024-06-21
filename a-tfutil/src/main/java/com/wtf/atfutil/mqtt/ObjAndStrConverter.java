package cn.ac.iscas.util;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ObjAndStrConverter {


    public static <T> T str2Obj(String inputString, Class<T> targetClass)
            throws IllegalAccessException, ParseException {
        T obj;
        try {
            obj = targetClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to create an instance of the target class.", e);
        }

        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ValueLocation.class)) {
                ValueLocation annotation = field.getAnnotation(ValueLocation.class);
                field.setAccessible(true);

                if (field.getType() == String.class) {
                    field.set(obj, processStringValue(inputString, annotation));
                } else if (field.getType() == Date.class) {
                    field.set(obj, processDateValue(inputString, annotation));
                }else if (field.getType() == Integer.class) {
                    field.set(obj, processIntegerValue(inputString, annotation));
                }else if (field.getType() == Long.class) {
                    field.set(obj, processLongValue(inputString, annotation));
                }else if (field.getType() == Double.class) {
                    field.set(obj, processDoubleValue(inputString, annotation));
                }else if (field.getType() == Float.class) {
                    field.set(obj, processFloatValue(inputString, annotation));
                }else if (field.getType() == Byte.class) {
                    field.set(obj, processByteValue(inputString, annotation));
                }

                // 其他类型的属性处理...
            }
        }

        return obj;
    }

    private static Object processStringValue(String value, ValueLocation annotation) {
        return value.substring(annotation.begin() - 1, annotation.end());
    }

    private static Object processLongValue(String value, ValueLocation annotation) {
        return Long.parseLong(value.substring(annotation.begin() - 1, annotation.end()));
    }

    private static Object processDoubleValue(String value, ValueLocation annotation) {
        return Double.parseDouble(value.substring(annotation.begin() - 1, annotation.end()));
    }

    private static Object processIntegerValue(String value, ValueLocation annotation) {
        return Integer.parseInt(value.substring(annotation.begin() - 1, annotation.end()));
    }

    private static Object processByteValue(String value, ValueLocation annotation) {
        return Byte.parseByte(value.substring(annotation.begin() - 1, annotation.end()));
    }

    private static Object processFloatValue(String value, ValueLocation annotation) {
        return Float.parseFloat(value.substring(annotation.begin() - 1, annotation.end()));
    }

    private static Date processDateValue(String value, ValueLocation annotation) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(annotation.format());
        dateFormat.setTimeZone(TimeZone.getTimeZone(annotation.timezone()));
        return dateFormat.parse(value.substring(annotation.begin() - 1, annotation.end()));
    }

    public static String obj2Str(Object obj) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        int maxEndPosition = getMaxEndPosition(fields);

        // 遍历可能的位置
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= maxEndPosition; i++) {
            // 遍历字段
            boolean filled = false;
            for (Field field : fields) {
                if (field.isAnnotationPresent(ValueLocation.class)) {
                    ValueLocation annotation = field.getAnnotation(ValueLocation.class);
                    field.setAccessible(true);

                    // 判断位置是否在字段的范围内
                    if (i >= annotation.begin() && i <= annotation.end()) {
                        // 字段的开始位置和结束位置之间
                        if (field.getType() == String.class) {
                            String value = (String) field.get(obj);
                            if (i - annotation.begin() < value.length()) {
                                stringBuilder.append(value.charAt(i - annotation.begin()));
                            } else {
                                stringBuilder.append(" ");
                            }
                            filled = true;
                        } else if (field.getType() == Date.class) {
                            Date dateValue = (Date) field.get(obj);
                            String dateString = processDateToString(dateValue, annotation);
                            if (i - annotation.begin() < dateString.length()) {
                                stringBuilder.append(dateString.charAt(i - annotation.begin()));
                            } else {
                                stringBuilder.append(" ");
                            }
                            filled = true;
                        }
                    }
                }
            }

            // 如果没有在字段的范围内找到值，填充空格
            if (!filled) {
                stringBuilder.append(" ");
            }
        }

        return stringBuilder.toString();
    }

    private static int getMaxEndPosition(Field[] fields) {
        int maxEndPosition = 0;

        // 获取最大的结束位置
        for (Field field : fields) {
            if (field.isAnnotationPresent(ValueLocation.class)) {
                ValueLocation annotation = field.getAnnotation(ValueLocation.class);
                if (annotation.end() > maxEndPosition) {
                    maxEndPosition = annotation.end();
                }
            }
        }

        return maxEndPosition;
    }

    private static String processDateToString(Date value, ValueLocation annotation) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(annotation.format());
        dateFormat.setTimeZone(TimeZone.getTimeZone(annotation.timezone()));
        return dateFormat.format(value);
    }
}