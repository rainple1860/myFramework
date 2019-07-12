package com.rainple.framework.core;

import com.rainple.framework.annotation.PathVariable;
import com.rainple.framework.annotation.RequestParam;
import com.rainple.framework.utils.ClassUtils;
import com.rainple.framework.utils.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @description: 封装了请求于controller中的方法
 * @author: rainple
 * @create: 2019-07-12 17:17
 **/
public class RequestMappingHandler {

    private static Logger logger = Logger.getLogger(RequestMappingHandler.class);

    //实例对象
    Object instance;
    //映射方法
    public Method method;
    //用于保存restful url中的参数名，如：、rainple/user/{name}/{age},pathVariables保存的是 name,age
    String[] pathVariables;

    Class originClass;
    Method originMethod;


    public RequestMappingHandler(Object instance,Method method,String[] pathVariables,Class originClass,Method originMethod){
        this.instance = instance;
        this.method = method;
        this.pathVariables = pathVariables;
        this.originClass = originClass;
        this.originMethod = originMethod;
    }

    /**
     * 解析前端发来的请求参数，并赋值
     * @param request 获取参数值
     * @param locationMap 通配符对应的键值对：/get/{id} ---> /get/1  ---> locationMap[id=1]
     * @return 返回方法的参数列表
     */
    public Object[] parseParams(HttpServletRequest request, HttpServletResponse resp, Map<String,String> locationMap){
        List<Object> list = new ArrayList<>();
        //请求参数键值对
        Map<String,String[]> reqMap = request.getParameterMap();
        //方法参数类型
        Class[] parameterTypes = method.getParameterTypes();
        //参数数量
        int count = method.getParameterCount();
        //每个方法参数的注解
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        String[] paramNames = null;
        if (count > 0 && locationMap == null) {
            //获取参数名,通过javassist字节码获取
            paramNames = ClassUtils.parameterNameDiscovery(this.originClass.getName(), originMethod);
        }
        for (int i = 0 ;i < count ; i++) {
            String valName = "";//参数名
            //请求参数值
            String reqVal = "";
            boolean isPathVariableAnno = false;//用于判断是否是restful风格
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation != null){
                    if (annotation.annotationType().getName().equals(RequestParam.class.getName()))
                        valName = ((RequestParam) parameterAnnotations[i][0]).value().trim();
                    else if (annotation.annotationType().getName().equals(PathVariable.class.getName())){
                        String pv = ((PathVariable) parameterAnnotations[i][0]).value().trim();
                        //获取占位符对应的值
                        reqVal = locationMap.get(pv);
                        if (reqVal == null) {
                            logger.error("无法从请求参数中获取值，请检查：" + method.getName(),new RuntimeException());
                        }
                        isPathVariableAnno = true;
                    }

                }
            }
            if (ClassUtils.isBaseType(parameterTypes[i])){//
                if (!isPathVariableAnno){//目标方法中的注解不是PathVariable
                    if ("".equals(valName))
                        valName = paramNames[i];//从controller的方法中获取到的参数名
                    String[] strings = reqMap.get(valName);//获取参数值
                    reqVal = strings[0];
                }
                /**
                 * 对反射调用方法的目标方法的参数进行封装
                 */
                if (parameterTypes[i].getName().equals("java.lang.String")) {
                    list.add(reqVal);
                } else if (parameterTypes[i].getName().equals("java.lang.Integer")) {
                    list.add(Integer.valueOf(reqVal));
                }
            }else {//方法参数是引用类型
                Object ins = setValueToReferenceType(request,resp,reqMap, parameterTypes[i]);
                list.add(ins);
            }
        }
        return list.toArray();
    }

    /**
     * 封装引用类型
     * @param reqMap 请求键值对
     * @param arg 方法参数
     * @return
     */
    private Object setValueToReferenceType(HttpServletRequest request,HttpServletResponse response,Map<String, String[]> reqMap, Class arg) {
        if (arg == HttpServletResponse.class)
            return response;
        if (arg == HttpServletRequest.class)
            return request;
        if (HttpSession.class == arg)
            return request.getSession();
        if (arg.getClass().isArray()) {
            Object[] objs = new Object[reqMap.keySet().size()];
            int i = 0;
            for (Map.Entry<String, String[]> stringEntry : reqMap.entrySet()) {
                objs[i++] = stringEntry.getValue();
            }
            return objs;
        }
        if (arg == Map.class){
            Map<String,Object> map = new HashMap<>();
            for (String key : reqMap.keySet()) {
                map.put(key,reqMap.get(key)[0]);
            }
            return map;
        }
        if (arg == Set.class){
            Set<Object> set = new HashSet<>();
            for (Map.Entry<String, String[]> stringEntry : reqMap.entrySet()) {
                set.add(stringEntry.getValue()[0]);
            }
            return set;
        }
        if (arg == List.class){
            List<Object> list = new ArrayList<>();
            for (Map.Entry<String, String[]> stringEntry : reqMap.entrySet()) {
                list.add(stringEntry.getValue()[0]);
            }
            return list;
        }
        /**
         * 参数是bean类型
         */
        Object newInstance = ClassUtils.newInstance(arg);
        for (Field field : arg.getDeclaredFields()) {
            String fieldName = field.getName();
            String[] strings = reqMap.get(fieldName);
            String v = "";
            //赋默认值
            if (strings == null || StringUtils.isEmpty(strings[0])){
                if (Number.class.isAssignableFrom(field.getType()))
                    v = "0";
                else if (field.getType() == Boolean.class)
                    v = "false";
            }else {
                v = strings[0];//请求参数值
            }
            ClassUtils.setField(newInstance,field,v);
        }
        return newInstance;
    }

    /**
     * 寻找前端传进来的url和controller类中的requestMapping配置的占位符相匹配的后台url
     * @param pv 前端传进来的uri
     * @return 返回handlerMapping中的url，即在controller中配置的requestMapping
     */
    private String findPathVariable(String pv) {
        if (pathVariables == null || StringUtils.isEmpty(pv))
            throw new RuntimeException("请求参数解析错误，请检查：" + method);
        for (String pathVariable : pathVariables) {
            if (pathVariable.equals(pv))
                return pathVariable;
        }
        return null;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "requestMappingHandler{" +
                "instance=" + instance +
                ", method=" + method +
                '}';
    }
}
