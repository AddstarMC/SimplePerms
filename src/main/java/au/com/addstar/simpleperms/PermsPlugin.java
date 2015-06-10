package au.com.addstar.simpleperms;

import java.io.File;

import net.md_5.bungee.api.plugin.Plugin;

public class PermsPlugin extends Plugin
{
	private ConfigManager configManager;
	
	@Override
	public void onEnable()
	{
		configManager = new ConfigManager(new File(getDataFolder(), "config.yml"));
		configManager.saveDefaultConfig();
		
		if (!configManager.load())
			getLogger().warning("The configuration file failed to load! This will not work");
	}
	
	public ConfigManager getConfigManager()
	{
		return configManager;
	}
}
