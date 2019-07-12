package com.rainple.framework.aop;/**
 * @Auther: Administrator
 * @Date: 2018/9/24 15:49
 * @PROJECT_NAME webapp
 * @Description:
 */

import com.rainple.framework.aop.advice.Advice;
import com.rainple.framework.utils.ClassUtils;

import java.io.File;
import java.util.*;

/**
 * @program: webapp
 *
 * @description:
 *
 * @author: Mr.rainple
 *
 * @create: 2018-09-24 15:49
 **/
public class AspectUtils {

    /**
     * 解析增强方法
     * @param str
     * @return
     */
    public static Advice parseAdviceMethod(String str){
        Advice advice = new Advice();
        if (str.startsWith("*")){
            String fullMethodName = str.substring(2,str.indexOf("("));
            int of = fullMethodName.lastIndexOf(".");
            String className = fullMethodName.substring(0,of);
            String pck = className.substring(0,className.indexOf("."));
            String methodName = fullMethodName.substring(of+1,fullMethodName.length());
            String classSimpleName = className.substring(className.lastIndexOf(".")+1,className.length());
            advice.setClassName(className);
            advice.setClassSimpleName(ClassUtils.lowerFirstCase(classSimpleName));
            advice.setMethodName(fullMethodName);
            advice.setMethodSimpleName(methodName);
        }
        return advice;
    }

    public static void scanPackage(Set<String> pck,String str,List<String> list){
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
                        String s1 = ClassUtils.lowerFirstCase(file1.getName().replace(".class",""));
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
                    String s1 = ClassUtils.lowerFirstCase(file1.getName().replace(".class",""));
                    list.add(s1);
                }
            }
        }
    }

    public static void getDirs(String basePath,Set<String> dirs){
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

    public static void main(String[] args){
        String basePath = AspectUtils.class.getClassLoader().getResource("").getPath();
        String pck = "* com.rainple.framework.core.impl.*.*(..)";
    }

}
