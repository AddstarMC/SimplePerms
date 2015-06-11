package au.com.addstar.simpleperms.permissions;

import java.util.List;
import java.util.UUID;

import au.com.addstar.simpleperms.backend.IBackend;

public class PermissionUser extends PermissionBase
{
	private UUID id;
	
	public PermissionUser(UUID id, List<String> rawPermissions, IBackend backend)
	{
		super(rawPermissions, backend);
		this.id = id;
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
}
