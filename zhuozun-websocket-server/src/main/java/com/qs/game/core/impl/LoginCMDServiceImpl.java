package com.qs.game.core.impl;

import com.alibaba.fastjson.JSONObject;
import com.qs.game.common.*;
import com.qs.game.common.game.CMD;
import com.qs.game.common.game.CommandService;
import com.qs.game.common.game.KunGold;
import com.qs.game.common.netty.Global;
import com.qs.game.core.IThreadService;
import com.qs.game.model.base.ReqEntity;
import com.qs.game.model.base.RespEntity;
import com.qs.game.model.game.Kuns;
import com.qs.game.model.game.Pool;
import com.qs.game.model.game.PoolCell;
import com.qs.game.core.ICommonService;
import com.qs.game.core.ILoginCMDService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by zun.wei on 2018/9/6 13:49.
 * Description: 登录命令业务接口实现类
 */
@Slf4j
@CommandService(CMD.LOGIN)
public class LoginCMDServiceImpl implements ILoginCMDService {

    @Autowired
    private Global global;

    @Resource
    private ICommonService commonService;

    @Override
    public Runnable execute(ChannelHandlerContext ctx, TextWebSocketFrame msg, ReqEntity reqEntity) {
        Future future = IThreadService.executor.submit(() -> {
            Integer cmd = reqEntity.getCmd();
            String mid = this.getPlayerId(ctx); //管道中的用户mid
            //获取玩家鲲池
            List<PoolCell> poolCells = this.getPlayerKunPoolCells(mid);
            Map<String, Object> content = new HashMap<>();
            content.put("pool", poolCells); //玩家鲲池
            content.put("gold", commonService.getPlayerGold(mid)); //玩家金币
            //content.put("goldSpeed", this.getPlayerGoldSpeedByMid(mid)); //玩家产金币速度
            String resultStr = RespEntity.getBuilder().setCmd(cmd).setErr(ERREnum.SUCCESS)
                    .setContent(content).buildJsonStr();
            global.sendMsg2One(resultStr, mid);
        });
        try {
            Object o = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    1.当前金币数量，
    2。每秒产生的金币，根据鲲池中的鲲在工作的鲲的类型以及数量计算。
     */

    @Override
    public long getPlayerGoldSpeedByMid(String mid) {
        Pool pool = commonService.getPlayerKunPool(mid);
        return Objects.isNull(pool) ? 0L :
                pool.getPoolCells().stream()
                        .filter(e -> e.getKuns().getType() > 0 && e.getKuns().getWork() > 1)
                        .map(e -> KunGold.goldByType(e.getKuns().getType()))
                        .reduce((e1, e2) -> e1 + e2).orElse(0L);
    }


    @Override
    public long getPlayerGoldSpeedByKunPoolJson(String kunPoolJson) {
        if (StringUtils.isBlank(kunPoolJson)) {
            return 0L;
        } else {
            Map map = JSONObject.parseObject(kunPoolJson, Map.class);
            return JSONObject.parseArray(map.values().toString(), Kuns.class)
                    .stream()
                    //筛选出不是空的坑位 并且 正在工作的鲲
                    .filter(e -> e.getType() > 0 && e.getWork() > 0)
                    //把符合条件的鲲每秒产金币数合并统计
                    .map(e -> KunGold.goldByType(e.getType()))
                    .reduce((e1, e2) -> e1 + e2).orElse(0L);
        }
    }

    @Override
    public String getPlayerKunPoolCellsJson(String mid) {
        Pool pool = commonService.getPlayerKunPool(mid);
        return JSONObject.toJSONString(pool.getPoolCells());
    }

    @Override
    public List<PoolCell> getPlayerKunPoolCells(String mid) {
        Pool pool = commonService.getPlayerKunPool(mid);
        return pool.getPoolCells();
    }

}