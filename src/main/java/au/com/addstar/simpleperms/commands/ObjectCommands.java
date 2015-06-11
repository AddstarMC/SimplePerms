package au.com.addstar.simpleperms.commands;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.permissions.PermissionBase;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@SuppressWarnings( "deprecation" )
public abstract class ObjectCommands
{
	protected final PermissionManager manager;
	
	public ObjectCommands(PermissionManager manager)
	{
		this.manager = manager;
	}
	
	public abstract String getName();
	
	public abstract PermissionBase getObject(String value) throws IllegalArgumentException;
	
	public abstract void listObjects(CommandSender sender);
	
	private void displayUsage(CommandSender sender, PermissionBase object, String... parts)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("/!perm ");
		builder.append(getName());
		builder.append(' ');
		builder.append(object.getDisplayName());
		
		for (String part : parts)
		{
			builder.append(' ');
			builder.append(part);
		}
		
		sender.sendMessage(builder.toString());
	}
	
	public final void onExecute(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length == 0)
		{
			onFullList(sender, object);
			return;
		}
		
		switch (args[0].toLowerCase())
		{
		case "check":
			onCheck(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "delete":
			onDelete(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "list":
			onList(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "add":
			onAdd(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "remove":
			onRemove(sender, object, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "group":
		case "parent":
			onParent(sender, object, args[0].toLowerCase(), Arrays.copyOfRange(args, 1, args.length));
			break;
		case "help":
			onHelp(sender);
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Unknown sub command " + args[0]);
			break;
		}
	}
	
	private void onFullList(CommandSender sender, PermissionBase object)
	{
		sender.sendMessage(String.format("Parents for %s:", object.getDisplayName()));
		
		int index = 1;
		for (PermissionGroup parent : object.parents())
			sender.sendMessage(String.format("%d) %s", index++, parent.getName()));
		
		sender.sendMessage(String.format("Permissions defined on %s:", object.getDisplayName()));
		
		index = 1;
		for (String permission : object.getRawPermissions())
			sender.sendMessage(String.format("%d) %s", index++, permission));
	}
	
	private void onCheck(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, "check <permission>");
			return;
		}
		
		Boolean value = object.getLocalPermission(args[0]);
		boolean local = true;
		if (value == null)
		{
			local = false;
			value = object.getPermission(args[0]);
		}
		
		
		if (value == null)
			sender.sendMessage(String.format("%s doesnt have permission \"%s\" defined (inherited)", object.getDisplayName(), args[0]));
		else
		{
			if (local)
				sender.sendMessage(String.format("%s has \"%s\" = %s (self)", object.getDisplayName(), args[0], value.toString().toUpperCase()));
			else
				sender.sendMessage(String.format("%s has \"%s\" = %s (inherited)", object.getDisplayName(), args[0], value.toString().toUpperCase()));
		}
	}
	
	private void onDelete(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length != 0)
		{
			displayUsage(sender, object, "delete");
			return;
		}
		
		try
		{
			manager.remove(object);
			sender.sendMessage(ChatColor.GOLD + object.getDisplayName() + " has been removed");
		}
		catch (IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + "Cannot remove this player as they are still online");
		}
	}
	
	private void onList(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length > 1)
		{
			displayUsage(sender, object, "list [<page>]");
			return;
		}
		
		int perPage = Integer.MAX_VALUE;
		if (sender instanceof ProxiedPlayer)
			perPage = 15;
		
		// Parse page number
		int page = 0;
		if (args.length == 1)
		{
			try
			{
				page = Integer.parseInt(args[0]);
				if (page <= 0)
				{
					sender.sendMessage(ChatColor.RED + "Page number must be 1 or higher");
					return;
				}
				
				--page;
			}
			catch (NumberFormatException e)
			{
				displayUsage(sender, object, "list [<page>]");
				return;
			}
		}
		
		// Display perms
		List<String> perms = object.getRawPermissions();
		int start = page * perPage;
		
		if (start >= perms.size() && start != 0)
			sender.sendMessage(ChatColor.RED + "Page number too high");
		else
		{
			sender.sendMessage(String.format("Permissions defined on %s:", object.getDisplayName()));
			
			for (int i = start; i < perms.size() && i < start + perPage; ++i)
				sender.sendMessage(String.format("%d) %s", i+1, perms.get(i)));
			
			// Show that there are more results
			if (perms.size() > start + perPage)
				sender.sendMessage("...");
		}
	}
	
	private void onAdd(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, "add <permission>");
			return;
		}
		
		object.addPermission(args[0]);
		object.rebuildPermissions();
		
		sender.sendMessage(ChatColor.GREEN + args[0] + " was added to " + object.getDisplayName());
	}
	
	private void onRemove(CommandSender sender, PermissionBase object, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, "remove <permission>");
			return;
		}

		object.removePermission(args[0]);
		object.rebuildPermissions();
		
		sender.sendMessage(ChatColor.GREEN + args[0] + " was removed from " + object.getDisplayName());
	}
	
	public void onHelp(CommandSender sender)
	{
		String prefix = "/!perm " + getName() + ChatColor.RED + " <" + getName() + "> " + ChatColor.GOLD;
		
		sender.sendMessage(ChatColor.WHITE + "All sub commands for " + getName());
		sender.sendMessage(ChatColor.GOLD + prefix);
		sender.sendMessage(ChatColor.AQUA + "   Lists all parents and permissions defined on this object");
		sender.sendMessage(ChatColor.GOLD + prefix + "add " + ChatColor.RED + "<permission>");
		sender.sendMessage(ChatColor.AQUA + "   Adds a permission to this object directly");
		sender.sendMessage(ChatColor.GOLD + prefix + "remove " + ChatColor.RED + "<permission>");
		sender.sendMessage(ChatColor.AQUA + "   Removes a permission from this object directly");
		sender.sendMessage(ChatColor.GOLD + prefix + "check " + ChatColor.RED + "<permission>");
		sender.sendMessage(ChatColor.AQUA + "   Checks if that permission is set for this object");
		sender.sendMessage(ChatColor.GOLD + prefix + "list");
		sender.sendMessage(ChatColor.AQUA + "   Lists all permissions that are set directly on this object");
		sender.sendMessage(ChatColor.GOLD + prefix + "delete");
		sender.sendMessage(ChatColor.AQUA + "   Deletes this object. If this is a user they must be offline");
		sender.sendMessage(ChatColor.GOLD + prefix + "parent add " + ChatColor.RED + "<group>");
		sender.sendMessage(ChatColor.AQUA + "   Adds the group to this object if possible");
		sender.sendMessage(ChatColor.GOLD + prefix + "parent remove " + ChatColor.RED + "<group>");
		sender.sendMessage(ChatColor.AQUA + "   Removes the group from this user");
		sender.sendMessage(ChatColor.GOLD + prefix + "parent list");
		sender.sendMessage(ChatColor.AQUA + "   Lists all parents of this object");
		sender.sendMessage(ChatColor.GOLD + prefix + "parent set " + ChatColor.RED + "<group> <group2>...");
		sender.sendMessage(ChatColor.AQUA + "   Sets the groups this object will inherit.");
	}
	
	// ====================================
	//           Parent Commands
	// ====================================
	
	private void onParent(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length == 0)
		{
			displayUsage(sender, object, label, "<command> [<params>...]");
			return;
		}
		
		switch (args[0].toLowerCase())
		{
		case "list":
			onParentList(sender, object, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "add":
			onParentAdd(sender, object, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "remove":
			onParentRemove(sender, object, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		case "set":
			onParentSet(sender, object, label, Arrays.copyOfRange(args, 1, args.length));
			break;
		default:
			sender.sendMessage(ChatColor.RED + "Unknown sub command " + args[0]);
			break;
		}
	}
	
	private void onParentAdd(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, label, "add <parent>");
			return;
		}
		
		PermissionGroup toAdd = manager.getGroup(args[0]);
		if (toAdd == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown group " + args[0]);
			return;
		}
		
		// Check that it doesnt already have this parent
		if (object.hasParent(toAdd))
		{
			sender.sendMessage(ChatColor.RED + object.getDisplayName() + " already inherits from " + toAdd.getName());
			return;
		}
		
		// Make sure we wont add a cycle
		if (object instanceof PermissionGroup && toAdd.hasParent((PermissionGroup)object))
		{
			sender.sendMessage(ChatColor.RED + "Adding that parent would create a cycle.");
			return;
		}
		
		object.addParent(toAdd);
		sender.sendMessage(ChatColor.GREEN + object.getDisplayName() + " now inherits from " + toAdd.getName());
	}
	
	private void onParentList(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length != 0)
		{
			displayUsage(sender, object, label, "list");
			return;
		}

		sender.sendMessage(String.format("Parents for %s:", object.getDisplayName()));
		
		int index = 1;
		for (PermissionGroup parent : object.parents())
			sender.sendMessage(String.format("%d) %s", index++, parent.getName()));
	}
	
	private void onParentRemove(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length != 1)
		{
			displayUsage(sender, object, label, "remove <parent>");
			return;
		}
		
		if (args.length != 1)
		{
			displayUsage(sender, object, label, "add <parent>");
			return;
		}
		
		PermissionGroup toRemove = manager.getGroup(args[0]);
		if (toRemove == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown group " + args[0]);
			return;
		}
		
		object.removeParent(toRemove);
		sender.sendMessage(ChatColor.GOLD + toRemove.getDisplayName() + " is not longer a parent of " + object.getDisplayName());
	}
	
	private void onParentSet(CommandSender sender, PermissionBase object, String label, String[] args)
	{
		if (args.length == 0)
		{
			displayUsage(sender, object, label, "set <parent> [<parent>...]");
			return;
		}
		
		// Parse groups
		List<PermissionGroup> groups = Lists.newArrayList();
		for (String groupName : args)
		{
			PermissionGroup group = manager.getGroup(groupName);
			if (group == null)
			{
				sender.sendMessage(ChatColor.RED + "Unknown group " + groupName);
				return;
			}
			groups.add(group);
		}
		
		object.setParents(groups);
		sender.sendMessage(ChatColor.GOLD + object.getDisplayName() + " now inherits from " + groups);
	}
}
