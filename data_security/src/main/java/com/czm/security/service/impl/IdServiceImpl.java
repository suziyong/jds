package com.czm.security.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.czm.security.service.IdService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author a_fig
 * @version V0.1
 * @className IdServiceImpl
 * @date 2020/8/6
 */
@Service
public class IdServiceImpl implements IdService {

    /**
     * 批量生成不重复id
     *
     * @param worker    工作台编号
     * @param dataCenter    数据中心编号
     * @param number    批量生成id的数量
     * @return java.util.List<java.lang.Long>
     * @author a_fig
     * @date 2020/8/6 16:30
     */
    @Override
    public List<Long> generateBatchId(long worker, long dataCenter, int number) {
        Snowflake snowflakeId = new Snowflake(worker, dataCenter);
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            result.add(snowflakeId.nextId());
        }
        return result;
    }

    /**
     * 生成一个不重复id
     *
     * @param worker    工作台编号
     * @param dataCenter    数据中心编号
     * @return long
     * @author a_fig
     * @date 2020/8/6 16:52
     */
    @Override
    public long generateId(long worker, long dataCenter) {
        Snowflake snowflakeId = new Snowflake(worker, dataCenter);
        return snowflakeId.nextId();
    }

    /**
     * 获取id中的工作台编号
     *
     * @param id id
     * @return java.lang.Long
     * @author a_fig
     * @date 2020/8/6 17:02
     */
    @Override
    public Long getWorkerNum(Long id) {
        Snowflake snowflakeId = new Snowflake(0, 0);
        return snowflakeId.getWorkerId(id);
    }

    /**
     * 获取id中的数据中心编号
     *
     * @param id    id
     * @return java.lang.Long
     * @author a_fig
     * @date 2020/8/6 17:07
     */
    @Override
    public Long getDataCenterNum(Long id) {
        Snowflake snowflakeId = new Snowflake(0, 0);
        return snowflakeId.getDataCenterId(id);
    }

    /**
     * 获取id中的创建时间
     *
     * @param id    id
     * @return java.lang.String
     * @author a_fig
     * @date 2020/8/6 17:15
     */
    @Override
    public String getGenerateDateTime(Long id) {
        Snowflake snowflakeId = new Snowflake(0, 0);
        long generateDateTimeLong = snowflakeId.getGenerateDateTime(id);
        LocalDateTime generateLocalDateTime = LocalDateTime.ofEpochSecond(generateDateTimeLong, 0, ZoneOffset.ofHours(8));
        return generateLocalDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
