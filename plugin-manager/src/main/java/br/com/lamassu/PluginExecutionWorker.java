package br.com.lamassu;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.lamassu.workers.Worker;
import br.com.lamassu.workers.WorkerConsumer;
import br.com.lamassu.workers.WorkerProducer;
import br.com.lamassu.workers.WorkerBuffer;

public class PluginExecutionWorker {
    //here is how it should work
    // 1 - a pool of workers of each type
    // 2 - the first (lowest) priority plugin should only have producers workers
    // 3 - the last (highest) priority plugin should only have consumer workers
    // 4 - each plugin in the middle should have a "plain" worker pool
    // information is passed down from one plugin to the next through each plugin manager buffer
    // all pools should have a fixed size

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
            int poolSize = Integer.parseInt(props.getProperty("thread.pool.size", "4"));

            PluginLoader pluginLoader = new PluginLoader(props);
            pluginLoader.load();
            CopyOnWriteArraySet<Integer> prSet = pluginLoader.getLoadedPluginsPriority();

            // sort priorities ascending
            List<Integer> priorities = new ArrayList<>(prSet);
            Collections.sort(priorities);

            if (priorities.isEmpty()) {
                System.out.println("No plugins loaded");
                return;
            }

            // create stage buffers, one per priority (messages flow from stage[i] -> stage[i+1])
            Map<Integer, WorkerBuffer> stageBuffers = new HashMap<>();
            for (Integer p : priorities) {
                stageBuffers.put(p, new WorkerBuffer());
            }

            // start plugins
            for (Integer p : priorities) {
                List<PluginManager> plugins = pluginLoader.getAllPluginsWithPririty(p);
                for (PluginManager pm : plugins) {
                    pm.getPlugin().start();
                }
            }

            // keep references so we can shut down later
            Map<Integer, ExecutorService> executors = new HashMap<>();
            Map<String, WorkerProducer> producers = new HashMap<>();

            // build worker pools for each priority
            for (int i = 0; i < priorities.size(); i++) {
                Integer p = priorities.get(i);
                List<PluginManager> plugins = pluginLoader.getAllPluginsWithPririty(p);

                WorkerBuffer inBuffer = stageBuffers.get(p);
                WorkerBuffer outBuffer = (i + 1 < priorities.size()) ? stageBuffers.get(priorities.get(i + 1)) : null;

                if (i == 0) {
                    // first priority: only producers
                    for (PluginManager pm : plugins) {
                        WorkerProducer wp = new WorkerProducer(inBuffer, pm.getPlugin());
                        producers.put(pm.getName(), wp);
                        // Producers don't need to run, but we could submit them if desired:
                        // executor may be used for producers if you want them as threads
                    }
                } else if (i == priorities.size() - 1) {
                    // last priority: only consumers
                    ExecutorService exec = Executors.newFixedThreadPool(poolSize);
                    executors.put(p, exec);
                    for (PluginManager pm : plugins) {
                        for (int t = 0; t < poolSize; t++) {
                            exec.submit(new WorkerConsumer(inBuffer, pm.getPlugin()));
                        }
                    }
                } else {
                    // middle priorities: plain workers
                    ExecutorService exec = Executors.newFixedThreadPool(poolSize);
                    executors.put(p, exec);
                    for (PluginManager pm : plugins) {
                        for (int t = 0; t < poolSize; t++) {
                            exec.submit(new Worker(inBuffer, outBuffer, pm.getPlugin()));
                        }
                    }
                }
            }

            // Demo: produce a message into the first available producer (API would do this)
            if (!producers.isEmpty()) {
                WorkerProducer anyProducer = producers.values().iterator().next();
                Message demo = new Message("demo-1", "Hello World");
                anyProducer.produce(demo);
                System.out.println("Demo message enqueued to priority " + priorities.get(0));
            }

            // add a shutdown hook to stop executors gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down worker executors...");
                executors.values().forEach(ExecutorService::shutdownNow);
            }));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
