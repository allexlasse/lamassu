package br.com.lamassu;

public interface Plugin {
    
    public String getName();

    public void start();

    public Message process(Message data);

    default void stop(){};

}
