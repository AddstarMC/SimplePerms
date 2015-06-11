package au.com.addstar.simpleperms.backend;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import au.com.addstar.simpleperms.permissions.PermissionBase;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import au.com.addstar.simpleperms.permissions.PermissionUser;

public interface IBackend
{
	public Map<String, PermissionGroup> loadAllGroups();
	
	public PermissionGroup loadGroup(String groupName);
	
	public PermissionUser loadUser(UUID userId);
	
	public List<String> loadParents(String groupName);
	
	public List<String> loadParents(UUID userId);
	
	public void addPermission(PermissionBase object, String permission);
	
	public void removePermission(PermissionBase object, String permission);
	
	public void shutdown();
	
	public boolean isValid();
}
