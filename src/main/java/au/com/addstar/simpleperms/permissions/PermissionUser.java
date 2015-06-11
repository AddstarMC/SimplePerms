package au.com.addstar.simpleperms.permissions;

import java.util.List;
import java.util.UUID;

public class PermissionUser extends PermissionBase
{
	private UUID id;
	
	public PermissionUser(UUID id, List<String> rawPermissions)
	{
		super(rawPermissions);
		this.id = id;
	}
	
	public UUID getId()
	{
		return id;
	}
}
