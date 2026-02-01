package br.com.lamassu;

public class PluginManager {
    
    private final String name;
    private final Integer priority;
    private final Plugin plugin;

    public PluginManager(String name, Integer priority, Plugin plugin) {
        this.name = name;
        this.priority = priority;
        this.plugin = plugin;
    }

    public String getName() {
        return name;
    }

    public Integer getPriority() {
        return priority;
    }

    public Plugin getPlugin() {
        return plugin;
    }

}
