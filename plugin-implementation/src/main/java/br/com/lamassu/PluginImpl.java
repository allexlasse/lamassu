package br.com.lamassu;

public class PluginImpl implements Plugin{

    public String getName() {
        return "PluginImpl";
    }

    public void start() {
        System.out.println("Executing from within a plugin... That rhimed!");
    }
    
}
