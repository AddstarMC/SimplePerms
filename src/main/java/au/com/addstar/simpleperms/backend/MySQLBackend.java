package au.com.addstar.simpleperms.backend;

import java.util.List;
import java.util.UUID;

import au.com.addstar.simpleperms.permissions.PermissionBase;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import au.com.addstar.simpleperms.permissions.PermissionUser;

public class MySQLBackend implements IBackend
{
	public void initialize()
	{
		// TODO: Take config as argument
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public List<PermissionGroup> loadAllGroups()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public PermissionGroup loadGroup(String groupName)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public PermissionUser loadUser(UUID userId)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public void save(PermissionBase object)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public void shutdown()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
