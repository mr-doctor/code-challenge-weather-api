package nz.co.solnet.helper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseHelper {

	// Hide the default constructor to prevent erroneous initialisation
	private DatabaseHelper() {

	}

	private static final String DATABASE_URL = "jdbc:derby:weatherdb;create=true";
	private static final Logger logger = LogManager.getLogger(DatabaseHelper.class);

	/**
	 * Create a derby database if it doesn't exist and insert seed data.
	 */
	public static void initialiseDB() {

		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {

			insertInitialData(conn);
		} catch (SQLException e) {
			logger.error("Error in inserting initial data", e);
		}
	}

	/**
	 * Insert sample seed data into the database.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	private static void insertInitialData(Connection conn) throws SQLException {

		try (Statement statement = conn.createStatement()) {

			if (!doesTableExist("cities_weather", conn)) {

				String sql = "CREATE TABLE cities_weather (id int not null generated always as identity,"
						+ " city varchar(256) not null," + " date date not null," + " temperature decimal(5,2),"
						+ " wind int, rain int," + " primary key (id, city, date))";
				statement.execute(sql);
				logger.info("Table created.");

				sql = "INSERT INTO cities_weather VALUES (DEFAULT, 'Wellington', '2019-04-15', 16, 65, 0), "
						+ "(DEFAULT, 'Auckland', '2019-04-15', 18, 15, 1.0), "
						+ "(DEFAULT, 'Christchurch', '2019-04-15', 19, 16, 1.5)";
				statement.execute(sql);
				logger.info("Table {} rows created.", statement.getUpdateCount());
			} else {
				logger.info("Table already exists");
			}
		}
	}

	/**
	 * Checks if the table exists in the database.
	 * 
	 * @param tableName - table name to be checked in the database
	 * @param conn      - database connection to use
	 * @return - boolean to indicate of the table exists or not
	 * @throws SQLException
	 */
	private static boolean doesTableExist(String tableName, Connection conn) throws SQLException {
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet result = meta.getTables(null, null, tableName.toUpperCase(), null);

		return result.next();
	}

	/**
	 * Utility method to drop the table.
	 */
	public static void cleanDatabase() {

		try (Connection conn = DriverManager.getConnection(DATABASE_URL);
				Statement statement = conn.createStatement()) {
			String sql1 = "DROP TABLE cities_weather";
			statement.execute(sql1);
			logger.info("Table dropped successfully");
		} catch (SQLException e) {
			logger.error("Error in dropping table", e);
		}
	}

	/**
	 * This method does a graceful shutdown for the database.
	 */
	public static void cleanupDB() {

		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {

			if (e.getSQLState().equals("XJ015")) {
				logger.info("Database shutdown successfully");
			} else {
				logger.error("Error in database shutdown", e);
			}
		}
	}
}
