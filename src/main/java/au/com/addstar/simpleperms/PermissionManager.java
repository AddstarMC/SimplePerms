package au.com.addstar.simpleperms;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import au.com.addstar.simpleperms.backend.IBackend;
import au.com.addstar.simpleperms.backend.MySQLBackend;
import au.com.addstar.simpleperms.permissions.PermissionBase;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import au.com.addstar.simpleperms.permissions.PermissionUser;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PermissionManager
{
	private IBackend backend;
	private PermsPlugin plugin;
	
	private Cache<UUID, PermissionUser> cachedUsers;
	private Map<String, PermissionGroup> groups;
	private PermissionGroup defaultGroup;
	
	public PermissionManager(PermsPlugin plugin)
	{
		this.plugin = plugin;
		
		cachedUsers = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
		groups = Maps.newHashMap();
		
		backend = new MySQLBackend(plugin.getConfigManager().getConfig(), plugin.getLogger(), this);
		if (!backend.isValid())
			backend = null;
	}
	
	public void shutdown()
	{
		if (backend != null)
			backend.shutdown();
	}
	
	public void load()
	{
		if (backend == null)
			return;
		
		groups = backend.loadAllGroups();
		for (PermissionGroup group : groups.values())
			group.rebuildPermissions();
		
		if (groups.containsKey("default"))
		{
			defaultGroup = groups.get("default");
			if (!defaultGroup.parents().isEmpty())
			{
				plugin.getLogger().warning("default group has parents " + defaultGroup.parents());
				defaultGroup.setParents(Collections.<PermissionGroup>emptyList());
			}
		}
		else
			defaultGroup = new PermissionGroup("default", Lists.<String>newArrayList(), backend, this);
		
		// We need to reload any users that are online
		cachedUsers.invalidateAll();
		for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers())
			loadUser(player.getPendingConnection());
	}
	
	public void loadUser(PendingConnection connection)
	{
		loadUser(connection.getUniqueId(), connection.getName());
	}
	
	private PermissionUser loadUser(UUID id, String name)
	{
		PermissionUser user = backend.loadUser(id, name);
		List<String> parents = backend.loadParents(id);
		List<PermissionGroup> parentGroups = Lists.newArrayListWithCapacity(parents.size());
		
		for (String parentName : parents)
		{
			PermissionGroup parent = groups.get(parentName.toLowerCase());
			if (parent == null)
				continue;
			
			parentGroups.add(parent);
		}
		
		user.setParentsInternal(parentGroups);
		user.rebuildPermissions();
		
		// Cache it
		cachedUsers.put(id, user);
		
		plugin.getLogger().info("Loaded player " + id);
		
		return user;
	}
	
	public PermissionUser getUser(UUID id)
	{
		PermissionUser user = cachedUsers.getIfPresent(id);
		if (user != null)
			return user;
		
		user = loadUser(id, null);
		
		return user;
	}
	
	public UUID findUserByName(String name)
	{
		return backend.findUser(name);
	}
	
	public PermissionGroup getGroup(String name)
	{
		return groups.get(name.toLowerCase());
	}
	
	public PermissionGroup getOrCreateGroup(String name)
	{
		PermissionGroup group = groups.get(name.toLowerCase());
		if (group != null)
			return group;
		
		group = new PermissionGroup(name, Lists.<String>newArrayList(), backend, this);
		groups.put(name.toLowerCase(), group);
		
		backend.addObject(group);
		return group;
	}
	
	public PermissionGroup getDefaultGroup()
	{
		return defaultGroup;
	}
	
	public Collection<PermissionGroup> getGroups()
	{
		return Collections.unmodifiableCollection(groups.values());
	}
	
	public void remove(PermissionBase object)
	{
		if (object instanceof PermissionUser)
		{
			if (ProxyServer.getInstance().getPlayer(((PermissionUser)object).getId()) != null)
				throw new IllegalArgumentException("Cannot remove a user while they are online");
		}
		
		backend.removeObject(object);
		
		if (object instanceof PermissionGroup)
		{
			// Remove any parent references
			for (PermissionGroup group : groups.values())
				group.removeParent((PermissionGroup)object);
			for (PermissionUser user : cachedUsers.asMap().values())
				user.removeParent((PermissionGroup)object);
			
			groups.remove(object.getName().toLowerCase());
		}
		else
			cachedUsers.invalidate(((PermissionUser)object).getId());
	}
	
	public PermsPlugin getPlugin()
	{
		return plugin;
	}
}
