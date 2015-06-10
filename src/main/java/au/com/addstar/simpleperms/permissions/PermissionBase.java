package au.com.addstar.simpleperms.permissions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class PermissionBase
{
	private List<PermissionGroup> parents;
	
	private List<String> rawPermissions;
	
	private Map<String, Boolean> staticPermissions;
	private Map<String, Boolean> dynamicPermissions;
	
	protected PermissionBase(List<String> rawPermissions)
	{
		this.rawPermissions = rawPermissions;
		
		parents = Lists.newArrayList();
		staticPermissions = Maps.newHashMap();
		dynamicPermissions = Maps.newHashMap();
	}
	
	public boolean hasPermission(String permission)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public boolean hasLocalPermission(String permission)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public void rebuildPermissions()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public List<PermissionGroup> parents()
	{
		return Collections.unmodifiableList(parents);
	}
	
	public void addParent(PermissionGroup parent)
	{
		parents.add(parent);
	}
	
	public void save()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public List<String> getRawPermissions()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
