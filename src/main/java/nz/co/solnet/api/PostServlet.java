package nz.co.solnet.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

import static nz.co.solnet.helper.DatabaseHelper.DATABASE_URL;
import static nz.co.solnet.api.Util.*;

@WebServlet(name = "PostServlet", urlPatterns = "/post")
public class PostServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(PostServlet.class);


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!inputsPresent(req, resp) || !inputsValid(req, resp)) {
			return;
		}

		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {

			PreparedStatement p = conn.prepareStatement("INSERT INTO cities_weather VALUES (DEFAULT, ?, ?, ?, ?, ?)");
			p.setString(1, req.getParameter(CITY_NAME_STR));
			p.setDate(2, Date.valueOf(req.getParameter(DATE_STR)));
			p.setBigDecimal(3, new BigDecimal(req.getParameter(TEMPERATURE_STR)));
			p.setInt(4, Integer.parseInt(req.getParameter(WIND_STR)));
			p.setInt(5, Integer.parseInt(req.getParameter(RAIN_STR)));
			p.execute();

			String newCity = Json.createObjectBuilder()
					.add(CITY_NAME_STR, req.getParameter(CITY_NAME_STR))
					.add(DATE_STR, req.getParameter(DATE_STR))
					.add(TEMPERATURE_STR, req.getParameter(TEMPERATURE_STR))
					.add(WIND_STR, req.getParameter(WIND_STR))
					.add(RAIN_STR, req.getParameter(RAIN_STR))
					.build().toString();

			logger.info(newCity);

		} catch (SQLException e) {
			logger.error("Error in initialising connection", e);
		}
	}
}
