package com.miracle.rpc.transport;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author miracle
 * @date 2021/4/19 9:29
 */
public class RemoveExpireThreadFactory implements ThreadFactory {
    private final String prefix;
    private final AtomicInteger threadNum;
    protected final boolean daemon;
    protected final ThreadGroup group;

    public RemoveExpireThreadFactory(String prefix, boolean daemon) {
        this.threadNum = new AtomicInteger(1);
        this.prefix = prefix + "-thread-";
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        this.group = s == null ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        String name = this.prefix + this.threadNum.getAndIncrement();
        Thread ret = new Thread(this.group, runnable, name, 0L);
            ret.setDaemon(this.daemon);
        return ret;
    }
}