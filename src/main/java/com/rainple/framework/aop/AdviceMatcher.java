package com.rainple.framework.aop;

import com.rainple.framework.core.BeanFactory;
import com.rainple.framework.utils.ClassUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-28 11:20
 **/
public class AdviceMatcher {

    public List<Advice> match(String expr){
        String basePath = AspectUtils.class.getClassLoader().getResource("").getPath();
        List<String> classNames = new ArrayList<>();
        Set<String> set = new TreeSet<>();
        getDirs(basePath,set);
        String prev = expr.substring(0,expr.indexOf("("));
        int of = prev.lastIndexOf(".");
        String methodName = prev.substring(of+1,prev.length());
        String classPrev = expr.substring(0,of);
        int of1 = classPrev.lastIndexOf(".");
        String className = classPrev.substring(of1+1,classPrev.length());
        String pckPrev = expr.substring(0,of1);
        String pck = pckPrev.substring(pckPrev.lastIndexOf(" ")+1,pckPrev.length());
        String p = expr.substring(expr.indexOf("(")+1,expr.indexOf(")"));
        scanPackage(set,pck,classNames);
        List<String> cls = getClassNames(className,classNames);
        List<String> methodNames = getMethodNames(methodName, cls);
        Class[] objects = parseParams(p);
        List<Advice> prams = getParams(methodNames,cls,objects);
        return prams;
    }

    private Class[] parseParams(String p) {
        List<Class> list = new ArrayList<>();
        if ("..".equals(p)){
            return null;
        }
        if (p.contains(",")){
            String[] split = p.split(",");
            for (String s : split)
                try {
                    Class<?> aClass = Class.forName(s);
                    list.add(aClass);
                } catch (Exception e) {
                    new RuntimeException("增强方法参数输入有误，" + s, e);
                }
        }else {
            try {
                Class<?> aClass = Class.forName(p);
                list.add(aClass);
            } catch (ClassNotFoundException e) {
                new RuntimeException("增强方法参数输入有误，" + p, e);
            }
        }
        return (Class[]) list.toArray();
    }

    private List<Advice> getParams(List<String> methodNames, List<String> cls, Class[] objects) {
        List<Advice> adviceList = new ArrayList<>();
        for (String cl : cls) {
            String simpleName = ClassUtils.lowerFirstCase(cl);
            Object bean = BeanFactory.getBeanFactory().getBean(simpleName);
            if (bean == null)
                continue;
            for (Method method : bean.getClass().getMethods()) {
                String name = method.getName();
                for (String methodName : methodNames) {
                    if (methodName.equals(name)){
                        Advice advice = new Advice();
                        advice.setMethodSimpleName(name);
                        advice.setClassSimpleName(simpleName);
                        advice.setClassName(bean.getClass().getName());
                        if (objects == null){
                            advice.setArgs(method.getParameterTypes());
                        }else {
                            try {
                                Method method1 = bean.getClass().getMethod(methodName, objects);
                                advice.setArgs(method1.getParameterTypes());
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
                        adviceList.add(advice);
                    }
                }
            }
        }
        return adviceList;
    }

    private List<String> getMethodNames(String methodName,List<String> cls) {
        BeanFactory beanFactory = BeanFactory.getBeanFactory();
        List<String> methodNames = new ArrayList<>();
        for (String cl : cls) {
            String simpleName = ClassUtils.lowerFirstCase(cl);
            Object bean = beanFactory.getBean(simpleName);
            if (bean == null)
                continue;
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if ("*".equals(methodName)){
                    if (isObjectMethod(method.getName(),method.getParameterTypes())) continue;
                    methodNames.add(method.getName());
                }else {
                    if (methodName.equals(method.getName())){
                        methodNames.add(method.getName());
                        break;
                    }
                }
            }
        }
        return methodNames;
    }

    private boolean isObjectMethod(String name, Class<?>[] parameterTypes) {
        try {
            return null == Object.class.getMethod(name,parameterTypes);
        } catch (NoSuchMethodException e) { }
        return false;
    }

    private List<String> getClassNames(String name,List<String> classNames) {
        if (name.equals("*"))
            return classNames;
        List<String> list = new ArrayList<>();
        for (String className : classNames) {
            if (className.equals(name))
                list.add(className);
        }
        return list;
    }

    private static void scanPackage(Set<String> pck,String str,List<String> list){
        if (str.contains("*")){
            Set<String> set = new TreeSet<>();
            if (str.startsWith("*")){
                String right = str.substring(1,str.length());
                for (String s : pck) {
                    if (s.startsWith(right)){
                        set.add(s);
                    }
                }
                scanPackage(set,"",list);
            }else if (str.endsWith("*")){
                String left = str.substring(0,str.length()-1);
                for (String s : pck) {
                    if (s.startsWith(left)){
                        set.add(s);
                    }
                }
                scanPackage(set,"",list);
            }else {
                String left = str.substring(0,str.indexOf("*"));
                String right = str.substring(str.indexOf("*")+2,str.length());
                List<String> list1 = new ArrayList<>();
                for (String s : pck) {
                    if (s.startsWith(left)){
                        String replace = s.replace(left, "");
                        if (!replace.contains("."))
                            continue;
                        String substring = replace.substring(replace.indexOf(".") + 1, replace.length());
                        if (substring.startsWith(right)){
                            list1.add(s);
                        }
                    }
                }
                if (list1.isEmpty())
                    return;
                String base = AspectUtils.class.getClassLoader().getResource("").getPath();
                for (String s : list1) {
                    String path = base + s.replaceAll("\\.","/");
                    File file = new File(path);
                    for (File file1 : file.listFiles()) {
                        if (file1.isDirectory())
                            continue;
                        String s1 = file1.getName().replace(".class","");
                        list.add(s1);
                    }
                }
            }
        }else {
            List<String> l1 = new ArrayList<>();
            for (String s : pck) {
                if ("".equals(str)){
                    l1.add(s);
                }else {
                    if (s.startsWith(str)) {
                        l1.add(s);
                    }
                }
            }
            String base = AspectUtils.class.getClassLoader().getResource("").getPath();
            for (String s : l1) {
                String path = base + s.replaceAll("\\.","/");
                File file = new File(path);
                for (File file1 : file.listFiles()) {
                    if (file1.isDirectory())
                        continue;
                    String s1 = file1.getName().replace(".class","");
                    list.add(s1);
                }
            }
        }
    }
    private static void getDirs(String basePath,Set<String> dirs){
        File dir = new File(basePath);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()){
                String path = (basePath + "/" + file.getName()).replaceAll("/+","/");
                getDirs(path,dirs);
            }else {
                String path = file.getParentFile().getPath();
                String resource = AspectUtils.class.getClassLoader().getResource("").getFile();
                String pck = path.replace(new File(resource).getPath(),"");
                if (pck.length() > 0) {
                    String s = (pck.substring(1, pck.length())).replaceAll("\\\\", "\\.");
                    dirs.add(s);
                }
            }
        }
    }
}
