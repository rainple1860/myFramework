package com.rainple.framework.servlet;

import com.rainple.framework.Constant.ConfigEnum;
import com.rainple.framework.annotation.Aspect;
import com.rainple.framework.annotation.Autowired;
import com.rainple.framework.annotation.Controller;
import com.rainple.framework.annotation.aspect.After;
import com.rainple.framework.annotation.aspect.Before;
import com.rainple.framework.aop.AbstractAspect;
import com.rainple.framework.aop.AfterAspect;
import com.rainple.framework.aop.BeforeAspect;
import com.rainple.framework.aop.ProxyMethodHandler;
import com.rainple.framework.aop.advice.Advice;
import com.rainple.framework.aop.advice.AdviceParser;
import com.rainple.framework.bean.ComponentBean;
import com.rainple.framework.bean.ComponentBeanFactory;
import com.rainple.framework.bean.InvokeBean;
import com.rainple.framework.core.*;
import com.rainple.framework.core.filter.ControllerHandlerChain;
import com.rainple.framework.core.filter.HandlerChain;
import com.rainple.framework.utils.ClassUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DispatcherServlet extends HttpServlet {

    private static Logger logger = Logger.getLogger(DispatcherServlet.class);
    //里面装有已实例化的对象
    private BeanFactory beanFactory = BeanFactory.getBeanFactory();
    //用户扫描的所有类
    private List<Class> beanClass = new ArrayList<>();
    //用户请求链映射,controller中使用到这种方式
    private static Map<Method,HandlerChain> filterMap = new HashMap<>();
    //代理类的方法映射
    private static Map<Method, List<AbstractAspect>> proxyMethodHandlers = new HashMap<>();


    /*存放未被代理的类*/
    private Map<String,Object> originalClass = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
        req.setCharacterEncoding("utf-8");
        resp.setCharacterEncoding("utf-8");
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doDispatcher(req,resp);
    }

    @Override
    public void init(ServletConfig config) {
        try {
            logger.info("开始初始化容器");
            //加载配置文件
            doLoadConfig(config);
            //扫包
            doScanPack(ApplicationConfig.applicationConfig.getProperty(ConfigEnum.SCANPACKAGE.getName()));

            componentBeanFilter();

            //初始化bean
            doInstanceBeans();
            doAspect();
            //注入
            DoIoc();
            //处理映射关系
            handlerMapping();
            logger.info("容器初始化完毕");
        }catch (Exception e) {
            logger.error("容器启动失败",e);
        }
    }

    private void componentBeanFilter() {
        ComponentBeanFactory instance = ComponentBeanFactory.getInstance();
        for (Class clazz : beanClass) {
            if (clazz.isAnnotation()) continue;
            Annotation[] annotations = clazz.getAnnotations();
            for (Annotation annotation : annotations) {
                ComponentBean componentBean = new ComponentBean();
                componentBean.setAnnotation(annotation);
                componentBean.setBeanClass(clazz);
                instance.addBean(componentBean);
            }
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
        new HandlerMappingSupporter(filterMap).execute(req,resp);
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
        new HandlerMappingSupporter(filterMap).parse();
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
        if (beanClass.isEmpty())
            return;
        List<Class> beans = beanClass;
        List<BeanInstanceHandler> beanHandlers = ClassUtils.getChildFromSuperToInstance(BeanInstanceHandler.class);
        BeanInstanceHandlerChain chain = new BeanInstanceHandlerChain(beanHandlers,beans);
        chain.proceed();
        logger.info("初始化Bean完成....");
    }

    /**
     *解析增强方法
     */
    private void doAspect(){
        List<ComponentBean> beans = ComponentBeanFactory.getInstance().getBeansByAnnotation(Aspect.class);
        if (beans.isEmpty())
            return;
        for (ComponentBean bean : beans) {
            try {
                Class clazz = bean.getBeanClass();
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
        AdviceParser parser = new AdviceParser(beanClass);
        List<Advice> adviceList = parser.parse(expression);
        for (Advice advice : adviceList) {
            advice.setOrder(order);
            //从容器中获取实例对象，注意：增强对象必须为容器对象
            Object target = beanFactory.getBean(advice.getClassSimpleName());
            if (target == null) {
                throw new RuntimeException("增强方法解析错误：" + advice.getClassSimpleName());
            }
            //controller层的方法都是使用反射调用，所以不需要生成代理类，只需要在反射调用前后调用即可
            if (target.getClass().isAnnotationPresent(Controller.class)) {

                Object bean = beanFactory.getBean(clazz);
                if (bean == null)
                    bean = ClassUtils.newInstance(clazz);
                Method targetMethod = target.getClass().getMethod(advice.getMethodName(), advice.getArgs());
                InvokeBean invokeBean = new InvokeBean();
                invokeBean.setMethod(method);
                invokeBean.setInstance(bean);
                invokeBean.setOrder(order);
                invokeBean.setArgs(method.getParameterTypes());
                HandlerChain handlerChain = filterMap.get(targetMethod);
                if (handlerChain != null) {
                    if (handlerChain instanceof ControllerHandlerChain) {
                        ((ControllerHandlerChain) handlerChain).putChain(invokeBean,adviceType);
                    }
                }else {
                    ControllerHandlerChain controllerHandlerChain = new ControllerHandlerChain(ClassUtils.forName(advice.getClassName()),advice.getMethod(),advice.getMethod().getParameterTypes());
                    controllerHandlerChain.putChain(invokeBean,adviceType);
                    filterMap.put(targetMethod,controllerHandlerChain);
                }
                continue;
            }
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
        Method targetMethod;
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
        List<AbstractAspect> abstractAspects = proxyMethodHandlers.get(targetMethod);
        if (abstractAspects != null && abstractAspects.size() > 0){
            abstractAspects.add(abstractAspect);
            proxyMethodHandlers.put(targetMethod, abstractAspects);
        }else {
            List<AbstractAspect> aspects = new ArrayList<>();
            aspects.add(abstractAspect);
            proxyMethodHandlers.put(targetMethod, aspects);
        }
    }

    /**
     *  创建代理类，并将代理类放到ioc容器中
     * @param beanName 实体类的类名，也是ioc容器的key
     * @param target  被代理类
     */
    private void createProxyAndPutIntoIOC(String beanName, Object target) {
        //创建代理类
        ProxyMethodHandler proxyMethodHandler = new ProxyMethodHandler(proxyMethodHandlers);
        Object proxy = proxyMethodHandler.getProxy(target);
        //这里创建的代理会覆盖掉已经实例化的bean
        for (Class<?> aClass : target.getClass().getInterfaces()) {
            beanFactory.putBeanForce(aClass.getName(),proxy);
        }
        beanFactory.putBeanForce(beanName,proxy);
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
                beanClass.add(ClassUtils.forName(clazz));
            }
        }
    }

    /**
     * servlet容器销毁时调用该方法，将容器清空
     */
    @Override
    public void destroy() {
        beanFactory.clear();
    }
}

