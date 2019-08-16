package nz.co.solnet.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static nz.co.solnet.api.Util.*;
import static nz.co.solnet.helper.DatabaseHelper.DATABASE_URL;

@WebServlet(name = "DeleteServlet", urlPatterns = "/delete")
public class DeleteServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(DeleteServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!IDValid(req, resp)) {
			return;
		}
		try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {

			PreparedStatement p = conn.prepareStatement("DELETE FROM cities_weather WHERE id=?");
			p.setInt(1, Integer.parseInt(req.getParameter(ID_STR)));
			p.execute();
			logger.info("Deleted item with id=" + req.getParameter(ID_STR));

		} catch (SQLException e) {
			logger.error("Error in initialising connection", e);
		}
	}
}
