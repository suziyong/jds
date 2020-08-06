package com.czm.security.service;

import java.util.List;

/**
 * @author a_fig
 * @version V0.1
 * @interfaceName IdService
 * @date 2020/8/6
 */
public interface IdService {

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
    List<Long> generateBatchId(long worker, long dataCenter, int number);

    /**
     * 生成一个不重复id
     *
     * @param worker    工作台编号
     * @param dataCenter    数据中心编号
     * @return long
     * @author a_fig
     * @date 2020/8/6 16:52
     */
    long generateId(long worker, long dataCenter);

    /**
     * 获取id中的工作台编号
     *
     * @param id id
     * @return java.lang.Long
     * @author a_fig
     * @date 2020/8/6 17:02
     */
    Long getWorkerNum(Long id);

    /**
     * 获取id中的数据中心编号
     *
     * @param id    id
     * @return java.lang.Long
     * @author a_fig
     * @date 2020/8/6 17:07
     */
    Long getDataCenterNum(Long id);

    /**
     * 获取id中的创建时间
     *
     * @param id    id
     * @return java.lang.String
     * @author a_fig
     * @date 2020/8/6 17:15
     */
    String getGenerateDateTime(Long id);
}
