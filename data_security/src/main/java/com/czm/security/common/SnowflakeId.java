package com.czm.security.common;

import java.io.Serializable;
import java.util.Date;

/**
 * Twitter_Snowflake<br>
 * SnowFlake的结构如下(每部分用-分开):<br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000 <br>
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0<br>
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序IdWorker类的startTime属性）。41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69<br>
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId<br>
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号<br>
 * 加起来刚好64位，为一个Long型。<br>
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 */
public class SnowflakeId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long twepoch;
    private final long workerIdBits = 5L;
    private final long dataCenterIdBits = 5L;
    //// 最大支持机器节点数0~31，一共32个
    // 最大支持数据中心节点数0~31，一共32个
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private final long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);
    // 序列号12位
    private final long sequenceBits = 12L;
    // 机器节点左移12位
    private final long workerIdShift = sequenceBits;
    // 数据中心节点左移17位
    private final long dataCenterIdShift = sequenceBits + workerIdBits;
    // 时间毫秒数左移22位
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    @SuppressWarnings({"PointlessBitwiseExpression", "FieldCanBeLocal"})
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);// 4095

    private final long workerId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造
     *
     * @param workerId     终端ID
     * @param dataCenterId 数据中心ID
     */
    public SnowflakeId(long workerId, long dataCenterId) {
        this(null, workerId, dataCenterId);
    }



    /**
     * @param epochDate        初始化时间起点（null表示默认起始日期）,后期修改会导致id重复,如果要修改连workerId dataCenterId，慎用
     * @param workerId         工作机器节点id
     * @param dataCenterId     数据中心id
     * @since 5.1.3
     */
    public SnowflakeId(Date epochDate, long workerId, long dataCenterId) {
        if (null != epochDate) {
            this.twepoch = epochDate.getTime();
        } else{
            // Thu, 04 Nov 2010 01:42:54 GMT
            this.twepoch = 1288834974657L;
        }
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDataCenterId));
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    /**
     * 根据Snowflake的ID，获取机器id
     *
     * @param id snowflake算法生成的id
     * @return 所属机器的id
     */
    public long getWorkerId(long id) {
        return id >> workerIdShift & ~(-1L << workerIdBits);
    }

    /**
     * 根据Snowflake的ID，获取数据中心id
     *
     * @param id snowflake算法生成的id
     * @return 所属数据中心
     */
    public long getDataCenterId(long id) {
        return id >> dataCenterIdShift & ~(-1L << dataCenterIdBits);
    }

    /**
     * 根据Snowflake的ID，获取生成时间
     *
     * @param id snowflake算法生成的id
     * @return 生成的时间
     */
    public long getGenerateDateTime(long id) {
        return (id >> timestampLeftShift & ~(-1L << 41L)) + twepoch;
    }

    /**
     * 下一个ID
     *
     * @return ID
     */
    public synchronized long nextId() {
        long timestamp = genTime();
        if (timestamp < lastTimestamp) {
            if(lastTimestamp - timestamp < 2000){
                // 容忍2秒内的回拨，避免NTP校时造成的异常
                timestamp = lastTimestamp;
            } else{
                // 如果服务器时间有问题(时钟后退) 报错。
                throw new IllegalStateException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (dataCenterId << dataCenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    /**
     * 循环等待下一个时间
     *
     * @param lastTimestamp 上次记录的时间
     * @return 下一个时间
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = genTime();
        // 循环直到操作系统时间戳变化
        while (timestamp == lastTimestamp) {
            timestamp = genTime();
        }
        if (timestamp < lastTimestamp) {
            // 如果发现新的时间戳比上次记录的时间戳数值小，说明操作系统时间发生了倒退，报错
            throw new IllegalStateException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        return timestamp;
    }

    /**
     * 生成时间戳
     *
     * @return 时间戳
     */
    private long genTime() {
        return System.currentTimeMillis();
    }

}
