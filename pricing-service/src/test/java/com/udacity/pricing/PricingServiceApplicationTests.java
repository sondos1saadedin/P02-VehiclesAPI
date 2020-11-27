package com.udacity.pricing;

import com.udacity.pricing.api.PriceList;
import com.udacity.pricing.domain.price.Price;
import com.udacity.pricing.service.PricingService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PricingServiceApplicationTests {
	private static final int PORT = 8082;
	public static final String BASE_URL = "http://localhost:";

	@Autowired
	private TestRestTemplate restTemplate;

	private PricingService pricingService;

	@Autowired
	public void setLocationService(PricingService pricingService) {
		this.pricingService = pricingService;
	}

	@Test
	@Before
	public void contextLoads() {
		JSONParser jsonParser = new JSONParser();
		ClassLoader classLoader = getClass().getClassLoader();

		try (FileReader reader = new FileReader(classLoader.getResource("prices.json").getFile()))
		{
			//Read JSON file
			Object obj = jsonParser.parse(reader);

			JSONArray prices = (JSONArray) obj;
			System.out.println(prices);

			prices.forEach( emp -> {
			Price price = parsePriceObject( (JSONObject) emp );
			pricingService.savePrice(price);
			});

		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetAllPrices() {
		ResponseEntity<PriceList> response =
				restTemplate.getForEntity(BASE_URL + PORT + "/prices", PriceList.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
	}


	private static Price parsePriceObject(JSONObject obj) {
		int id = (Integer) obj.get("id");
		String currency = (String) obj.get("currency");
		double price = (double) obj.get("price");

		return new Price((long) id, currency, new BigDecimal(price), null);
	}
}
