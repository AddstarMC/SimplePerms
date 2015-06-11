package au.com.addstar.simpleperms.commands;

import net.md_5.bungee.api.CommandSender;
import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.permissions.PermissionBase;
import au.com.addstar.simpleperms.permissions.PermissionGroup;

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
	
	@SuppressWarnings( "deprecation" )
	@Override
	public void listObjects( CommandSender sender )
	{
		if (manager.getGroups().isEmpty())
			sender.sendMessage("No groups are defined");
		else
		{
			int index = 1;
			for (PermissionGroup group : manager.getGroups())
				sender.sendMessage(String.format("%d) %s", index++, group.getName()));
		}
	}
}
