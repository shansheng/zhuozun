package com.qs.game.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.*;

/**
 * Created by zun.wei on 2018/8/28 19:32.
 * Description:
 */
@Data
@Accessors(chain = true)
public class ReqEntity implements Serializable {

    private Integer cmd;

    private String token;

    private String sign;

    private Long stamp;

    private Map<String, Object> params;

    public static void main(String[] args) {
        ReqEntity reqEntity = new ReqEntity();
        Map<String, Object> req = new HashMap<>();
        req.put("user", "zhansgan");
        req.put("passwore", "dasfa");
        req.put("sex", "1");
        reqEntity.setCmd(11).setToken("abcdefg").setSign("aaaa")
                .setStamp(new Date().getTime() / 1000).setParams(req);
        String json = JSONObject.toJSONString(reqEntity);
        System.out.println("reqEntity = " + json);
        json = "{\"aa\":11}";
        ReqEntity reqEntity1 = JSONObject.parseObject(json, ReqEntity.class);
        System.out.println("reqEntity1 = " + reqEntity1);

    }

}
