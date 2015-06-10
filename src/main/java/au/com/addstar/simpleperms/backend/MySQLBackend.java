package au.com.addstar.simpleperms.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private PreparedStatement loadChildren;
	
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
				statement.executeUpdate("CREATE TABLE `hierarchy` (`idx` INTEGER AUTO_INCREMENT PRIMARY KEY, `parentid` VARCHAR(36) NOT NULL, `childid` VARCHAR(36) NOT NULL, INDEX (`parentid`, `idx`));");
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
			loadChildren = connection.prepareStatement("SELECT `childid` FROM `hierarchy` WHERE `parentid`=?;");
			return true;
		}
		catch (SQLException e)
		{
			logger.log(Level.SEVERE, "Failed initialize statements", e);
			return false;
		}
	}
	
	@Override
	public List<PermissionGroup> loadAllGroups()
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public PermissionGroup loadGroup(String groupName)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public PermissionUser loadUser(UUID userId)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public void save(PermissionBase object)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public void shutdown()
	{
		try
		{
			loadGroups.close();
			loadObject.close();
			loadChildren.close();
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
