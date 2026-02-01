package br.com.lamassu.workers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import br.com.lamassu.Message;

public class WorkerBuffer {
    BlockingQueue<Message> out = new LinkedBlockingQueue<>();

    public void push (Message message){
        out.add(message);
    }

    public Message pop(){
        return out.poll();
    }
}
