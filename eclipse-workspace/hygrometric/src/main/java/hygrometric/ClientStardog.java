package hygrometric;

import java.util.concurrent.TimeUnit;

import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.ConnectionPool;
import com.complexible.stardog.api.ConnectionPoolConfig;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;

public class ClientStardog {
	/**
	 *  Creates a connection to the DBMS itself so we
	 *  can perform some administrative actions.
	 */
	private final static String url = "http://localhost:5820";
	private final static String username = "admin";
	private final static String password = "admin";
	private final static String dbName = "hygrometric";
	private static boolean reasoningType = false;
	private static int maxPool = 200;
	private static int minPool = 10;
	private static long blockCapacityTime = 900;
    private static TimeUnit blockCapacityTimeUnit = TimeUnit.SECONDS;
    private static long expirationTime = 300;
    private static TimeUnit expirationTimeUnit = TimeUnit.SECONDS;
	
	
	public static void createAdminConnection() {
	    try (final AdminConnection aConn = AdminConnectionConfiguration.toServer(url)
	            .credentials(username, password)
	            .connect()) {

	        // A look at what databases are currently in Stardog
	        aConn.list().forEach(item -> System.out.println(item));

	        // Checks to see if the 'myNewDB' is in Stardog. If it is, we are
	        // going to drop it so we are starting fresh
			if (aConn.list().contains(dbName)) {aConn.drop(dbName);}
	        // Convenience function for creating a persistent
	        // database with all the default settings.
	        aConn.disk(dbName).create();
	    }
	}
	
	

	/**
	 * Now we want to create the configuration for our pool.
	 * @param connectionConfig the configuration for the connection pool
	 * @return the newly created pool which we will use to get our Connections
	 */
	private static ConnectionPool createConnectionPool
	                              (ConnectionConfiguration connectionConfig) {
	    ConnectionPoolConfig poolConfig = ConnectionPoolConfig
	            .using(connectionConfig)
	            .minPool(minPool)
	            .maxPool(maxPool)
	            .expiration(expirationTime, expirationTimeUnit)
	            .blockAtCapacity(blockCapacityTime, blockCapacityTimeUnit);

	    return poolConfig.create();
	}
	
	public static void main(String[] args) {
		createAdminConnection();
		
		ConnectionConfiguration connectionConfig = ConnectionConfiguration
		        .to(dbName)
		        .server(url)
		        .reasoning(reasoningType)
		        .credentials(username, password);
		
		// creates the Stardog connection pool
		ConnectionPool connectionPool = createConnectionPool(connectionConfig);
	}
}