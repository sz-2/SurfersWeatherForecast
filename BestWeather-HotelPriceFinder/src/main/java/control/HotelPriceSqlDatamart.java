package control;

import model.HotelPrice;
import model.Location;
import org.json.JSONObject;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


public class HotelPriceSqlDatamart {
	public static void updateHotelPrice(Statement statement, String event) throws SQLException {
		createHotelPriceTable(statement);
		HotelPrice hotelPrice = getHotelPriceFromEvent(event);
		statement.execute(String.format(
				"INSERT OR REPLACE INTO Hotel (date, hotelName, location, island, agency, price) " +
						"SELECT '%s', '%s', '%s', '%s', '%s', %s " +
						"WHERE (SELECT price FROM Hotel WHERE date = '%s' AND location = '%s' AND hotelName = '%s') > %s " +
						"OR NOT EXISTS (SELECT 1 FROM Hotel WHERE date = '%s' AND location = '%s' AND hotelName = '%s');",
				hotelPrice.getDate(),
				hotelPrice.getHotelName(),
				hotelPrice.getLocation().getName(),
				hotelPrice.getLocation().getIsland(),
				hotelPrice.getAgency(),
				hotelPrice.getPrice(),
				hotelPrice.getDate(),
				hotelPrice.getLocation().getName(),
				hotelPrice.getHotelName(),
				hotelPrice.getPrice(),
				hotelPrice.getDate(),
				hotelPrice.getLocation().getName(),
				hotelPrice.getHotelName()
		));
		deletePastHotelPrice(statement);
	}

	private static HotelPrice getHotelPriceFromEvent(String event) {
		JSONObject hotel = new JSONObject(event);
		String hotelName = hotel.getJSONObject("hotel").getString("hotelName").toLowerCase();
		String locationName = hotel.getJSONObject("hotel").getString("location").toLowerCase();
		String islandName = hotel.getJSONObject("hotel").getString("island").toLowerCase();
		String agency = hotel.getString("agency").toLowerCase();
		float price = hotel.getFloat("price");
		String date = stringDateFormatter(hotel.getString("date"));
		return new HotelPrice(hotelName, agency, price, date, new Location(locationName, islandName));
	}

	private static void createHotelPriceTable(Statement statement) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS Hotel " +
				"(" +
				"date TEXT," +
				"hotelName TEXT," +
				"location TEXT," +
				"island TEXT," +
				"agency TEXT," +
				"price REAL," +
				"PRIMARY KEY (date, location, hotelName)" +
				");");
	}

	private static void deletePastHotelPrice(Statement statement) throws SQLException {
		String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
		statement.execute(String.format("DELETE FROM Hotel WHERE date < '%s';", today));
	}

	private static String stringDateFormatter(String date) {
		Instant dateInstant = Instant.parse(date);
		return DateTimeFormatter.ofPattern("yyyy-MM-dd")
				.withZone(ZoneId.systemDefault())
				.format(dateInstant);
	}
}


