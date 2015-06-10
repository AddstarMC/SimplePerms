package au.com.addstar.simpleperms.permissions;

import java.util.List;

public class PermissionGroup extends PermissionBase
{
	private String name;
	
	public PermissionGroup(String name, List<String> rawPermissions)
	{
		super(rawPermissions);
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
}
