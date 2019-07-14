package com.rainple.framework.core.filter;

import java.lang.reflect.Method;

public interface HandlerChain {

    Object handle(Object instance, Method method, Object[] args);

}
