package com.rainple.framework.utils;

import java.lang.reflect.Field;
import java.util.*;

public class ListUtil {

    public static final String DESC = "desc";
    public static final String ASC = "asc";

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> List<T> sort(List<T> list, final String field, final String sort,int start,int size) {
        Collections.sort(list, new Comparator() {
            int ret = 0;

            @Override
            public int compare(Object a, Object b) {
                try {
                    Field f = a.getClass().getDeclaredField(field);
                    f.setAccessible(true);
                    Class type = f.getType();
                    if (type == Integer.class) {
                        this.ret = ((Integer) f.get(a)).compareTo((Integer) f.get(b));
                    } else if (type == Double.class) {
                        this.ret = ((Double)f.get(a)).compareTo(f.getDouble(b));
                    } else if (type == Long.class) {
                        this.ret = ((Long)f.get(a)).compareTo(f.getLong(b));
                    } else if (type == Float.class) {
                        this.ret = ((Float)f.get(a)).compareTo(f.getFloat(b));
                    } else if (type == Date.class) {
                        this.ret = ((Date) f.get(a)).compareTo((Date) f.get(b));
                    } else if (isImplementsOf(type, Comparable.class)) {
                        this.ret = ((Comparable) f.get(a)).compareTo(f.get(b));
                    } else {
                        this.ret = String.valueOf(f.get(a)).compareTo(String.valueOf(f.get(b)));
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return sort != null && sort.toLowerCase().equals("desc") ? -this.ret : this.ret;
            }
        });
        return limit(list,start,size);
    }

    private static <T> List<T> limit(List<T> list,int start ,int size){
        List<T> list1 = new ArrayList();
        if (list.size() < (size+start) && list.size() > start){
            size = list.size()-start;
        }
        if (start > list.size()){
            return null;
        }
        for (int i = start; i < size+start; i++) {
            list1.add(list.get(i));
        }
        return list1;
    }

    private static boolean isImplementsOf(Class<?> clazz, Class<?> interfaces) {
        boolean flag = false;
        Class[] itfs = clazz.getInterfaces();
        Class[] var7 = itfs;
        int var6 = itfs.length;

        for(int var5 = 0; var5 < var6; ++var5) {
            Class<?> class1 = var7[var5];
            if (class1 == interfaces) {
                flag = true;
            } else {
                flag = isImplementsOf(class1, interfaces);
            }
        }

        if (!flag && clazz.getSuperclass() != null) {
            return isImplementsOf(clazz.getSuperclass(), interfaces);
        } else {
            return flag;
        }
    }
}
