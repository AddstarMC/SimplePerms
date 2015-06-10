package au.com.addstar.simpleperms;

import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PermissionListener implements Listener
{
	private PermissionManager manager;
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPermissionCheck(PermissionCheckEvent event)
	{	
	}
}
