package au.com.addstar.simpleperms.commands;

import java.util.Arrays;

import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.permissions.PermissionBase;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

@SuppressWarnings( "deprecation" )
public class BaseCommand extends Command
{
	private PermissionManager manager;
	private UserCommands userCommands;
	private GroupCommands groupCommands;
	
	public BaseCommand(PermissionManager manager)
	{
		super("!perm", "simpleperms.manage", "!perms");
		this.manager = manager;
		
		userCommands = new UserCommands(manager);
		groupCommands = new GroupCommands(manager);
	}
	
	@Override
	public void execute( CommandSender sender, String[] args )
	{
		if (args.length == 0)
		{
			sender.sendMessage("/!perm <command> <params>...");
			return;
		}
		
		switch (args[0].toLowerCase())
		{
		case "user":
			handleObjectCommands(sender, userCommands, args);
			break;
		case "group":
			handleObjectCommands(sender, groupCommands, args);
			break;
		case "reload":
			onReload(sender);
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Unknown command " + args[0]);
			break;
		}
	}
	
	private void handleObjectCommands(CommandSender sender, ObjectCommands executor, String[] args)
	{
		if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("list")))
		{
			executor.listObjects(sender);
			return;
		}
		
		if (args[1].equalsIgnoreCase("help"))
		{
			executor.onHelp(sender);
			return;
		}
		
		try
		{
			PermissionBase object = executor.getObject(args[1]);
			executor.onExecute(sender, object, Arrays.copyOfRange(args, 2, args.length));
		}
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
	
	private void onReload(CommandSender sender)
	{
		manager.load();
		sender.sendMessage(ChatColor.GOLD + "All permissions reloaded");
	}
}
