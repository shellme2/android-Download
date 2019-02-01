package com.eebbk.bfc.download.demo.basic.presenter;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/10/26.
 */
public class DefaultThreadPool {

   static{
//       sExecutorService = Executors.newFixedThreadPool(getNumCores());
       sExecutorService = new ThreadPoolExecutor(getNumCores(),Integer.MAX_VALUE,30L, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>(),Executors.defaultThreadFactory(),new ThreadPoolExecutor.CallerRunsPolicy());
    }


    private static ExecutorService sExecutorService;


    public static ExecutorService getExecutorService(){
        return sExecutorService;
    }

    private static int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if(Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch(Exception e) {
            e.printStackTrace();
            return 1;
        }
    }
}
