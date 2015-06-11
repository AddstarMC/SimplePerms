package au.com.addstar.simpleperms;

import au.com.addstar.simpleperms.permissions.PermissionUser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PermissionListener implements Listener
{
	private PermissionManager manager;
	
	public PermissionListener(PermissionManager manager)
	{
		this.manager = manager;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerJoin(final LoginEvent event)
	{
		// Load the player early
		event.registerIntent(manager.getPlugin());
		
		ProxyServer.getInstance().getScheduler().runAsync(manager.getPlugin(), new Runnable()
		{
			@Override
			public void run()
			{
				manager.loadUser(event.getConnection());
				event.completeIntent(manager.getPlugin());
			}
		});
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPermissionCheck(PermissionCheckEvent event)
	{
		if (!(event.getSender() instanceof ProxiedPlayer))
			return;
		
		ProxiedPlayer player = (ProxiedPlayer)event.getSender();
			
		PermissionUser user = manager.getUser(player.getUniqueId());
		event.setHasPermission(user.hasPermission(event.getPermission()));
	}
}
