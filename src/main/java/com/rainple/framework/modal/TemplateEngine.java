package com.rainple.framework.modal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 模板引擎，用于解析页面
 * @author: rainple
 * @create: 2019-07-16 11:59
 **/
public class TemplateEngine {

    private RandomAccessFile accessFile;
    private ModalAndView modalAndView;

    public TemplateEngine(ModalAndView modalAndView) {
        this.modalAndView = modalAndView;
        try {
            accessFile = new RandomAccessFile("C:\\data\\ideaProjects\\myFramework\\src\\webapp\\pages\\hello.html","rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void parse() {

    }

    private void replace(String oldStr,String newStr) {
        String line;
        long lastPoint = 0;
        try {
            while ((line = accessFile.readLine()) != null) {
                final long point = accessFile.getFilePointer();
                if(line.contains(oldStr)){
                    String str=line.replace(oldStr, newStr);
                    accessFile.seek(lastPoint);
                    accessFile.writeBytes(str);
                }
                lastPoint = point;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String page() {
        StringBuilder builder = new StringBuilder();
        String line;
        try{
            Modal modal = modalAndView.getModal();
            Pattern regex = Pattern.compile("#[a-z]*\\([a-z]*\\)");
            while ((line = accessFile.readLine()) != null) {
                Matcher matcher = regex.matcher(line);
                while (matcher.find()) {
                    String group = matcher.group().trim();
                    System.out.println(group);
                    String key = group.substring(2,group.length() - 1);
                    if (key.startsWith("list")) {
                        key = key.split(" as ")[1];
                    }
                    Object val = modal.get(key);
                    if (val == null)
                        throw new RuntimeException("没有找到节点：" + key);
                    line = line.replace(group,val.toString());

                }
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                accessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        Modal map = new Modal();
        map.put("name","rainple");
        ModalAndView modalAndView = new ModalAndView("hello",map);
        TemplateEngine templateEngine = new TemplateEngine(modalAndView);
        String page = templateEngine.page();
    }

}
