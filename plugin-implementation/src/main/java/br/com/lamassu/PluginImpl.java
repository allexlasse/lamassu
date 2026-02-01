package br.com.lamassu;

public class PluginImpl implements Plugin{

    public String getName() {
        return "PluginImpl";
    }

    public void start() {
        System.out.println("I'll assume this is the loading method");
    }

    @Override
    public Message process(Message data) {
        System.out.println("Processing message: " + data.getId());
        System.out.println("Processing payload: " + data.getContent());
        return data;
    }
}
