package au.com.addstar.simpleperms.permissions;

import java.util.List;
import java.util.Map;

public abstract class PermissionBase
{
	private String name;
	
	private List<String> rawPermissions;
	
	private Map<String, Boolean> staticPermissions;
	private Map<String, Boolean> dynamicPermissions;
	
	public String getName()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public boolean hasPermission(String permission)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public boolean hasLocalPermission(String permission)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public void rebuildPermissions()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public List<PermissionGroup> parents()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public void save()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public List<String> getRawPermissions()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
