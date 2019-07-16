package com.rainple.framework.modal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-16 11:32
 **/
public class Modal {

    private Map<String,Object> modal = new HashMap<>();

    public Object put(String key,Object object) {
        return modal.put(key,object);
    }

    public Object remove(String key) {
        return modal.remove(key);
    }

    public Object get(String key) {
        return modal.get(key);
    }

    public Set<String> keySet() {
        return modal.keySet();
    }

    public Set<Map.Entry<String,Object>> entrySet() {
        return modal.entrySet();
    }

}
