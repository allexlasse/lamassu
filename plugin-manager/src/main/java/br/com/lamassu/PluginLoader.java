package br.com.lamassu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class PluginLoader {

    //TODO
    // Live reload
   
    private Properties props;
    private CopyOnWriteArrayList<PluginWrapper> loadedPlugins;
    private CopyOnWriteArraySet<Integer> loadedPluginsPriority;

    public PluginLoader(Properties props) throws FileNotFoundException, IOException {
        this.props = props;
        this.loadedPlugins = new CopyOnWriteArrayList<>();
        this.loadedPluginsPriority = new CopyOnWriteArraySet<>();
    }

    @SuppressWarnings("unchecked")
    public void load() throws MalformedURLException, IOException {
        //get the plugin folder, but don't iterate over it
        String fPath = props.getProperty("plugin.folder");
        //search for the first plugin... Should be "plugin.priority.X", where X is its priority. And I should automatically look for every prop with the prefix "plugin.priority"
        Enumeration<String> pluginsToLoad = (Enumeration<String>) props.propertyNames();
        while(pluginsToLoad.hasMoreElements()){
            String propName = pluginsToLoad.nextElement();
            String propVal = props.getProperty(propName);
            Integer priority = null;
            if(propName.contains("plugin.priority")){
                priority = Integer.valueOf(propName.split("\\.")[2]);
                //get the jar file as a... well... File
                String fullPath = fPath + File.separator + propVal;
                File plugin = new File(fullPath);
                if(plugin.exists()){
                    loadPlugin(plugin, priority);
                    this.loadedPluginsPriority.add(priority);
                }
            }
        }
    }

    private void loadPlugin(File plugin, Integer priority) throws MalformedURLException{
        ServiceLoader<Plugin> serviceLoader = getServiceLoader(getClassLoader(plugin));
        for (Plugin p : serviceLoader){
            String pluginName = p.getName();
            if(!pluginWasLoaded(pluginName)){
                loadedPlugins.add(new PluginWrapper(pluginName, priority, p));
                System.out.println("Loaded plugin " + pluginName);
            }
            System.out.println("Plugin was already loaded");
        }
    }

    public CopyOnWriteArraySet<Integer> getLoadedPluginsPriority(){
        return this.loadedPluginsPriority;
    }

    private Boolean pluginWasLoaded(String name){
        return getPluginByName(name) != null;
    }

    public PluginWrapper getPluginByName(String name){
        return loadedPlugins.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    public PluginWrapper getPluginByPriority(Integer priority){
        return loadedPlugins.stream().filter(p -> p.getPriority().equals(priority)).findFirst().orElse(null);
    }

    public List<PluginWrapper> getAllPluginsWithPririty(Integer priority){
        return loadedPlugins.stream().filter(p -> p.getPriority().equals(priority)).collect(Collectors.toList());
    }

    //ClassLoader to... well... Load the classes from the specified jar file
    private URLClassLoader getClassLoader(File plugin) throws MalformedURLException{
        return new URLClassLoader(new URL[]{plugin.toURI().toURL()}, ClassLoader.getSystemClassLoader());    
    }

    //Service loader should look up the manifest to know how to "find" our class that implements Plugin interface
    private  ServiceLoader<Plugin> getServiceLoader (URLClassLoader classLoader){
        return ServiceLoader.load(Plugin.class, classLoader);
    }
}
