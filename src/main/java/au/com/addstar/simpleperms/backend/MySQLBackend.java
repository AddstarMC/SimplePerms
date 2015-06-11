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
	private PreparedStatement loadParents;
	private PreparedStatement addPerm;
	private PreparedStatement removePerm;
	
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
				statement.executeUpdate("CREATE TABLE `objects` (`objectid` VARCHAR(36) NOT NULL PRIMARY KEY, `type` TINYINT(1) NOT NULL);");
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
			loadParents = connection.prepareStatement("SELECT `parentid` FROM `hierarchy` WHERE `childid`=?;");
			
			addPerm = connection.prepareStatement("INSERT INTO `permissions` VALUES (DEFAULT,?,?);");
			removePerm = connection.prepareStatement("DELETE FROM `permissions` WHERE `objectid`=? AND `permission`=?;");
			
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
				for (String parentName : parentNames)
				{
					PermissionGroup parent = groups.get(parentName.toLowerCase());
					if (parent == null)
						continue;
					
					group.addParent(parent);
				}
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
	public PermissionUser loadUser(UUID userId)
	{
		try
		{
			return new PermissionUser(userId, loadPermissions(userId.toString()), this);
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed to load user " + userId, e);
			return new PermissionUser(userId, Collections.<String>emptyList(), this);
		}
	}
	
	@Override
	public void shutdown()
	{
		try
		{
			loadGroups.close();
			loadObject.close();
			loadParents.close();
			addPerm.close();
			removePerm.close();
			
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
