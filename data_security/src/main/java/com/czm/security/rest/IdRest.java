package com.czm.security.rest;

import com.czm.security.service.IdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author a_fig
 * @version V0.1
 * @className IdRest
 * @date 2020/8/6
 */
@RequestMapping("/security/id")
@RestController
public class IdRest {

    /**
     * 注入id逻辑接口
     *
     * @author a_fig
     * @date 2020/8/6 16:25
     */
    @Resource
    private IdService idService;

    /**
     * 批量生成不重复id(每次最高生成一百万条id)
     *
     * @param worker    工作台编号
     * @param dataCenter    数据中心编号
     * @param number    批量生成id的数量
     * @return java.util.List<java.lang.Long>
     * @author a_fig
     * @date 2020/8/6 16:30
     */
    @GetMapping("/list")
    public List<Long> generateBatchId(Long worker, Long dataCenter, Integer number) {
        // 工作台&数据中心编号，最小值 0
        long min = 0L;
        // 工作台&数据中心编号，最大值 31
        long max = 31L;
        // 生成数量最小值1条
        long numberMin = 1;
        // 生成数量最大值1000000条（一百万）
        long numberMax = 1000000;

        // 校验参数
        if (worker == null || worker < min || worker > max) {
            return new ArrayList<>();
        }
        if (dataCenter == null || dataCenter < min || dataCenter > max) {
            return new ArrayList<>();
        }
        if (number == null || number < numberMin || number > numberMax) {
            return new ArrayList<>();
        }
        // 批量生成不重复id
        return idService.generateBatchId(worker, dataCenter, number);
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
    @GetMapping("/single")
    public Long generateId(Long worker, Long dataCenter) {
        // 工作台&数据中心编号，最小值 0
        long min = 0L;
        // 工作台&数据中心编号，最大值 31
        long max = 31L;

        // 校验参数
        if (worker == null || worker < min || worker > max) {
            return null;
        }
        if (dataCenter == null || dataCenter < min || dataCenter > max) {
            return null;
        }
        // 生成不重复id
        return idService.generateId(worker, dataCenter);
    }

    /**
     * 获取id中的工作台编号
     *
     * @param id    id
     * @return java.lang.Long
     * @author a_fig
     * @date 2020/8/6 17:02
     */
    @GetMapping("/worker/num")
    public Long getWorkerNum(Long id) {
        if (id == null) {
            return null;
        }
        return idService.getWorkerNum(id);
    }

    /**
     * 获取id中的数据中心编号
     *
     * @param id    id
     * @return java.lang.Long
     * @author a_fig
     * @date 2020/8/6 17:07
     */
    @GetMapping("/data/center/num")
    public Long getDataCenterNum(Long id) {
        if (id == null) {
            return null;
        }
        return idService.getDataCenterNum(id);
    }

    /**
     * 获取id中的创建时间
     *
     * @param id    id
     * @return java.lang.String
     * @author a_fig
     * @date 2020/8/6 17:15
     */
    @GetMapping("/generate/date/time")
    public String getGenerateDateTime(Long id) {
        if (id == null) {
            return null;
        }
        return idService.getGenerateDateTime(id);
    }
}
