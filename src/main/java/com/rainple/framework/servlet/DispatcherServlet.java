package com.rainple.framework.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.rainple.framework.Constant.ConfigEnum;
import com.rainple.framework.annotation.*;
import com.rainple.framework.annotation.Aspect;
import com.rainple.framework.annotation.aspect.After;
import com.rainple.framework.annotation.aspect.Before;
import com.rainple.framework.aop.*;
import com.rainple.framework.core.*;
import com.rainple.framework.utils.ClassUtils;
import com.rainple.framework.utils.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;


public class DispatcherServlet extends HttpServlet {

    private static Logger logger = Logger.getLogger(DispatcherServlet.class);

    List<String> beanNames = new ArrayList<>();

    BeanFactory beanFactory = BeanFactory.getBeanFactory();

    Map<String,requestMappingHandler> handlerMapping = new HashMap<>();

    /*存放未被代理的类*/
    private Map<String,Object> originalClass = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("gbk");
        resp.setCharacterEncoding("gbk");
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatcher(req,resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        logger.info("开始初始化容器");
        //加载配置文件
        doLoadConfig(config);
        //扫包
        doScanPack(ApplicationConfig.applicationConfig.getProperty(ConfigEnum.SCANPACKAGE.getName()));
        //初始化bean
        doInstanceBeans();
        doAspect();
        //注入
        DoIoc();
        //处理映射关系
        handlerMapping();
        logger.info("容器初始化完毕");
    }
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        //统一格式，方便后边的处理
        uri = uri.replace(contextPath,"");
        //解析前端传进来的url，如果有通配符则进行封装，找到handlerMapping中对应的类
        Map findMap = findUrlFromHandlerMapping(uri);
        //uri中参数位置的映射
        Map<String,String> locationMap = (Map<String, String>) findMap.get("pv");
        //这里得到的是handlerMapping中的uri
        String findUri = (String) findMap.get("uri");
        requestMappingHandler mappingHandler = handlerMapping.get(findUri);
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
                Object result = method.invoke(instance, params);
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
                    if (StringUtils.isEmpty(prefix) || StringUtils.isEmpty(subfix))
                        throw new RuntimeException("请求路径配置错误");
                    if (!(result instanceof String))
                        throw new RuntimeException("页面返回转发路径类型必须为String类型");
                    if (((String) result).startsWith("redirect:")){
                        result = ((String) result).replace("redirect:","");
                        String path = (contextPath + "/" + prefix + "/" + result + subfix).replaceAll("/+", "/");
                        logger.info("转发路径 : " + path);
                        resp.sendRedirect(path);
                        return;
                    }else {
                        String path = (contextPath + "/" + prefix + "/" + result + subfix).replaceAll("/+", "/");
                        logger.info("请求路径 : " + path);
                        req.getRequestDispatcher(path).forward(req,resp);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载配置文件
     * @param config
     */
    private void doLoadConfig(ServletConfig config) {
        String pack = config.getInitParameter("applicationContext");
        logger.info("开始加载配置文件：" + pack);
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(pack);
            logger.info("配置文件加载完成");
            ApplicationConfig.applicationConfig.load(new InputStreamReader(is,"utf-8"));
        } catch (IOException e) {
            logger.error("配置文件加载失败",e);
        }finally {
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 处理url和controller方法的映射关系
     */
    private void handlerMapping() {
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
                        String pv = url.substring(of,url.length());
                        pvs = parsePathVariable(pv);
                    }
                    String path = (baseUrl + url).replaceAll("/+","/");
                    //将映射关系封装成requestMappingHandler内部类
                    requestMappingHandler mappingHandler = new requestMappingHandler(entry.getValue(),method,pvs);
                    handlerMapping.put(path,mappingHandler);
                    logger.info("完成handlerMapping映射关系：" + path);
                }
            }
        }
    }

    /**
     * 依赖注入
     */
    private void DoIoc() {
        Map<String, Object> ioc = beanFactory.getBeans();
        if (ioc.isEmpty())
            return;
        for (Map.Entry<String,Object> entry : ioc.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String beanName = autowired.value().trim();
                    if ("".equals(beanName)){
                        beanName = field.getType().getName();
                    }
                    Object bean = ioc.get(beanName);
                    if (bean == null){
                        throw  new RuntimeException("can not found bean :" + beanName);
                    }
                    try {
                        field.set(entry.getValue(),bean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    /**
     * 实例化bean
     */
    private void doInstanceBeans() {
        logger.info("正在初始化Bean...");
        if (beanNames.isEmpty())
            return;
        List<String> beans = beanNames;
        List<BeanInstanceHandler> beanHandlers = ClassUtils.getChildFromSuper(BeanInstanceHandler.class);
        BeanInstanceHandlerChain chain = new BeanInstanceHandlerChain(beanHandlers,beans);
        chain.proceed();
        logger.info("初始化Bean完成....");
    }

    /**
     *解析增强方法
     */
    private void doAspect(){
        if (beanNames.isEmpty())
            return;
        for (String beanName : beanNames) {
            try {
                Class<?> clazz = Class.forName(beanName);
                if (clazz.isAnnotationPresent(Aspect.class) && clazz.isAnnotationPresent(Component.class)){
                    for (Method method : clazz.getMethods()) {
                        if (method.isAnnotationPresent(Before.class)){
                            Before before = method.getAnnotation(Before.class);
                            String expression = before.value().trim();//被增强方法的表达式
                            int order = before.order();//用于执行顺序
                            //解析增强方法的表达式
                            parseAdvice(clazz, method, expression,Before.class,order);
                        }else if (method.isAnnotationPresent(After.class)){
                            After after = method.getAnnotation(After.class);
                            String expression = after.value().trim();
                            int order = after.order();
                            parseAdvice(clazz,method,expression,After.class,order);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解析增强类
     * @param clazz 增强类
     * @param method 增强方法
     * @param expression 被增强的方法的表达式
     * @param adviceType 增强方式
     * @throws NoSuchMethodException
     */
    private void parseAdvice(Class<?> clazz, Method method, String expression,Class adviceType,int order) throws NoSuchMethodException {
        //解析表达式
        //Advice advice = AspectUtils.parseAdviceMethod(expression);
        AdviceMatcher matcher = new AdviceMatcher();
        List<Advice> adviceList = matcher.match(expression);
        for (Advice advice : adviceList) {
            advice.setOrder(order);
            //从容器中获取实例对象，注意：增强对象必须为容器对象
            Object target = beanFactory.getBean(advice.getClassSimpleName());
            if (target == null) {
                throw new RuntimeException("增强方法解析错误：" + advice.getClassSimpleName());
            }
            //目前无法解决handlerMapping映射controller层代理类
            //todo
            if (target.getClass().isAnnotationPresent(Controller.class))
                return;
            //处理增强方法
            handleAdvice(clazz, method, advice, target, adviceType);
            //代理类已经创建，不需要再创建
            if (ClassUtils.isProxyClass(target.getClass())) {
                return;
            }
            //在创建代理类之前将类放进原始类集合中
            originalClass.put(target.getClass().getName(), target);
            //创建代理类，并将其放进容器中
            createProxyAndPutIntoIOC(advice.getClassSimpleName(), target);
        }
    }

    /**
     *
     * @param clazz 增强类
     * @param method 增强方法
     * @param advice 被增强表达式解析出来的 对象
     * @param target 被增强的已经实例化好的类
     * @param AdviceType 增强类型
     * @throws NoSuchMethodException
     */
    private void handleAdvice(Class<?> clazz, Method method, Advice advice, Object target,Class AdviceType) throws NoSuchMethodException {
        String simpleName = ClassUtils.lowerFirstCase(clazz.getSimpleName());
        Object adviceInstance = beanFactory.getBean(simpleName);
        Method targetMethod = null;
        /**
         * 该类为代理类，利用原始类来获取被增强方法
         * 避免直接调用代理类获取方法时，代理方法重复执行导致多次实例化的问题
         */
        if (ClassUtils.isProxyClass(target.getClass())){
            String nameWithoutCglibMarkFromProxy = ClassUtils.getClassNameWithoutCglibMarkFromProxy(target.getClass());
            Object originalClass = this.originalClass.get(nameWithoutCglibMarkFromProxy);
            //todo
            targetMethod = originalClass.getClass().getMethod(advice.getMethodSimpleName(),advice.getArgs());
        }else {
            //todo
            targetMethod = target.getClass().getMethod(advice.getMethodSimpleName(),advice.getArgs());
        }
        //添加切面类
        AbstractAspect abstractAspect = null;
        if (AdviceType == Before.class)
            abstractAspect = new BeforeAspect(adviceInstance,method,method.getParameters(),advice.getOrder());
        else if (AdviceType == After.class)
            abstractAspect = new AfterAspect(adviceInstance,method,method.getParameters(),advice.getOrder());
        Map<Method, List<AbstractAspect>> proxyMethodHandlers = RegisterCenter.proxyMethodHandlers;
        List<AbstractAspect> abstractAspects = proxyMethodHandlers.get(targetMethod);
        if (abstractAspects != null && abstractAspects.size() > 0){
            abstractAspects.add(abstractAspect);
            proxyMethodHandlers.put(targetMethod, abstractAspects);
        }else {
            List<AbstractAspect> aspects = new ArrayList<>();
            aspects.add(abstractAspect);
            RegisterCenter.proxyMethodHandlers.put(targetMethod, aspects);
        }
    }

    /**
     *  创建代理类，并将代理类放到ioc容器中
     * @param beanName 实体类的类名，也是ioc容器的key
     * @param target  被代理类
     */
    private void createProxyAndPutIntoIOC(String beanName, Object target) {
        //创建代理类
        ProxyMethodHandler proxyMethodHandler = new ProxyMethodHandler();
        Object proxy = proxyMethodHandler.getProxy(target);
        for (Class<?> aClass : target.getClass().getInterfaces()) {
            beanFactory.getBeans().put(aClass.getName(),proxy);
        }
        beanFactory.getBeans().put(beanName,proxy);
    }

    /**
     * 扫描包
     * @param pck
     */
    private void doScanPack(String pck) {
        URL url = this.getClass().getClassLoader().getResource("/" + pck.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()){
                doScanPack(pck + "." + file.getName());
            }else {
                String clazz = pck + "." + file.getName().replace(".class","");
                beanNames.add(clazz);
            }
        }
    }

    /**
     * 匿名内部类，对请求映射关系进行封装
     */
    private class requestMappingHandler{
        //实例对象
        public Object instance;
        //映射方法
        public Method method;
        //用于保存restful url中的参数名，如：、rainple/user/{name}/{age},pathVariables保存的是 name,age
        String[] pathVariables;

        public requestMappingHandler(Object instance,Method method,String[] pathVariables){
            this.instance = instance;
            this.method = method;
            this.pathVariables = pathVariables;
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
                paramNames = ClassUtils.parameterNameDiscovery(instance.getClass().getName(), method);
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
                            if (reqVal == null)
                                throw new RuntimeException("无法从请求参数中获取值，请检查：" + method.getName());
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

    /**
     * servlet容器销毁时调用该方法，将容器清空
     */
    @Override
    public void destroy() {
        beanFactory.clear();
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

