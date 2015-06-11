package au.com.addstar.simpleperms.permissions;

import java.util.List;
import java.util.UUID;

import au.com.addstar.simpleperms.PermissionManager;
import au.com.addstar.simpleperms.backend.IBackend;

public class PermissionUser extends PermissionBase
{
	private UUID id;
	private String name;
	
	public PermissionUser(UUID id, List<String> rawPermissions, String name, IBackend backend, PermissionManager manager)
	{
		super(rawPermissions, backend, manager);
		
		this.id = id;
		this.name = name;
	}
	
	public UUID getId()
	{
		return id;
	}
	
	@Override
	public String getName()
	{
		return id.toString();
	}
	
	@Override
	public String getDisplayName()
	{
		return name;
	}
}
