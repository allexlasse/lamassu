package br.com.lamassu.workers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.lamassu.Message;
import br.com.lamassu.Plugin;

public class WorkerProducer implements Runnable{

    private final UUID id = UUID.randomUUID();
    private AtomicBoolean running = new AtomicBoolean(false);
    private final WorkerBuffer out;
    private final Plugin plugin;

    public WorkerProducer(WorkerBuffer out, Plugin plugin) {
        this.out = out;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        running.set(true);
        try {
            while (running.get()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void produce(Message message){
        if (message == null) return;
        System.out.println("WorkerProducer " + id + " enqueuing message " + message.getId());
        out.push(plugin.process(message));
    }

    public void stop() {
        running.set(false);
    }
}
