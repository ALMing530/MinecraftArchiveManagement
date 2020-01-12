package wxm.entity;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;

public class Params {
    public HashMap params= new HashMap();
    public Params addParams(String name, Object value){
        params.put(name,value);
        return this;
    }
    public String generate(){
        if(params.isEmpty()){
            throw new RuntimeException("参数未初始化");
        }
        String string = JSON.toJSONString(params);
        System.out.println(string);
        return string;
    }
}
