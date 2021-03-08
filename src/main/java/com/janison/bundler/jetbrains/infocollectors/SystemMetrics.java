package com.janison.bundler.jetbrains.infocollectors;

import com.intellij.openapi.application.ApplicationInfo;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OSSession;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class SystemMetrics {


    /***
     * Gets general metrics about the application
     * @return
     */
    public Map<String, Double> getMetrics(){

        Map<String, Double> baseMetrics = new HashMap<String, Double>();

        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        Sensors sensors = hardwareAbstractionLayer.getSensors();

        //Lets use this
        baseMetrics.put( "cpu%", (double)centralProcessor.getSystemCpuLoadTicks()[2]);

        double cpuFrequency = Arrays.stream(centralProcessor.getCurrentFreq()).reduce(0, Long::sum);

        //Just leaving this here for now
        baseMetrics.put( "cpuSpeed%", cpuFrequency);
        baseMetrics.put( "cpuSpeedGHz", hzToGigaHz(centralProcessor.getMaxFreq()));
        baseMetrics.put( "memMB", bytesToMeg(hardwareAbstractionLayer.getMemory().getTotal()));
        baseMetrics.put( "mem%", getMemPercent(hardwareAbstractionLayer));
        baseMetrics.put( "temp", sensors.getCpuTemperature() );

        printDisks(hardwareAbstractionLayer.getDiskStores());

//        baseMetrics.put( "disk.r/sec", hardwareAbstractionLayer.getd );
//        baseMetrics.put( "disk.w/sec", sys.GetAvgDiskWritePerSec() );
//        baseMetrics.put( "disk.r%", sys.GetPercentDiskReadTime() );
//        baseMetrics.put( "disk.w%", sys.GetPercentDiskWriteTime() );
//        baseMetrics.put( "disk.free%", sys.GetPercentDiskFreeSpace() );
//        Going to reuse these so that we can do a direct compare

        try {
            int pid = getRiderPid();
            //talk to tyson about this one - CLR exceptions may not be able to be used.
            //baseMetrics.put( "rider.ex#", );
            baseMetrics.put( "rider.memMB", (double)bytesToMeg(getMemoryUtilizationPerProcess(pid)));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return  baseMetrics;
    }


    /***
     * Gets general properties about the executing environment.
     * @return Map of environment properties for telemetry ingestion
     */
    public Map<String, String> getEnvProperties(Map<String, String> extras){

        Map<String, String> envProp = new HashMap<String,String>();

        String riderversion = ApplicationInfo.getInstance().getBuild().asString();

        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();

        envProp.put("processorCount", String.valueOf(centralProcessor.getLogicalProcessorCount()));
        envProp.put("is64bit", String.valueOf(operatingSystem.getBitness() == 64));
        envProp.put("osVersion", operatingSystem.getVersionInfo().getCodeName());
        envProp.put("riderVersion", riderversion);
        envProp.put("workstation", operatingSystem.getNetworkParams().getHostName());

        String possibleUserName = System.getProperty("user.name");
        List<String> possibleUserNames = operatingSystem.getSessions()
                                                        .stream()
                                                        .map(OSSession::getUserName)
                                                        .collect(Collectors.toList());

        envProp.put("username", determineLoggedInUserName(possibleUserNames,possibleUserName, systemInfo));

        if(extras.size() > 0)   {
            envProp.putAll(extras);
        }

        return  envProp;
    }


    /***
     * Filters to the most likely candidate username. Theres some complicated logic going on so take a read at the comment.
     * @param possibleUserNames
     * @param possibleUserName
     * @return
     */
    public String determineLoggedInUserName(List<String> possibleUserNames, String possibleUserName, SystemInfo systemInfo)
    {
        /*
            Complicated logic here *ugh* so let me explain to future me/person reading this. Java's System.getProperty("user.name")
            is not platform specific but more than that, its actually not that secure. it can be spoofed, it can also
            be null, and it could actually invoke some exceptions.

            Sooooo what do we do?

            Well OSHI can provide us a list of currently logged in users. this is great if we want this tool to be independent of
            operating system one day (hopes of mac and windows development).

            the caveat is, it doesn't tell us whom is actually logged in running the code, so there might be more than one.
        */

        /*
            Step 1.
            Grab out the users from oshi and compare them to our stored system.getProperty. if it doesnt exist in the list, move on.
         */
        if(possibleUserNames.contains(possibleUserName)){
            return possibleUserName;
        }

        /*
            Step 2.
            Grab a unique count of processes and username. We want this to determine who is most LIKELY to be logged in.
         */
        Map<String, Long> users = systemInfo.getOperatingSystem()
                                        .getProcesses()
                                        .stream()
                                        .map(OSProcess::getUser)
                                        .collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                                        .entrySet()
                                        .stream()
                                        .sorted(Map.Entry.comparingByValue())
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return users.entrySet().iterator().next().getKey();
    }

    private static void printDisks(List<HWDiskStore> diskStores) {

        StringBuilder oshi = new StringBuilder();

        oshi.append("Disks:");
        for (HWDiskStore disk : diskStores) {
            boolean readwrite = disk.getReads() > 0 || disk.getWrites() > 0;
            oshi.append(String.format(
                    " %s: (model: %s - S/N: %s) size: %s, reads: %s (%s), writes: %s (%s), xfer: %s ms%n",
                    disk.getName(), disk.getModel(), disk.getSerial(),
                    disk.getSize() > 0 ? FormatUtil.formatBytesDecimal(disk.getSize()) : "?",
                    readwrite ? disk.getReads() : "?",
                    readwrite ? FormatUtil.formatBytes(disk.getReadBytes()) : "?",
                    readwrite ? disk.getWrites() : "?",
                    readwrite ? FormatUtil.formatBytes(disk.getWriteBytes()) : "?",
                    readwrite ? disk.getTransferTime() : "?"));
            List<HWPartition> partitions = disk.getPartitions();
            if (partitions == null) {
                // TODO Remove when all OS's implemented
                continue;
            }
            for (HWPartition part : partitions) {
                oshi.append(String.format(" |-- %s: %s (%s) Maj:Min=%d:%d, size: %s%s%n",
                        part.getIdentification(), part.getName(), part.getType(), part.getMajor(),
                        part.getMinor(), FormatUtil.formatBytesDecimal(part.getSize()),
                        part.getMountPoint().isEmpty() ? "" : " @ " + part.getMountPoint()));
            }
        }
    }

    public static double getMemPercent(HardwareAbstractionLayer hardwareAbstractionLayer){
        double totalMemory = (double)hardwareAbstractionLayer.getMemory().getTotal();
        double totalAvailable = (double)hardwareAbstractionLayer.getMemory().getAvailable();
        double differenceMemory = totalMemory - totalAvailable;
        return (differenceMemory/totalMemory * 100.0);
    }

    /***
     * Converts bse long bytes to mega bytes.
     * @param bytes
     * @return
     */
    public static double bytesToMeg(long bytes) {
        double bytesD = (double) bytes;
        return  Math.round((bytesD / 1024 / 1024) * 100.0) / 100.0;
    }

    /***
     * Converts base long hz to giga hz, used primarily for CPU Speed
     * @param hz
     * @return
     */
    public static double hzToGigaHz(long hz) {
        double hzD = (double) hz;
        //rounds to 2 decimal places
        return Math.round((hzD / 1024 / 1024 / 1024) * 100.0) / 100.0;
    }

    /***
     * Gets Memory Utilisation per process
     * @param pid
     * @return
     */
    public static long getMemoryUtilizationPerProcess(int pid) {
        OSProcess process;
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        process = os.getProcess(pid);
        return process.getResidentSetSize();
    }

    /***
     * Gets the rider PID using system reflection.
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     */
    public static Integer getRiderPid() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException {

        java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
        java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
        jvm.setAccessible(true);
        sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
        java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
        pid_method.setAccessible(true);

        return (Integer) pid_method.invoke(mgmt);
    }
}
