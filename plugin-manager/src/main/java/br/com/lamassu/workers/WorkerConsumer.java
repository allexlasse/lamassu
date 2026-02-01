package br.com.lamassu.workers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.lamassu.Message;
import br.com.lamassu.Plugin;

public class WorkerConsumer implements Runnable{
    
    private final UUID id = UUID.randomUUID();
    private AtomicBoolean running = new AtomicBoolean(false);
    private final WorkerBuffer in;
    private final Plugin plugin;

    public WorkerConsumer(WorkerBuffer in, Plugin plugin) {
        this.in = in;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        running.set(true);
        try {
            while(running.get()){
                Message message = in.pop();
                if(message != null){
                    //no out buffer
                    System.out.println("WorkerConsumer " + id + " consuming message " + message.getId());
                    plugin.process(message);
                } else {
                    Thread.sleep(50);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running.set(false);
    }
}
