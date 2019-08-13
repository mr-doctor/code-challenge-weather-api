package nz.co.solnet.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static nz.co.solnet.helper.DatabaseHelper.DATABASE_URL;


@WebServlet(name = "UpdateServlet", urlPatterns = "/update")
public class UpdateServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(UpdateServlet.class);
	private static String[] updateCommands = {
			"city",
			"date",
			"temperature",
			"wind",
			"rain",
	};

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {

			Map<String, String> parameters = handleParams(req);
			PreparedStatement p = conn.prepareStatement("SELECT * FROM cities_weather WHERE id=?");
			p.setString(1, req.getParameter("id"));

			ResultSet q = p.executeQuery();
			q.next();

			JsonObject oldRecord = API.buildCityJSON(q);
			Map<String, String> updated = new HashMap<>();

			for (Map.Entry<String, JsonValue> entry : oldRecord.entrySet()) {
				updated.put(entry.getKey(), parameters.getOrDefault(entry.getKey(), entry.getValue().toString().replace("\"", "")));
			}

			// 1. Create existing record - DONE
			// 2. Swap out new values for old ones if they are present in map - DONE
			// 3. Set all values for the specific id to the ones we've defined - DONE
			logger.info("Updating to " + updated.entrySet());
			logger.info("Date is " + updated.get("date"));
			p = conn.prepareStatement("UPDATE cities_weather SET city=?, date=?, temperature=?, wind=?, rain=? WHERE id=?");
			p.setInt(6, Integer.parseInt(req.getParameter("id")));

			p.setString(1, updated.get("city"));
			p.setDate(2, Date.valueOf(updated.get("date")));
			p.setBigDecimal(3, new BigDecimal(updated.get("temperature")));

			p.setInt(4, Integer.parseInt(updated.get("wind")));
			p.setInt(5, Integer.parseInt(updated.get("rain")));

			p.execute();
			logger.info("Updated item with id=" + req.getParameter("id"));

		} catch (SQLException e) {
			logger.error("Error in initialising connection", e);
		}
	}

	private JsonObjectBuilder jsonObjectToBuilder(JsonObject jo) {
		JsonObjectBuilder job = Json.createObjectBuilder();

		for (Map.Entry<String, JsonValue> entry : jo.entrySet()) {
			job.add(entry.getKey(), entry.getValue());
		}

		return job;
	}

	private Map<String, String> handleParams(HttpServletRequest request) {
		Map<String, String> parameters = new HashMap<>();

		Enumeration enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = enumeration.nextElement().toString();
			if (request.getParameter(paramName).length() > 0) {
				parameters.put(paramName, request.getParameter(paramName));
			}
		}

		return parameters;
	}
}
