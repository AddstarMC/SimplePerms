package au.com.addstar.simpleperms.permissions;

import java.util.List;

import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.backend.IBackend;

public class PermissionGroup extends PermissionBase
{
	private String name;
	
	public PermissionGroup(String name, List<String> rawPermissions, IBackend backend, PermissionManager manager)
	{
		super(rawPermissions, backend, manager);
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getDisplayName()
	{
		return name;
	}
}
