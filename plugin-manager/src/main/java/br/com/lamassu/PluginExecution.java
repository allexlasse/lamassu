package br.com.lamassu;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginExecution {
    public static void main(String[] args) {
        String path = null;
        for(int i = 0 ; i < args.length ; i++){
            if(args[i].equals("-path")){
                path = args[++i];
            }
        }
        if(path == null){
            path = "application.properties";
        }
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(path));
            Integer poolSize = Integer.valueOf(props.getProperty("thread.pool.size"));

            PluginLoader pluginLoader = new PluginLoader(props);
            pluginLoader.load();

            CopyOnWriteArraySet<Integer> pr = pluginLoader.getLoadedPluginsPriority();
            
            ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
            
            executorService.submit(()->{
                //get all priorities
                pr.forEach(pri ->{
                    try {
                        //plugins
                        List<PluginManager> pl = pluginLoader.getAllPluginsWithPririty(pri);
                        CountDownLatch latch = new CountDownLatch(pl.size());
                        for(PluginManager pw : pl){
                            Thread.ofVirtual().start(() -> {
                                try{
                                    pw.getPlugin().start();
                                }
                                finally{
                                    latch.countDown();
                                }
                            });
                        }
                        latch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                });
            });
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
