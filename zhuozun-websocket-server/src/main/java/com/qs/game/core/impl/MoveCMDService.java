package com.qs.game.core.impl;

import com.qs.game.common.ERREnum;
import com.qs.game.common.game.CMD;
import com.qs.game.common.game.CommandService;
import com.qs.game.common.netty.Global;
import com.qs.game.core.ICommonService;
import com.qs.game.core.IMoveCMDService;
import com.qs.game.core.IThreadService;
import com.qs.game.model.base.ReqEntity;
import com.qs.game.model.base.RespEntity;
import com.qs.game.model.game.Pool;
import com.qs.game.model.game.PoolCell;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by zun.wei on 2018/9/9.
 * 移动鲲命令接口实现类
 */
@Slf4j
@CommandService(CMD.MOVE)
public class MoveCMDService implements IMoveCMDService {


    @Autowired
    private Global global;

    @Autowired
    private ICommonService commonService;


    @Override
    public Runnable execute(ChannelHandlerContext ctx, TextWebSocketFrame msg, ReqEntity reqEntity) {
        IThreadService.executor.execute(() -> {
            Integer cmd = reqEntity.getCmd();
            String mid = this.getPlayerId(ctx); //管道中的用户mid
            Map<String, Object> params = reqEntity.getParams();
            Integer fromIndex = commonService.getAndCheckRequestNo(this.getClass(), "from", cmd, mid, params);
            Integer toIndex = commonService.getAndCheckRequestNo(this.getClass(), "to", cmd, mid, params);
            if (Objects.isNull(fromIndex) || Objects.isNull(toIndex)) return;

            //获取玩家的鲲池
            Pool pool = commonService.getAndCheckPool(this.getClass(),cmd, mid);
            if (Objects.isNull(pool)) return;

            List<PoolCell> poolCells = pool.getPoolCells();

            PoolCell fromCell = poolCells.get(fromIndex);
            PoolCell toCell = poolCells.get(toIndex);

            //校验原对象单元格是否为空
            if (Objects.isNull(fromCell)) {
                log.info("MoveCMDService execute fromCell is null !");
                return;
            }

            //判断原对象单元格鲲是否在工作
            if (fromCell.getKuns().getWork() > 0) {
                log.info("MoveCMDService execute fromCell.getKuns() less than 1 !");
                return;
            }

            if (Objects.isNull(toCell)) {
                log.info("MoveCMDService execute toCell is null !");
                return;
            }


            //判断新单元格位置是否为空位置
            if (toCell.getKuns().getType() < 1) {
                poolCells = this.switchNo(fromIndex, toIndex, fromCell, toCell, poolCells);
                //保存鲲池到缓存和内存
                commonService.savePool2CacheAndMemory(mid, pool.setPoolCells(poolCells));
                String resultStr = RespEntity.getBuilder().setCmd(cmd).setErr(ERREnum.SUCCESS).buildJsonStr();
                global.sendMsg2One(resultStr, mid);
                return;
            }

            int fromType = fromCell.getKuns().getType();
            int toType = toCell.getKuns().getType();

            // 判断两个单元格的鲲的类型是否一致
            if (fromType == toType) {
                log.info("MoveCMDService execute fromType equals toType !");
                return;
            }

            //判断新单元格鲲是否在工作
            if (toCell.getKuns().getWork() > 0) {
                log.info("MoveCMDService execute toCell.getKuns().getWork() > 0 !");
                return;
            }

            //如果两个单元格鲲类型不一致并且都不在工作则互换位置
            poolCells = this.switchNo(fromIndex, toIndex, fromCell, toCell, poolCells);
            //保存鲲池到缓存和内存
            commonService.savePool2CacheAndMemory(mid, pool.setPoolCells(poolCells));

            String resultStr = RespEntity.getBuilder().setCmd(cmd).setErr(ERREnum.SUCCESS).buildJsonStr();
            global.sendMsg2One(resultStr, mid);
            ReferenceCountUtil.release(msg);
        });
        return null;
    }

    @Override
    public List<PoolCell> switchNo(Integer fromIndex, Integer toIndex,
                                   PoolCell fromCell, PoolCell toCell, List<PoolCell> poolCells) {
        PoolCell targetFrom = new PoolCell();
        PoolCell targetTo = new PoolCell();
        BeanUtils.copyProperties(fromCell, targetFrom);
        BeanUtils.copyProperties(toCell, targetTo);
        poolCells = poolCells.stream().map(e -> {
            if (Objects.equals(e.getNo(), fromIndex))
                return e.setKuns(targetTo.getKuns());
            if (Objects.equals(e.getNo(), toIndex))
                return e.setKuns(targetFrom.getKuns());
            return e;
        }).collect(Collectors.toList());
        //.sorted(Comparator.comparingInt(PoolCell::getNo))
        return poolCells;
    }


}
