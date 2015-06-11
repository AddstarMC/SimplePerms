package au.com.addstar.simpleperms.commands;

import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.permissions.PermissionBase;

public class UserCommands extends ObjectCommands
{
	public UserCommands(PermissionManager manager)
	{
		super(manager);
	}
	
	@Override
	public String getName()
	{
		return "user";
	}

	@Override
	public PermissionBase getObject( String value ) throws IllegalArgumentException
	{
		UUID id;
		try
		{
			id = UUID.fromString(value);
		}
		catch (IllegalArgumentException e)
		{
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(value);
			if (player != null)
				id = player.getUniqueId();
			else
				throw new IllegalArgumentException("Unknown player " + value + " (NOTE. Offline players are not supported by name)");
		}
		
		return manager.getUser(id);
	}
}
