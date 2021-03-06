package com.stratio.mesos;

import com.stratio.mesos.api.ExhibitorApi;
import com.stratio.mesos.api.MarathonApi;
import com.stratio.mesos.api.MesosApi;

import java.util.Arrays;

/**
 * Created by alonso on 30/06/17.
 */
public class CLI {

    // mesos resources unreserve
    public static void unreserve(MesosApi mesos, String principal, String role, String serviceName, boolean active) {
        String[] frameworkIds = mesos.findFrameworkId(serviceName, role, principal, active).orElse(new String[]{});
        println("Found " + frameworkIds.length + " frameworks");

        // resources unreserve
        for (String frameworkId : frameworkIds) {
            boolean teardown = mesos.teardown(frameworkId);
            println("Teardown " + frameworkId + " returned " + teardown);
            Arrays.stream(
                    mesos.findSlavesForFramework(frameworkId).orElse(new String[]{}))
                    .forEach(slaveId -> {
                        System.out.println("Looking for resources on slave " + slaveId);
                        String[] resources = mesos.findResourcesFor(role, slaveId);
                        for (String resource : resources) {
                            int code = mesos.unreserveResourceFor(slaveId, resource);
                            System.out.println("Janithor " + resource + ": " + code);
                        }
                    });
        }
    }

    // Mesos framework teardown
    public static void teardown(MesosApi mesos, String principal, String role, String serviceName) {
        String[] frameworkIds = mesos.findFrameworkId(serviceName, role, principal).orElse(new String[]{});
        println("Found " + frameworkIds.length + " frameworks");
        for (String frameworkId : frameworkIds) {
            boolean teardown = mesos.teardown(frameworkId);
            println("Teardown "+frameworkId+" returned " + teardown);
        }
    }

    // Marathon service destroy
    public static void destroy(MarathonApi marathon, String serviceName) {
        boolean destroy = marathon.destroy(serviceName);
        System.out.println("Marathon service "+serviceName+" shutdown is " + destroy);
    }

    // transform ppal, role and framework into FrameworkId
    public static void lookup(MesosApi mesos, String principal, String role, String serviceName) {
        String[] frameworkIds = mesos.findFrameworkId(serviceName, role, principal).orElse(new String[]{});
        println("Found " + frameworkIds.length + " frameworks");
        for (String frameworkId : frameworkIds) {
            println(frameworkId);
        }
    }

    // list all resources
    public static void resources(MesosApi mesos, String principal, String role, String serviceName, boolean active) {
        String[] frameworkIds = mesos.findFrameworkId(serviceName, role, principal, active).orElse(new String[]{});
        println("Found " + frameworkIds.length + " frameworks");

        // resources unreserve
        for (String frameworkId : frameworkIds) {
            Arrays.stream(
                    mesos.findSlavesForFramework(frameworkId).orElse(new String[]{}))
                    .forEach(slaveId -> {
                        println("\nResources on slave " + slaveId);
                        String[] resources = mesos.findResourcesFor(role, slaveId);
                        for (String resource : resources) {
                            println(resource);
                        }
                    });
        }
    }

    // exhibitor znode cleanup
    public static void cleanup(ExhibitorApi exhibitor, String serviceName) {
        boolean deleted = exhibitor.delete(serviceName);
        println("Service " + serviceName + " was " + (deleted?"deleted successfully":"not deleted"));
    }

    public static String dcosToken(String url, String user, String pass) {
        String token = MarathonApi.obtainToken(user, pass, url);
        println(token);
        return token;
    }

    // TODO: add proper log
    private static void println(String message) {
        System.out.println(message);
    }
}
