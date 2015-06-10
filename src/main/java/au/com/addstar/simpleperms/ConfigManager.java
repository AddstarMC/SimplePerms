package au.com.addstar.simpleperms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class ConfigManager
{
	private YamlConfiguration loader;
	private File configFile;
	private Configuration config;
	
	public ConfigManager(File configFile)
	{
		this.configFile = configFile;
		
		loader = (YamlConfiguration)ConfigurationProvider.getProvider(YamlConfiguration.class);
	}
	
	public void saveDefaultConfig()
	{
		if (configFile.exists())
			return;
		
		try
		{
			configFile.getParentFile().mkdirs();
			
			InputStream stream = ConfigManager.class.getResourceAsStream("/" + configFile.getName());
			Files.copy(stream, configFile.toPath());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean load()
	{
		if (!configFile.exists())
			return false;
		
		try
		{
			config = loader.load(configFile);
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public Configuration getConfig()
	{
		return config;
	}
}
