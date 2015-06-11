package au.com.addstar.simpleperms.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.md_5.bungee.config.Configuration;
import au.com.addstar.simpleperms.permissions.PermissionBase;
import au.com.addstar.simpleperms.permissions.PermissionGroup;
import au.com.addstar.simpleperms.permissions.PermissionUser;

public class MySQLBackend implements IBackend
{
	private Connection connection;
	private Logger logger;
	
	private PreparedStatement loadGroups;
	private PreparedStatement loadObject;
	private PreparedStatement addObject;
	private PreparedStatement loadUserName;
	private PreparedStatement removeObject;
	private PreparedStatement removeObjectPermissions;
	private PreparedStatement removeObjectHierarchy;
	private PreparedStatement loadParents;
	private PreparedStatement addPerm;
	private PreparedStatement removePerm;
	private PreparedStatement addParent;
	private PreparedStatement removeParent;
	private PreparedStatement findUser;
	
	public MySQLBackend(Configuration config, Logger logger)
	{
		this.logger = logger;
		
		Configuration dbConfig = config.getSection("database");
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + dbConfig.getString("host", "localhost") + "/" + dbConfig.getString("database", "database"), dbConfig.getString("username", "username"), dbConfig.getString("password", "password"));
			
			checkTables();
			initializeStatements();
		}
		catch (ClassNotFoundException e)
		{
			// MySQL JDBC driver is always packaged with BungeeCord
			throw new AssertionError();
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to initialize connection to MySQL", e);
		}
	}
	
	private boolean checkTables()
	{
		try
		{
			Statement statement = connection.createStatement();

			// Check objects table
			try
			{
				statement.executeQuery("SELECT * FROM `objects` LIMIT 0;");
				// EXISTS DO NOTHING
			}
			catch (SQLException e)
			{
				// Create it
				statement.executeUpdate("CREATE TABLE `objects` (`objectid` VARCHAR(36) NOT NULL PRIMARY KEY, `type` TINYINT(1) NOT NULL, `name` VARCHAR(30));");
			}
			
			// Check permissions table
			try
			{
				statement.executeQuery("SELECT * FROM `permissions` LIMIT 0;");
				// EXISTS DO NOTHING
			}
			catch (SQLException e)
			{
				// Create it
				statement.executeUpdate("CREATE TABLE `permissions` (`idx` INTEGER AUTO_INCREMENT PRIMARY KEY, `objectid` VARCHAR(36) NOT NULL, `permission` VARCHAR(100) NOT NULL, INDEX (`objectid`, `idx`));");
			}
			
			// Check hierarchy table
			try
			{
				statement.executeQuery("SELECT * FROM `hierarchy` LIMIT 0;");
				// EXISTS DO NOTHING
			}
			catch (SQLException e)
			{
				// Create it
				statement.executeUpdate("CREATE TABLE `hierarchy` (`idx` INTEGER AUTO_INCREMENT PRIMARY KEY, `childid` VARCHAR(36) NOT NULL, `parentid` VARCHAR(36) NOT NULL, INDEX (`childid`, `idx`));");
			}
			
			statement.close();
			return true;
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to check / create database tables", e);
			return false;
		}
	}
	
	private boolean initializeStatements()
	{
		try
		{
			loadGroups = connection.prepareStatement("SELECT `objectid` FROM `objects` WHERE `type`=1;");
			loadObject = connection.prepareStatement("SELECT `permission` FROM `permissions` WHERE `objectid`=?;");
			addObject = connection.prepareStatement("REPLACE INTO `objects` VALUES (?,?,?);");
			loadParents = connection.prepareStatement("SELECT `parentid` FROM `hierarchy` WHERE `childid`=?;");
			
			loadUserName = connection.prepareStatement("SELECT `name` FROM `objects` WHERE `objectid`=?;");
			
			removeObject = connection.prepareStatement("DELETE FROM `objects` WHERE `objectid`=?;");
			removeObjectPermissions = connection.prepareStatement("DELETE FROM `permissions` WHERE `objectid`=?;");
			removeObjectHierarchy = connection.prepareStatement("DELETE FROM `hierarchy` WHERE `childid`=? OR `parentid`=?;");
			
			addPerm = connection.prepareStatement("INSERT INTO `permissions` VALUES (DEFAULT,?,?);");
			removePerm = connection.prepareStatement("DELETE FROM `permissions` WHERE `objectid`=? AND `permission`=?;");
			
			addParent = connection.prepareStatement("INSERT INTO `hierarchy` VALUES (DEFAULT,?,?);");
			removeParent = connection.prepareStatement("DELETE FROM `hierarchy` WHERE `childid`=? AND `parentid`=?;");
			
			findUser = connection.prepareStatement("SELECT `objectid` FROM `objects` WHERE `name` LIKE ? AND `type`=0;");
			
			return true;
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed initialize statements", e);
			return false;
		}
	}
	
	@Override
	public Map<String, PermissionGroup> loadAllGroups()
	{
		try
		{
			// Load group names
			List<String> names = Lists.newArrayList();
			ResultSet rs = loadGroups.executeQuery();
			
			while(rs.next())
				names.add(rs.getString(1));
			
			rs.close();
			
			// Load group contents
			Map<String, PermissionGroup> groups = Maps.newHashMapWithExpectedSize(names.size());
			ListMultimap<String, String> parents = ArrayListMultimap.create();
			
			for (String name : names)
			{
				groups.put(name.toLowerCase(), new PermissionGroup(name, loadPermissions(name), this));
				parents.putAll(name, loadParents0(name));
			}
			
			// Compute hierarchy
			for (PermissionGroup group : groups.values())
			{
				List<String> parentNames = parents.get(group.getName());
				List<PermissionGroup> parentGroups = Lists.newArrayListWithCapacity(parentNames.size());
				for (String parentName : parentNames)
				{
					PermissionGroup parent = groups.get(parentName.toLowerCase());
					if (parent == null)
						continue;
					
					parentGroups.add(parent);
				}
				
				group.setParentsInternal(parentGroups);
			}
			
			return groups;
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to load groups", e);
			return Collections.emptyMap();
		}
	}
	
	private List<String> loadPermissions(String objectId) throws SQLException
	{
		List<String> permissions = Lists.newArrayList();
		
		loadObject.setString(1, objectId);
		ResultSet rs = loadObject.executeQuery();
		try
		{
			while(rs.next())
				permissions.add(rs.getString(1));
		}
		finally
		{
			rs.close();
		}
		
		return permissions;
	}
	
	@Override
	public void addPermission( PermissionBase object, String permission )
	{
		try
		{
			addPerm.setString(1, object.getName());
			addPerm.setString(2, permission);
			addPerm.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to add permission '" + permission + "' to " + object.getName(), e);
		}
	}
	
	@Override
	public void removePermission( PermissionBase object, String permission )
	{
		try
		{
			removePerm.setString(1, object.getName());
			removePerm.setString(2, permission);
			removePerm.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to remove permission '" + permission + "' from " + object.getName(), e);
		}
	}
	
	@Override
	public void addParent( PermissionBase object, PermissionGroup parent )
	{
		try
		{
			addParent.setString(1, object.getName());
			addParent.setString(2, parent.getName());
			addParent.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to add parent " + parent.getName() + " to " + object.getName(), e);
		}
	}
	
	@Override
	public void removeParent( PermissionBase object, PermissionGroup parent )
	{
		try
		{
			removeParent.setString(1, object.getName());
			removeParent.setString(2, parent.getName());
			removeParent.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to remove parent " + parent.getName() + " from " + object.getName(), e);
		}
	}
	
	private List<String> loadParents0(String objectId) throws SQLException
	{
		List<String> parents = Lists.newArrayList();
		
		loadParents.setString(1, objectId);
		ResultSet rs = loadParents.executeQuery();
		try
		{
			while(rs.next())
				parents.add(rs.getString(1));
		}
		finally
		{
			rs.close();
		}
		
		return parents;
	}
	
	@Override
	public List<String> loadParents( String groupName )
	{
		try
		{
			return loadParents0(groupName);
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to load parents for group " + groupName, e);
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<String> loadParents( UUID userId )
	{
		try
		{
			return loadParents0(userId.toString());
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to load parents for user " + userId, e);
			return Collections.emptyList();
		}
	}
	
	@Override
	public PermissionGroup loadGroup(String groupName)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public PermissionUser loadUser(UUID userId, String name)
	{
		try
		{
			if (name == null)
			{
				loadUserName.setString(1, userId.toString());
				ResultSet rs = loadUserName.executeQuery();
				if (rs.next())
					name = rs.getString(1);
				rs.close();
			}
			else
			{
				addObject.setString(1, userId.toString());
				addObject.setInt(2, 0);
				addObject.setString(3, name);
				addObject.executeUpdate();
			}
			
			return new PermissionUser(userId, loadPermissions(userId.toString()), name, this);
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to load user " + userId, e);
			return new PermissionUser(userId, Lists.<String>newArrayList(), name, this);
		}
	}
	
	@Override
	public void addObject( PermissionBase object )
	{
		try
		{
			addObject.setString(1, object.getName());
			if (object instanceof PermissionGroup)
			{
				addObject.setInt(2, 1);
				addObject.setString(3, null);
			}
			else
			{
				addObject.setInt(2, 0);
				addObject.setString(3, ((PermissionUser)object).getDisplayName());
			}
			addObject.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to add object " + object.getName(), e);
		}
	}
	
	@Override
	public void removeObject( PermissionBase object )
	{
		try
		{
			removeObject.setString(1, object.getName());
			removeObjectPermissions.setString(1, object.getName());
			removeObjectHierarchy.setString(1, object.getName());
			removeObjectHierarchy.setString(2, object.getName());
			removeObject.executeUpdate();
			removeObjectPermissions.executeUpdate();
			removeObjectHierarchy.executeUpdate();
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to remove object " + object.getName(), e);
		}
	}
	
	@Override
	public UUID findUser( String name )
	{
		try
		{
			findUser.setString(1, name);
			ResultSet rs = findUser.executeQuery();
			
			UUID id = null;
			if (rs.next())
				id = UUID.fromString(rs.getString(1));
			
			rs.close();
			return id;
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to find name " + name, e);
			return null;
		}
	}
	
	@Override
	public void shutdown()
	{
		try
		{
			loadGroups.close();
			loadObject.close();
			addObject.close();
			loadUserName.close();
			removeObject.close();
			removeObjectPermissions.close();
			removeObjectHierarchy.close();
			loadParents.close();
			addPerm.close();
			removePerm.close();
			findUser.close();
			
			connection.close();
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to close database connection", e);
		}
	}
	
	public boolean isValid()
	{
		return connection != null;
	}
}
