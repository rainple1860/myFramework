package com.rainple.framework.aop.advice;

import com.rainple.framework.utils.ClassUtils;
import com.rainple.framework.utils.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 增强方法的解析器
 * @author: rainple
 * @create: 2019-07-10 11:35
 **/
public class AdviceParser {

    public static void main(String[] args) {
        AdviceParser adviceParser = new AdviceParser(null);
    }

    private AdviceExpressionInformation expressionInformation;
    private List<String> beanNames;

    public AdviceParser(List<Class> beanClass) {
        beanNames = beanClass.stream().map(Class::getName).collect(Collectors.toList());
    }

    public List<Advice> parse(String exp) {
        expressionInformation = new AdviceExpressionInformation();
        List<Advice> adviceList;
        exp = paramParser(exp);
        exp = methodParser(exp);
        exp = clazzParser(exp);
        reAndModifierParser(exp);
        String clazz = expressionInformation.getClazz();
        int clazzLen = clazz.split("\\.").length;
        if (clazz.contains("*")) {
            List<String> eqList = new ArrayList<>();
            if (clazz.endsWith("*")) {
                int len = clazz.length();
                int olen = len;
                for(;clazz.endsWith("*");){
                    len -= 2;
                    clazz = clazz.substring(0,len);
                }
                if (!clazz.contains("*")) {
                    if (olen - 2 == len) {//这种情况是表达式中类名最后只有一个*
                        for (String beanName : beanNames) {
                            if (beanName.startsWith(clazz))
                                eqList.add(beanName);
                        }
                    }else {//这种情况最后有多个*，这种情况需保证包的深度一致
                        for (String beanName : beanNames) {
                            int benLen = beanName.split("\\.").length;
                            if (beanName.startsWith(clazz.substring(0, clazz.length() - 2)) && benLen == clazzLen)
                                eqList.add(beanName);
                        }
                    }
                }else {
                    String[] split = clazz.split("\\.");
                    for (String beanName : beanNames) {
                        String[] split1 = beanName.split("\\.");
                        int eqCount = 0;
                        for (int i = 0; i < split.length; i++) {
                            if (split[i].equals(split1[i]) || "*".equals(split[i])) {
                                eqCount++;
                            }
                        }
                        if (eqCount == split.length) {
                            if (len + 2 < olen) {
                                if (split1.length == clazzLen) {
                                    eqList.add(beanName);
                                }
                            } else {
                                eqList.add(beanName);
                            }
                        }
                    }
                }
            }else {
                String[] split = clazz.split("\\.");
                for (String beanName : beanNames) {
                    String[] split1 = beanName.split("\\.");
                    int eqCount = 0;
                    for (int i = 0; i < split.length; i++) {
                        if (split.length != split1.length) break;
                        if (split[i].equals(split1[i]) || "*".equals(split[i])) {
                            eqCount++;
                        }
                    }
                    if (eqCount == split.length) {
                        eqList.add(beanName);
                    }
                }
            }
            adviceList = matchAdvice(eqList);
        }else {
            adviceList = matchAdvice(Collections.singletonList(clazz));
        }
        return adviceList;
    }

    private List<Advice> matchAdvice(List<String> classes) {
        List<Advice> adviceList = new ArrayList<>();
        for (String clazz : classes) {
            Class cl = ClassUtils.forName(clazz);
            assert cl != null;
            for (Method method : cl.getDeclaredMethods()) {
                String informationMethod = expressionInformation.getMethod();
                String methodName = method.getName();
                boolean paramMatch = false;
                if ("*".equals(informationMethod)){
                    paramMatch = parametersMatch(method);
                }else if (informationMethod.equals(methodName)){
                    paramMatch = parametersMatch(method);
                }
                if (!paramMatch) continue;
                String returnType = expressionInformation.getReturnType();
                if (!"*".equals(returnType) && !returnType.equals(method.getReturnType().getName())) continue;
                String modifier = expressionInformation.getModifier();
                if (modifier.contains("abstract") || "interface".equals(modifier)) continue;
                if (!"*".equals(modifier) && !modifier.equals(Modifier.toString(method.getModifiers()))) continue;
                Advice advice = new Advice();
                advice.setClassName(clazz);
                advice.setClassSimpleName(ClassUtils.lowerFirstCase(cl.getSimpleName()));
                advice.setArgs(method.getParameterTypes());
                advice.setMethodSimpleName(methodName);
                advice.setMethodName(methodName);
                advice.setMethod(method);
                adviceList.add(advice);
            }
        }
        return adviceList;
    }

    private boolean parametersMatch(Method method) {
        List<String> parameters = expressionInformation.getParameters();
        Class<?>[] types = method.getParameterTypes();
        if (parameters == null) {
            if (types.length == 0) {
                return true;
            }
            return false;
        }
        if (parameters.size() == 1 && "..".equals(parameters.get(0))) //匹配所有参数
           return true;
        if (types.length != parameters.size()) return false;
        int i = 0;
        for (Class<?> c : types) {
            String name = c.getName();
            if (parameters.get(i).equals(name)) {
                i++;
            }
        }
        if (i == types.length) {
            return true;
        }
        return false;
    }

    private String paramParser(String exp) {
        int left = exp.lastIndexOf("(");
        int right = exp.lastIndexOf(")");
        String p = exp.substring(left+1,right);
        if (StringUtils.isEmpty(p)) {
            expressionInformation.setParameters(null);
        }else {
            if (p.contains(",")) {
                String[] split = p.split(",");
                List<String> list = new ArrayList<>();
                Collections.addAll(list, split);
                expressionInformation.setParameters(list);
            }else {
                expressionInformation.setParameters(Arrays.asList(p));
            }
        }
        return exp.substring(0,left);
    }

    private String methodParser(String exp) {
        int index = exp.lastIndexOf(".");
        String m = exp.substring(index+1);
        expressionInformation.setMethod(m);
        return exp.substring(0,index);
    }

    private String clazzParser(String exp) {
        int start = exp.lastIndexOf(" ");
        String c = exp.substring(start+1);
        expressionInformation.setClazz(c);
        return exp.substring(0,start);
    }

    private void reAndModifierParser(String exp) {
        if (exp.contains(" ")) {
            String t = exp.substring(exp.lastIndexOf(" ")+1);
            expressionInformation.setReturnType(t);
            String m = exp.substring(0, exp.lastIndexOf(" "));
            expressionInformation.setModifer(m);
        }else {
            expressionInformation.setReturnType(exp);
            expressionInformation.setModifer(exp);
        }
    }

}
