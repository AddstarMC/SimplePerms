package au.com.addstar.simpleperms;

import java.io.File;

import au.com.addstar.simpleperms.commands.BaseCommand;
import net.md_5.bungee.api.plugin.Plugin;

public class PermsPlugin extends Plugin
{
	private ConfigManager configManager;
	private PermissionManager permManager;
	
	@Override
	public void onEnable()
	{
		configManager = new ConfigManager(new File(getDataFolder(), "config.yml"));
		configManager.saveDefaultConfig();
		
		if (!configManager.load())
			getLogger().warning("The configuration file failed to load! This will not work");
		
		permManager = new PermissionManager(this);
		permManager.load();
		
		getProxy().getPluginManager().registerListener(this, new PermissionListener(permManager));
		getProxy().getPluginManager().registerCommand(this, new BaseCommand(permManager));
	}
	
	@Override
	public void onDisable()
	{
		permManager.shutdown();
	}
	
	public ConfigManager getConfigManager()
	{
		return configManager;
	}
	
	public PermissionManager getPermissionManager()
	{
		return permManager;
	}
}
