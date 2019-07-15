package com.rainple.framework.utils;



import com.rainple.framework.aop.MethodHandler;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ClassUtils {

    public static String[] parameterNameDiscovery(String clazz,Method method){
        try {
            //获取要操作的类对象
            ClassPool pool = ClassPool.getDefault();
            ClassClassPath classPath = new ClassClassPath(ClassUtils.class);
            pool.insertClassPath(classPath);
            CtClass ctClass = pool.get(clazz);

            //获取要操作的方法参数类型数组，为获取该方法代表的CtMethod做准备
            int count = method.getParameterCount();
            Class<?>[] paramTypes = method.getParameterTypes();
            CtClass[] ctParams = new CtClass[count];
            for (int i = 0; i < count; i++) {
                ctParams[i] = pool.getCtClass(paramTypes[i].getName());
            }

            CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName(), ctParams);
            //得到该方法信息类
            javassist.bytecode.MethodInfo methodInfo = ctMethod.getMethodInfo();

            //获取属性变量相关
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();

            //获取方法本地变量信息，包括方法声明和方法体内的变量
            //需注意，若方法为非静态方法，则第一个变量名为this
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            int pos = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
            String[] params = new String[count];
            for (int i = 0; i < count; i++) {
                String s = attr.variableName(i + pos);
                params[i] = s;
            }
            return params;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String lowerFirstCase(String str){
        if(!Character.isLetter(str.charAt(0)))
            return str;
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public static Class forName(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String uperFirstCase(String str){
        return String.valueOf(str.charAt(0)).toUpperCase() + str.substring(1,str.length());
    }

    public static boolean isBaseType(Class claszz){
        String name = claszz.getSimpleName();
        switch (name){
            case "String" : return true;
            case "Short" : return true;
            case "Integer" : return true;
            case "Byte" : return true;
            case "Long" : return true;
            case "Float" : return true;
            case "Double" : return true;
            case "Boolean" : return true;
            case "Character" : return true;
            default: return false;
        }
    }

    public static void setField(Object instance,Field field,Object value) {
        field.setAccessible(true);
        try {
            field.set(instance,value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setField(Object instance, Field field, String value){
        field.setAccessible(true);
        try {
            String name = field.getType().getName();
            if (name.equals("java.lang.Integer")){
               field.set(instance,Integer.valueOf(value));
            }else if (name.equals("java.lang.Boolean")){
                field.set(instance,Boolean.valueOf(value));
            }else if (name.equals("java.lang.Character")){
                field.set(instance,name.charAt(0));
            }else if (name.equals("java.lang.Short")){
                field.set(instance,Short.valueOf(name));
            }else if (name.equals("java.lang.Double")){
                field.set(instance,Double.valueOf(name));
            }else if (name.equals("java.lang.Float")){
                field.set(instance,Float.valueOf(name));
            }else if (name.equals("java.lang.String")){
                field.set(instance,value);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setSetmethod(Object instance,Method method,String value){
        String name = method.getParameterTypes()[0].getName();
        try {
            if (name.equals("java.lang.Integer")){
                method.invoke(instance,Integer.valueOf(value));
            }else if (name.equals("java.lang.Boolean")){
                method.invoke(instance,Boolean.valueOf(value));
            }else if (name.equals("java.lang.Character")){
                method.invoke(instance,name.charAt(0));
            }else if (name.equals("java.lang.Short")){
                method.invoke(instance,Short.valueOf(name));
            }else if (name.equals("java.lang.Double")){
                method.invoke(instance,Double.valueOf(name));
            }else if (name.equals("java.lang.Float")){
                method.invoke(instance,Float.valueOf(name));
            }else if (name.equals("java.lang.String")){
                method.invoke(instance,value);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Object newInstance(String className){
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newInstance(Class clazz){
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Set<Object> getAllClassByInterface(Class clazz){
        Reflections reflections = new Reflections(clazz.getPackage().getName());
        Set subTypesOf = reflections.getSubTypesOf(clazz);
        return subTypesOf;
    }

    public static void getClasses(String path,List<String> fileList){
        String path1 = ClassUtils.class.getClassLoader().getResource("").getPath();
        String s = path1 + path.replaceAll("\\.", "/");
        try {
            File dir = new File(s);
            for (File file : dir.listFiles()) {
                if (file.isDirectory()){
                    getClasses(path + "." + file.getName(),fileList);
                }else {
                    String clazz = path + "." + file.getName().replace(".class", "");
                    fileList.add(clazz);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Class> isAssignableFrom(Class clazz) {
        String path = clazz.getPackage().getName();
        List<String> cls = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        getClasses(path,cls);
        for (String cl : cls) {
            Class aClass = forName(cl);
            assert aClass != null;
            if (aClass.isInterface()) continue;
            classes.add(aClass);
        }
        return classes;
    }

    public static <T> List<T> getChildFromSuperToInstance(Class clazz){
        String path = clazz.getPackage().getName();
        List<T> classes = new ArrayList<>();
        List<String> cls = new ArrayList<>();
        getClasses(path,cls);
        for (String className : cls) {
            if (StringUtils.isEmpty(className))
                continue;
            try {
                Class<?> forName = Class.forName(className);
                if (forName.isInterface())
                    continue;
                if (Modifier.isAbstract(forName.getModifiers()))
                    continue;
                if (clazz.isAssignableFrom(forName)) {
                    classes.add((T) newInstance(className));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }
    public static boolean isProxyClass(Class clazz){
        if (clazz.getSimpleName().contains("$$EnhancerByCGLIB$$")){
            return true;
        }
        return false;
    }

    public static String getClassNameWithoutCglibMarkFromProxy(Class clazz){
        if (isProxyClass(clazz))
            return clazz.getName().substring(0,clazz.getName().indexOf("$$EnhancerByCGLIB$$"));
        return clazz.getName();
    }

    public static void main(String[] args){
        List<Object> childFromSuper = ClassUtils.getChildFromSuperToInstance(MethodHandler.class);
        System.out.println(childFromSuper);
    }
}
