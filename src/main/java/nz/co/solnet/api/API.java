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

import static nz.co.solnet.helper.DatabaseHelper.DATABASE_URL;

@WebServlet(name = "API", urlPatterns = "/weather")
public class API extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(API.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
			if ("coldest".equals(req.getParameter("extremeRecord")) || "warmest".equals(req.getParameter("extremeRecord"))) {
				getCityByTemperature(req.getParameter("extremeRecord"), conn, req, resp);
			} else {
				if (req.getParameter("id") != null) {
					getCityByID(conn, req, resp);
				} else {
					getAllCities(conn, req, resp);
				}
			}
		} catch (SQLException e) {
			logger.error("Error in initialising connection", e);
		}
	}

	private void getCityByTemperature(String extremeRecord, Connection conn, HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		Statement s = conn.createStatement();
		ResultSet q = s.executeQuery("SELECT * FROM cities_weather");

		q.next();
		JsonObject bestCity = buildCityJSON(q);
		BigDecimal bestTemperature = q.getBigDecimal("temperature");
		while (q.next()) {
			JsonObject newCity = buildCityJSON(q);
			BigDecimal newTemperature = q.getBigDecimal("temperature");
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

	public static JsonObject buildCityJSON(ResultSet q) throws SQLException {
		return Json.createObjectBuilder()
				.add("city", q.getString("city"))
				.add("date", q.getDate("date").toString())
				.add("temperature", q.getBigDecimal("temperature"))
				.add("wind", q.getInt("wind"))
				.add("rain", q.getInt("rain"))
				.build();
	}

	private void getAllCities(Connection conn, HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		Statement s = conn.createStatement();
		ResultSet q = s.executeQuery("SELECT * FROM cities_weather");

		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		while (q.next()) {
			JsonObject j = buildCityJSON(q);
			logger.info(j);
			jsonBuilder.add(q.getString("id"), j);
		}

		resp.getWriter().write(jsonBuilder.build().toString());
	}

	private void getCityByID(Connection conn, HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
		PreparedStatement p = conn.prepareStatement("SELECT * FROM cities_weather WHERE id=?");
		p.setString(1, req.getParameter("id"));

		ResultSet q = p.executeQuery();
		q.next();


		JsonObject json = buildCityJSON(q);
		resp.getWriter().write(json.toString());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {

			PreparedStatement p = conn.prepareStatement("INSERT INTO cities_weather VALUES (DEFAULT, ?, ?, ?, ?, ?)");
			p.setString(1, req.getParameter("city"));
			p.setDate(2, Date.valueOf(req.getParameter("date")));
			p.setBigDecimal(3, new BigDecimal(req.getParameter("temperature")));
			p.setInt(4, Integer.parseInt(req.getParameter("wind")));
			p.setInt(5, Integer.parseInt(req.getParameter("rain")));
			p.execute();

			String newCity = Json.createObjectBuilder()
					.add("city", req.getParameter("city"))
					.add("date", req.getParameter("date"))
					.add("temperature", req.getParameter("temperature"))
					.add("wind", req.getParameter("wind"))
					.add("rain", req.getParameter("rain"))
					.build().toString();

			logger.info(newCity);

		} catch (SQLException e) {
			logger.error("Error in initialising connection", e);
		}
	}

	//	public static void main(String[] args) {
//		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
//			System.out.println(conn);
//
//			Statement s = conn.createStatement();
//			PreparedStatement p = conn.prepareStatement("SELECT * FROM cities_weather WHERE city=?");
//			p.setString(1, "Boogah");
//			ResultSet q = p.executeQuery();
//			while (q.next()){
//				System.out.println(q.getString("city"));
//			}
//
//			p = conn.prepareStatement("INSERT INTO cities_weather VALUES (DEFAULT, ?, ?, ?, ?, ?)");
//
//			p.setString(1, "poop");
//			p.setDate(2, new Date(1));
//			p.setBigDecimal(3, new BigDecimal("3.14"));
//			p.setInt(4, 69);
//			p.setInt(5, 420);
//			p.execute();
//
//
//			s = conn.createStatement();
//			q = s.executeQuery("SELECT * FROM cities_weather");
//			System.out.println("---");
//			while (q.next()){
//				System.out.println(q.getString("city"));
//				System.out.println(q.getDate("date"));
//				System.out.println(q.getBigDecimal("temperature"));
//				System.out.println(q.getInt("wind"));
//				System.out.println(q.getInt("rain"));
//				System.out.println();
//			}
//
//
//		} catch (SQLException e) {
//			logger.error("Error in initialising connection", e);
//		}
//	}
}
