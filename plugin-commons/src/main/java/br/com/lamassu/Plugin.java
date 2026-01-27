package br.com.lamassu;

public interface Plugin {
    
    public String getName();

    default void load(){};

    public void start();

    default void stop(){};

}
