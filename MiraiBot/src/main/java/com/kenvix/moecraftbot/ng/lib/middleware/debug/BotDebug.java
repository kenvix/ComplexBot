//--------------------------------------------------
// Class BotDebug
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib.middleware.debug;

import com.kenvix.moecraftbot.ng.lib.bot.AbstractDriver;
import com.kenvix.moecraftbot.ng.lib.bot.BotCommandQueryData;
import com.kenvix.moecraftbot.ng.lib.bot.BotUpdate;
import com.kenvix.moecraftbot.ng.lib.middleware.BotMiddleware;
import com.kenvix.utils.log.Logging;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;

public class BotDebug implements BotMiddleware {
    private AbstractDriver<?> driverContext;

    @Override
    public void onEnable(@NotNull AbstractDriver<?> driverContext) {
        this.driverContext = driverContext;
        driverContext.registerCommand("debug", this::onDebug);
    }

    private Unit onDebug(BotUpdate<?> botUpdate, BotCommandQueryData botCommandQueryData) {
        StringBuilder builder = new StringBuilder("MoeCraftBot Debug Info\n");

        //==========================Memory=========================
        builder.append("\n==========================Memory=========================");
        MemoryMXBean memoryMBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memoryMBean.getHeapMemoryUsage();
        builder.append("\n初始化 Heap: ").append(usage.getInit() / 1024 / 1024).append("MiB");
        builder.append("\n最大Heap: ").append(usage.getMax() / 1024 / 1024).append("MiB");
        builder.append("\n已经使用Heap: ").append(usage.getUsed() / 1024 / 1024).append("MiB");
        builder.append("\nHeap Memory Usage: ").append(memoryMBean.getHeapMemoryUsage());
        builder.append("\nNon-Heap Memory Usage: ").append(memoryMBean.getNonHeapMemoryUsage());
        //==========================Runtime=========================
        builder.append("\n==========================Runtime=========================");
        RuntimeMXBean runtimeMBean = ManagementFactory.getRuntimeMXBean();
        builder.append("\nJVM name : ").append(runtimeMBean.getVmName());
        builder.append("\nlib path : ").append(runtimeMBean.getLibraryPath());
        builder.append("\nclass path : ").append(runtimeMBean.getClassPath());
        builder.append("\ngetVmVersion() ").append(runtimeMBean.getVmVersion());
        //java options
        List<String> argList = runtimeMBean.getInputArguments();
        for(String arg : argList){
            builder.append("\narg : ").append(arg);
        }

        //==========================OperatingSystem=========================
        builder.append("\n==========================OperatingSystem=========================");
        OperatingSystemMXBean osMBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        //获取操作系统相关信息
        builder.append("\ngetName() ").append(osMBean.getName());
        builder.append("\ngetVersion() ").append(osMBean.getVersion());
        builder.append("\ngetArch() ").append(osMBean.getArch());
        builder.append("\ngetAvailableProcessors() ").append(osMBean.getAvailableProcessors());
        //==========================Thread=========================
        builder.append("\n==========================Thread=========================");
        //获取各个线程的各种状态，CPU 占用情况，以及整个系统中的线程状况
        ThreadMXBean threadMBean=(ThreadMXBean)ManagementFactory.getThreadMXBean();
        builder.append("\ngetThreadCount() ").append(threadMBean.getThreadCount());
        builder.append("\ngetPeakThreadCount() ").append(threadMBean.getPeakThreadCount());
        builder.append("\ngetCurrentThreadCpuTime() ").append(threadMBean.getCurrentThreadCpuTime());
        builder.append("\ngetDaemonThreadCount() ").append(threadMBean.getDaemonThreadCount());
        builder.append("\ngetCurrentThreadUserTime() ").append(threadMBean.getCurrentThreadUserTime());
        //==========================Compilation=========================
        builder.append("\n==========================Compilation=========================");
        CompilationMXBean compilMBean=(CompilationMXBean)ManagementFactory.getCompilationMXBean();
        builder.append("\ngetName() ").append(compilMBean.getName());
        builder.append("\ngetTotalCompilationTime() ").append(compilMBean.getTotalCompilationTime());
        //==========================MemoryPool=========================
        builder.append("\n==========================MemoryPool=========================");
        //获取多个内存池的使用情况
        List<MemoryPoolMXBean> mpMBeanList= ManagementFactory.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean mpMBean : mpMBeanList){
            builder.append("\ngetUsage() ").append(mpMBean.getUsage());
            builder.append("\ngetMemoryManagerNames() ").append(Arrays.toString(mpMBean.getMemoryManagerNames()));
        }
        //==========================GarbageCollector=========================
        builder.append("\n==========================GarbageCollector=========================");
        //获取GC的次数以及花费时间之类的信息
        List<GarbageCollectorMXBean> gcMBeanList=ManagementFactory.getGarbageCollectorMXBeans();
        for(GarbageCollectorMXBean gcMBean : gcMBeanList){
            builder.append("\ngetName() ").append(gcMBean.getName());
            builder.append("\ngetMemoryPoolNames() ").append(Arrays.toString(gcMBean.getMemoryPoolNames()));
        }
        //==========================Other=========================
        builder.append("\n==========================Other=========================");
        //Java 虚拟机中的内存总量,以字节为单位
        int total = (int)Runtime.getRuntime().totalMemory()/1024/1024;
        builder.append("\nTotal Memory ：").append(total).append("MiB");
        int free = (int)Runtime.getRuntime().freeMemory()/1024/1024;
        builder.append("\nFree memory ： ").append(free).append("MiB");
        int max = (int) (Runtime.getRuntime().maxMemory() /1024 / 1024);
        builder.append("\nMax memory ： ").append(max).append("MiB");

        driverContext.getBotProvider().sendMessage(botUpdate, builder.toString(), null);
        return null;
    }
}
