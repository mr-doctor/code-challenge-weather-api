package nz.co.solnet.api;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

class Util {

	static final String ID_STR = "id";
	static final String CITY_NAME_STR = "city";
	static final String DATE_STR = "date";
	static final String TEMPERATURE_STR = "temperature";
	static final String WIND_STR = "wind";
	static final String RAIN_STR = "rain";

	private static String[] parameters = {
			CITY_NAME_STR,
			DATE_STR,
			TEMPERATURE_STR,
			WIND_STR,
			RAIN_STR,
	};

	private static boolean isDateValid(String date) {
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			df.setLenient(false);
			df.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	private static boolean isDouble(String strNum) {
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	private static boolean isInteger(String strNum) {
		try {
			double i = Integer.parseInt(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	static boolean IDValid(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (request.getParameter(ID_STR) == null) {
			response.sendError(422, "ID parameter not provided.");
			return false;
		}
		if (!isInteger(request.getParameter(ID_STR))) {
			response.sendError(422, "ID parameter invalid.");
			return false;
		}
		return true;
	}

	static boolean inputsPresent(HttpServletRequest request, HttpServletResponse response) throws IOException {
		StringBuilder stringBuilder = new StringBuilder("Parameters not provided: ");
		int startLength = stringBuilder.length();
		for (String parameter : parameters) {
			if (request.getParameter(parameter) == null) {
				stringBuilder.append("'").append(parameter).append("' parameter, ");
			}
		}

		return handleBadInput(stringBuilder, startLength, response);
	}

	static boolean inputsValid(Map<String, String> inputs, HttpServletResponse response) throws IOException {
		StringBuilder stringBuilder = new StringBuilder("Parameters invalid: ");
		int startLength = stringBuilder.length();
		if (inputs.get(DATE_STR) != null && !isDateValid(inputs.get(DATE_STR))) {
			stringBuilder.append("'date' parameter, ");
		}
		if (inputs.get(TEMPERATURE_STR) != null && !isDouble(inputs.get(TEMPERATURE_STR))) {
			stringBuilder.append("'temperature' parameter, ");
		}
		if (inputs.get(TEMPERATURE_STR) != null && !isInteger(inputs.get(WIND_STR))) {
			stringBuilder.append("'wind' parameter, ");
		}
		if (inputs.get(TEMPERATURE_STR) != null && !isInteger(inputs.get(RAIN_STR))) {
			stringBuilder.append("'rain' parameter, ");
		}

		return handleBadInput(stringBuilder, startLength, response);
	}

	static boolean inputsValid(HttpServletRequest request, HttpServletResponse response) throws IOException {
		StringBuilder stringBuilder = new StringBuilder("Parameters invalid: ");
		int startLength = stringBuilder.length();
		if (request.getParameter(CITY_NAME_STR).length() == 0) {
			stringBuilder.append("'city' parameter, ");
		}
		if (!isDateValid(request.getParameter(DATE_STR))) {
			stringBuilder.append("'date' parameter, ");
		}
		if (!isDouble(request.getParameter(TEMPERATURE_STR))) {
			stringBuilder.append("'temperature' parameter, ");
		}
		if (!isInteger(request.getParameter(WIND_STR))) {
			stringBuilder.append("'wind' parameter, ");
		}
		if (!isInteger(request.getParameter(RAIN_STR))) {
			stringBuilder.append("'rain' parameter, ");
		}
		return handleBadInput(stringBuilder, startLength, response);
	}

	private static boolean handleBadInput(StringBuilder stringBuilder, int startLength, HttpServletResponse response) throws IOException {
		// this means we have not added any invalid parameters
		if (stringBuilder.length() != startLength) {
			// Delete the last comma-space
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append(".");
			// uses 422 because the server cannot process the request due to the parameters being invalid
			response.sendError(422, stringBuilder.toString());
			return false;
		}
		return true;
	}


	static JsonObject buildCityJSON(ResultSet q) throws SQLException {
		return Json.createObjectBuilder()
				.add(CITY_NAME_STR, q.getString(CITY_NAME_STR))
				.add(DATE_STR, q.getDate(DATE_STR).toString())
				.add(TEMPERATURE_STR, q.getBigDecimal(TEMPERATURE_STR))
				.add(WIND_STR, q.getInt(WIND_STR))
				.add(RAIN_STR, q.getInt(RAIN_STR))
				.build();
	}
}
