package au.com.addstar.simpleperms;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import au.com.addstar.simpleperms.backend.IBackend;
import au.com.addstar.simpleperms.backend.MySQLBackend;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import au.com.addstar.simpleperms.permissions.PermissionUser;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

public class PermissionManager
{
	private IBackend backend;
	private PermsPlugin plugin;
	
	private Cache<UUID, PermissionUser> cachedUsers;
	private Map<String, PermissionGroup> groups;
	
	public PermissionManager(PermsPlugin plugin)
	{
		this.plugin = plugin;
		
		cachedUsers = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
		groups = Maps.newHashMap();
		
		backend = new MySQLBackend(plugin.getConfigManager().getConfig(), plugin.getLogger());
		if (!backend.isValid())
			backend = null;
	}
	
	public void shutdown()
	{
		if (backend != null)
			backend.shutdown();
	}
	
	public void load()
	{
		if (backend == null)
			return;
		
		groups = backend.loadAllGroups();
		plugin.getLogger().info("Loaded " + groups.size() + " permission groups");
	}
	
	public PermissionUser getUser(UUID id)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public PermissionGroup getGroup(String name)
	{
		return groups.get(name.toLowerCase());
	}
}
