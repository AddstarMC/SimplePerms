package au.com.addstar.simpleperms;

import java.util.Map;
import java.util.UUID;

import au.com.addstar.simpleperms.backend.IBackend;
import au.com.addstar.simpleperms.backend.MySQLBackend;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import au.com.addstar.simpleperms.permissions.PermissionUser;

import com.google.common.cache.Cache;

public class PermissionManager
{
	private IBackend backend;
	private PermsPlugin plugin;
	
	private Cache<UUID, PermissionUser> cachedUsers;
	private Map<String, PermissionGroup> groups;
	
	public PermissionManager(PermsPlugin plugin)
	{
		this.plugin = plugin;
		
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
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public PermissionUser getUser(UUID id)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public PermissionGroup getGroup(String name)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
