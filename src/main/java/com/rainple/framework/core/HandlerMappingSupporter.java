package com.rainple.framework.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rainple.framework.Constant.ConfigEnum;
import com.rainple.framework.annotation.Controller;
import com.rainple.framework.annotation.RequestMapping;
import com.rainple.framework.annotation.ResponseBody;
import com.rainple.framework.core.filter.HandlerChain;
import com.rainple.framework.core.filter.HandlerInterceptorSupporter;
import com.rainple.framework.modal.ModalAndView;
import com.rainple.framework.modal.TemplateEngine;
import com.rainple.framework.utils.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 处理请求的类
 * @author: rainple
 * @create: 2019-07-12 17:12
 **/
public class HandlerMappingSupporter {

    private BeanFactory beanFactory = BeanFactory.getBeanFactory();
    private static Logger logger = Logger.getLogger(HandlerMappingSupporter.class);
    private static Map<String,RequestMappingHandler> handlerMapping = new HashMap<>();
    private Map<Method, HandlerChain> filterMap;

    public HandlerMappingSupporter(Map<Method,HandlerChain> filterMap) {
        this.filterMap = filterMap;
    }

    /**
     * 将action key 与 controller 的方法解析成一一对应关系
     */
    public void parse() {
        Map<String, Object> ioc = beanFactory.getBeans();
        if (ioc.isEmpty())
            return;
        for (Map.Entry<String,Object> entry : ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(Controller.class))
                continue;
            if (clazz.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                String baseUrl = requestMapping.value().trim();
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(RequestMapping.class))
                        continue;
                    RequestMapping req = method.getAnnotation(RequestMapping.class);
                    String url = req.value().trim();
                    //处理restful风格的url
                    String[] pvs = null;
                    if (url.contains("{")){
                        int of = url.indexOf("{");
                        String pv = url.substring(of);
                        //获取到占位符的名字
                        pvs = parsePathVariable(pv);
                    }
                    String path = (baseUrl + url).replaceAll("/+","/");
                    if (handlerMapping.containsKey(path)) {
                        logger.error(path + "已存在",new RuntimeException());
                        System.exit(0);
                    }
                    //将映射关系封装成requestMappingHandler内部类
                    Object proxyBean = beanFactory.getProxyBean(clazz.getName());
                    RequestMappingHandler mappingHandler;

                    if (proxyBean != null) {
                        Method method1 = null;
                        for (Method proxyMethod : proxyBean.getClass().getDeclaredMethods()) {
                            if(proxyMethod.getName().equals(method.getName())){
                                method1 = proxyMethod;
                                break;
                            }
                        }
                        mappingHandler = new RequestMappingHandler(proxyBean, method1, pvs,clazz,method);
                    }else {
                        mappingHandler = new RequestMappingHandler(entry.getValue(), method, pvs,clazz,method);
                    }
                    handlerMapping.put(path, mappingHandler);
                    logger.info("完成映射：[\""+ path + "\"<=>" + method +"]");
                }
            }
        }
    }

    /**
     * 获取占位符的名字 、{name} ---> name
     * @param pv controller中配置的url
     * @return 占位符名字数组
     */
    private String[] parsePathVariable(String pv){
        String all = pv.replaceAll("\\{\\}"," ");
        return all.split(" ");
    }

    public void execute(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        //统一格式，方便后边的处理
        uri = uri.replace(contextPath,"");
        RequestMappingHandler mappingHandler;
        mappingHandler = handlerMapping.get(uri);
        Map<String, String> locationMap = null;
        if (mappingHandler == null) {
            //解析前端传进来的url，如果有通配符则进行封装，找到handlerMapping中对应的类
            Map findMap = findUrlFromHandlerMapping(uri);
            //uri中参数位置的映射
            locationMap = (Map<String, String>) findMap.get("pv");
            //这里得到的是handlerMapping中的uri
            String findUri = (String) findMap.get("uri");
            mappingHandler = handlerMapping.get(findUri);
        }
        if (mappingHandler != null){
            Object instance = mappingHandler.getInstance();
            Method method = mappingHandler.getMethod();
            int parameterCount = method.getParameterCount();
            Object[] params = null;
            if (parameterCount > 0) {
                //对请求方法路径进行映射，封装
                params = mappingHandler.parseParams(req,resp, locationMap);
            }
            try {
                HandlerInterceptorSupporter handlerInterceptorSupportor = new HandlerInterceptorSupporter(req, resp);
                boolean preHandle = handlerInterceptorSupportor.preHandle();
                if (!preHandle) return;
                Object result = invokeChain(instance,method,params,handlerInterceptorSupportor);
                handlerInterceptorSupportor.afterHandle();
                //controller类中添加了responseBody注解，直接返回字符串，如果是对象则转成json格式
                if (instance.getClass().isAnnotationPresent(ResponseBody.class)
                        || method.isAnnotationPresent(ResponseBody.class)) {
                    Object json = JSON.toJSON(result);
                    String s = JSON.toJSONString(json, SerializerFeature.WriteNullNumberAsZero);
                    resp.getWriter().write(s);
                } else {
                    if (result == null)
                        throw new RuntimeException("转发页面不能为空");
                    //返回路径前缀
                    String prefix = ApplicationConfig.applicationConfig.getProperty(ConfigEnum.DISPATCHER_PREFIX.getName());
                    //返回页面的文件类型，没有配置的话默认是jsp
                    String subfix = ApplicationConfig.applicationConfig.getProperty(ConfigEnum.DISPATCHER_SUBFIX.getName());
                    if (StringUtils.isEmpty(subfix))
                        subfix = ".jsp";
                    if (StringUtils.isEmpty(prefix))
                        throw new RuntimeException("请求路径配置错误");
                    String root = "";
                    if (result instanceof String) {
                        root = (String) result;
                    }else if (result instanceof ModalAndView) {
                        TemplateEngine templateEngine = new TemplateEngine((ModalAndView) result);
                        String page = templateEngine.page();
                        resp.setContentType("text/html;charset=utf-8");
                        resp.getWriter().print(page);
                        return;
                    }
                    if (root.startsWith("redirect:")) {
                        root = root.replace("redirect:", "");
                        String path = (contextPath + "/" + prefix + "/" + root + subfix).replaceAll("/+", "/");
                        logger.info("转发路径 : " + path);
                        resp.sendRedirect(path);
                    } else {
                        String path = (contextPath + "/" + prefix + "/" + root + subfix).replaceAll("/+", "/");
                        logger.info("请求路径 : " + path);
                        req.getRequestDispatcher(path).forward(req, resp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Object invokeChain(Object instance, Method method, Object[] params, HandlerInterceptorSupporter handlerInterceptorSupporter) throws InvocationTargetException, IllegalAccessException {
        HandlerChain handlerChain = filterMap.get(method);
        handlerInterceptorSupporter.postHandle();
        if (handlerChain != null)
            return handlerChain.handle(instance,method,params);
        else
            return method.invoke(instance,params);
    }

    /**
     * 通过前端传进来的uri查找controller中配置的url，有可能存在占位符
     * @param uri 前端发送的请求
     * @return map集合，{key为uri的是controller中配置的url，key为pv的则为map集合，里面封装的是前端传进来的值与controller中占位符的对应关系}
     */
    private Map findUrlFromHandlerMapping(String uri){
        Map<String,Object> map = new HashMap<>();
        for (String key : handlerMapping.keySet()) {
            if (uri.equals(key)) {
                map.put("uri", uri);
                return map;
            }
            String[] split = uri.split("/");//前端url
            String[] split1 = key.split("/");//controller url
            String s1 = ""; //临时保存前端截取后的url
            String s2 = "";//临时保存controller 截取后的url
            Map<String,String> location = new HashMap<>();//用于保存uri中带有参数的位置映射
            int count = 0;//用于计算uri与handlerMapping中相同的数量
            if (split.length == split1.length){
                for (int i = 0 ; i < split.length ; i++) {
                    s1 = split[i];
                    s2 = split1[i];
                    if (s1.equals(s2)) {
                        count++;
                    }else if (s2.contains("{")){//带有参数的位置
                        String k = s2.substring(1, s2.length() - 1);
                        location.put(k,s1);
                        count++;
                    }
                }
            }
            //前端请求url与controller匹配上
            if (count == split.length){
                map.put("uri",key);
                map.put("pv",location);
                return map;
            }
        }
        //找不到匹配的就返回原来的uri
        map.put("uri",uri);
        return map;
    }

}
