package nz.co.solnet.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.json.JsonObject;
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
import static nz.co.solnet.api.Util.*;


@WebServlet(name = "UpdateServlet", urlPatterns = "/update")
public class UpdateServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(UpdateServlet.class);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, String> parameters = handleParams(req);
		if (!IDValid(req, resp) || !inputsValid(parameters, resp)) {
			return;
		}

		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {

			PreparedStatement p = conn.prepareStatement("SELECT * FROM cities_weather WHERE id=?");
			p.setString(1, req.getParameter(ID_STR));

			ResultSet q = p.executeQuery();
			if (!q.next()) {
				return;
			}

			// create the old record
			JsonObject oldRecord = buildCityJSON(q);
			Map<String, String> updated = new HashMap<>();

			// update the record, using unchanged values from the old record and changed values from the new record
			for (Map.Entry<String, JsonValue> entry : oldRecord.entrySet()) {
				// strip away quote marks for proper formatting
				updated.put(entry.getKey(), parameters.getOrDefault(entry.getKey(), entry.getValue().toString().replace("\"", "")));
			}

			p = conn.prepareStatement("UPDATE cities_weather SET city=?, date=?, temperature=?, wind=?, rain=? WHERE id=?");

			p.setString(1, updated.get(CITY_NAME_STR));
			p.setDate(2, Date.valueOf(updated.get(DATE_STR)));
			p.setBigDecimal(3, new BigDecimal(updated.get(TEMPERATURE_STR)));
			p.setInt(4, Integer.parseInt(updated.get(WIND_STR)));
			p.setInt(5, Integer.parseInt(updated.get(RAIN_STR)));
			p.setInt(6, Integer.parseInt(req.getParameter(ID_STR)));

			p.execute();
			logger.info("Updated item with id=" + req.getParameter(ID_STR));

		} catch (SQLException e) {
			logger.error("Error in initialising connection", e);
		}
	}

	private Map<String, String> handleParams(HttpServletRequest request) {
		Map<String, String> parameters = new HashMap<>();

		// pull the parameters out of the request
		Enumeration enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()) {
			String paramName = enumeration.nextElement().toString();
			// for each parameter name, if the parameter has a value, add it to the map
			if (request.getParameter(paramName).length() > 0) {
				parameters.put(paramName, request.getParameter(paramName));
			}
		}

		return parameters;
	}
}
