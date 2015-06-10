package au.com.addstar.simpleperms;

import java.util.Map;
import java.util.UUID;

import au.com.addstar.simpleperms.backend.IBackend;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import au.com.addstar.simpleperms.permissions.PermissionUser;

import com.google.common.cache.Cache;

public class PermissionManager
{
	private IBackend backend;
	
	private Cache<UUID, PermissionUser> cachedUsers;
	private Map<String, PermissionGroup> groups;
	
	public void initialize()
	{
		throw new UnsupportedOperationException("Not yet implemented");
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
