package com.qs.game.core.impl;

import com.qs.game.common.ERREnum;
import com.qs.game.common.game.CMD;
import com.qs.game.common.game.CommandService;
import com.qs.game.common.game.KunType;
import com.qs.game.common.netty.Global;
import com.qs.game.config.game.GameManager;
import com.qs.game.core.ICommonService;
import com.qs.game.core.IThreadService;
import com.qs.game.core.IWorkCMDService;
import com.qs.game.model.base.ReqEntity;
import com.qs.game.model.base.RespEntity;
import com.qs.game.model.game.Kuns;
import com.qs.game.model.game.Pool;
import com.qs.game.model.game.PoolCell;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by zun.wei on 2018/9/9.
 * 创建一个新对象命令接口实现类
 */
@Slf4j
@CommandService(CMD.NEW)
public class NewCMDService implements IWorkCMDService {


    @Autowired
    private ICommonService commonService;

    @Autowired
    private Global global;


    @Override
    public Runnable execute(ChannelHandlerContext ctx, TextWebSocketFrame msg, ReqEntity reqEntity) {
        IThreadService.executor.execute(() -> {
            Integer cmd = reqEntity.getCmd();
            String mid = this.getPlayerId(ctx); //管道中的用户mid
            Map<String, Object> params = reqEntity.getParams();
            //校验参数是否为空
            Integer noIndex = commonService.getAndCheckRequestNo(this.getClass(), "no", cmd, mid, params);
            if (Objects.isNull(noIndex)) return;

            //获取玩家的鲲池
            Pool pool = commonService.getAndCheckPool(this.getClass(), cmd, mid);
            if (Objects.isNull(pool)) return;


            List<PoolCell> poolCells = pool.getPoolCells();
            PoolCell pc = poolCells.get(noIndex);

            // 如果要新建的位置已经有鲲占位置了，不允许新加
            if (pc.getKuns().getType() > KunType.TYPE_0) {
                log.info("NewCMDService poolCell.getKuns().getType() > 0 ");
                String resultStr = RespEntity.getBuilder().setCmd(cmd).setErr(ERREnum.SEAT_NOT_EMPTY).buildJsonStr();
                global.sendMsg2One(resultStr, mid);
                return;
            }

            //调用一次新增，添加一只最小的鲲
            int type = KunType.TYPE_1;
            poolCells = poolCells.stream().peek(e -> {
                if (Objects.equals(e.getNo(), noIndex)) {
                    e.setKuns(new Kuns().setType(type).setWork(0).setTime(0));
                }
            }).collect(Collectors.toList());

            //保存鲲池到缓存和内存
            commonService.savePool2CacheAndMemory(mid, pool.setPoolCells(poolCells));

            Map<String, Object> content = new HashMap<>();
            content.put("no", noIndex);
            content.put("type", type);
            String resultStr = RespEntity.getBuilder().setCmd(cmd).setErr(ERREnum.SUCCESS).setContent(content).buildJsonStr();
            global.sendMsg2One(resultStr, mid);
            ReferenceCountUtil.release(msg);
        });
        return null;
    }



}
