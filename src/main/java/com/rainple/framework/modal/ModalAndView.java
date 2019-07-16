package com.rainple.framework.modal;

/**
 * @description:
 * @author: rainple
 * @create: 2019-07-16 11:31
 **/
public class ModalAndView {

    private String view;
    private Modal modal;

    public ModalAndView(){}

    public ModalAndView(String view,Modal modal){
        this.view = view;
        this.modal = modal;
    }

    public void setAttribute(String key,Object object) {
        modal.put(key,object);
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Modal getModal() {
        return modal;
    }

    public void setModal(Modal modal) {
       this.modal = modal;
    }
}
