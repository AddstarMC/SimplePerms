package au.com.addstar.simpleperms.commands;

import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.permissions.PermissionBase;

public class GroupCommands extends ObjectCommands
{
	public GroupCommands(PermissionManager manager)
	{
		super(manager);
	}
	
	@Override
	public String getName()
	{
		return "group";
	}

	@Override
	public PermissionBase getObject( String value ) throws IllegalArgumentException
	{
		return manager.getOrCreateGroup(value);
	}
}
