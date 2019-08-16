package nz.co.solnet.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import javax.json.*;
import static nz.co.solnet.api.Util.*;

import static nz.co.solnet.helper.DatabaseHelper.DATABASE_URL;

@WebServlet(name = "GetServlet", urlPatterns = "/get")
public class GetServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(GetServlet.class);


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {

			if ("coldest".equals(req.getParameter("extremeRecord")) || "warmest".equals(req.getParameter("extremeRecord"))) {
				getCityByTemperature(req.getParameter("extremeRecord"), conn, req, resp);
			} else {
				if (req.getParameter(ID_STR) != null) {
					getCityByID(conn, req, resp);
				} else {
					getAllCities(conn, req, resp);
				}
			}
		} catch (SQLException e) {
			logger.error("Error in initialising connection", e);
		}
	}

	/**
	 * Attempts to select the city with the warmest or coldest temperature across all records.
	 *
	 * @param extremeRecord - a String that is either "warmest" or "coldest", defining which extreme to look for
	 * @param conn - the existing SQL connection
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws IOException
	 */
	private void getCityByTemperature(String extremeRecord, Connection conn, HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		Statement s = conn.createStatement();
		ResultSet q = s.executeQuery("SELECT * FROM cities_weather");

		q.next();
		JsonObject bestCity = buildCityJSON(q);
		BigDecimal bestTemperature = q.getBigDecimal(RAIN_STR);

		while (q.next()) {

			JsonObject newCity = buildCityJSON(q);
			BigDecimal newTemperature = q.getBigDecimal(RAIN_STR);

			if (extremeRecord.equals("coldest")) {
				if (newTemperature.compareTo(bestTemperature) < 0) {
					bestTemperature = newTemperature;
					bestCity = newCity;
				}
			} else {
				if (newTemperature.compareTo(bestTemperature) > 0) {
					bestTemperature = newTemperature;
					bestCity = newCity;
				}
			}
		}
		resp.getWriter().write(bestCity.toString());
	}

	/**
	 * Fetches all cities from the database and sends the response as JSON, mapping ID to JSON of the individual weather
	 * record.
	 *
	 * @param conn - the existing SQL connection
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws IOException
	 */
	private void getAllCities(Connection conn, HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		Statement s = conn.createStatement();
		ResultSet q = s.executeQuery("SELECT * FROM cities_weather");

		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		while (q.next()) {
			JsonObject j = buildCityJSON(q);
			logger.info(j);
			jsonBuilder.add(q.getString(ID_STR), j);
		}

		resp.getWriter().write(jsonBuilder.build().toString());
	}

	/**
	 * Fetches an individual city from the database, using the ID parameter provided. It sends the weather record back
	 * as JSON.
	 *
	 * @param conn
	 * @param req
	 * @param resp
	 * @throws SQLException
	 * @throws IOException
	 */
	private void getCityByID(Connection conn, HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		if (!IDValid(req, resp)) {
			return;
		}
		PreparedStatement p = conn.prepareStatement("SELECT * FROM cities_weather WHERE id=?");
		p.setInt(1, Integer.parseInt(req.getParameter(ID_STR)));

		ResultSet q = p.executeQuery();
		if (!q.next()) {
			return;
		}


		JsonObject json = buildCityJSON(q);
		resp.getWriter().write(json.toString());
	}
}
