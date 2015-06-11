package au.com.addstar.simpleperms.permissions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.backend.IBackend;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class PermissionBase
{
	private IBackend backend;
	private PermissionManager manager;
	
	private List<PermissionGroup> parents;
	
	private List<String> rawPermissions;
	
	private Map<String, Boolean> staticPermissions;
	private Map<String[], Boolean> dynamicPermissions;
	
	protected PermissionBase(List<String> rawPermissions, IBackend backend, PermissionManager manager)
	{
		this.rawPermissions = rawPermissions;
		this.backend = backend;
		this.manager = manager;
		
		parents = Lists.newArrayList();
		staticPermissions = Maps.newHashMap();
		dynamicPermissions = Maps.newHashMap();
	}
	
	public void addPermission(String permission)
	{
		permission = permission.toLowerCase();
		
		if (rawPermissions.contains(permission))
			return;
		
		rawPermissions.add(permission);
		
		backend.addPermission(this, permission);
	}
	
	public void removePermission(String permission)
	{
		permission = permission.toLowerCase();
		
		if (!rawPermissions.remove(permission))
			return;
		
		backend.removePermission(this, permission);
	}
	
	public boolean hasPermission(String permission)
	{
		Boolean value = getPermission(permission);
		if (value != null)
			return value;
		else
			return false;
	}
	
	public Boolean getPermission(String permission)
	{
		// Highest priority: local perm
		Boolean value = getLocalPermission(permission);
		if (value != null)
			return value;
		
		// Next priority, groups
		for (PermissionGroup group : parents)
		{
			value = group.getPermission(permission);
			if (value != null)
				return value;
		}
		
		// Dont recuse on default group
		if (this == manager.getDefaultGroup())
			return null;
		
		// Lowest priority
		return manager.getDefaultGroup().getPermission(permission);
	}
	
	public Boolean hasLocalPermission(String permission)
	{
		Boolean value = getLocalPermission(permission);
		if (value != null)
			return value;
		else
			return false;
	}
	
	public Boolean getLocalPermission(String permission)
	{
		permission = permission.toLowerCase();
		
		// Check static perms
		Boolean value = staticPermissions.get(permission);
		if (value != null)
			return value;
		
		// Check dynamic permissions
		Map.Entry<String[], Boolean> mostSpecific = null;
		int bestMatch = 0;
		
		// Find the most specific matching glob
		for (Map.Entry<String[], Boolean> entry : dynamicPermissions.entrySet())
		{
			int matchPos = globMatches(entry.getKey(), permission);
			if (matchPos > bestMatch)
			{
				mostSpecific = entry;
				bestMatch = matchPos;
			}
		}
		
		if (mostSpecific != null)
			return mostSpecific.getValue();
		else
			return null;
	}
	
	private int globMatches(String[] glob, String permission)
	{
		int index = 0;
		boolean match = true;
		
		for (String section : glob)
		{
			if (index == 0)
			{
				match = permission.startsWith(section);
				index += section.length();
			}
			else
			{
				int start = permission.indexOf(section, index);
				match = (start != -1);
				index = start + section.length();
			}
			
			if (!match)
				break;
		}
		
		if (match)
			return index;
		else
			return 0;
	}
	
	public void rebuildPermissions()
	{
		staticPermissions.clear();
		dynamicPermissions.clear();
		
		// Sort into static and dynamic perms
		for (String rawPermission : rawPermissions)
		{
			boolean value = true;
			if (rawPermission.startsWith("-"))
			{
				value = false;
				rawPermission = rawPermission.substring(1);
			}
			
			if (rawPermission.contains("*"))
				dynamicPermissions.put(rawPermission.toLowerCase().split("\\*"), value);
			else
				staticPermissions.put(rawPermission.toLowerCase(), value);
		}
	}
	
	public List<PermissionGroup> parents()
	{
		return Collections.unmodifiableList(parents);
	}
	
	public boolean hasParent(PermissionGroup parent)
	{
		Stack<PermissionBase> toSearch = new Stack<PermissionBase>();
		
		toSearch.add(this);
		
		while(!toSearch.isEmpty())
		{
			PermissionBase group = toSearch.pop();
			
			if (group.equals(parent))
				return true;
			
			toSearch.addAll(group.parents);
		}
		
		return false;
	}
	
	public void setParentsInternal(List<PermissionGroup> groups)
	{
		parents.clear();
		parents.addAll(groups);
	}
	
	public void setParents(List<PermissionGroup> groups)
	{
		for (PermissionGroup group : parents)
			backend.removeParent(this, group);
		
		parents.clear();
		parents.addAll(groups);
		
		for (PermissionGroup group : groups)
			backend.addParent(this, group);
	}
	
	public void addParent(PermissionGroup parent)
	{
		if (parents.add(parent))
			backend.addParent(this, parent);
	}
	
	public void removeParent(PermissionGroup parent)
	{
		if (parents.remove(parent))
			backend.removeParent(this, parent);
	}
	
	public List<String> getRawPermissions()
	{
		return Collections.unmodifiableList(rawPermissions);
	}
	
	public abstract String getName();
	public abstract String getDisplayName();
	
	@Override
	public String toString()
	{
		return getName();
	}
}
