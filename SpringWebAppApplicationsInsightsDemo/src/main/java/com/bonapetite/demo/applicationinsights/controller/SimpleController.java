package com.bonapetite.demo.applicationinsights.controller;

import com.bonapetite.demo.applicationinsights.model.Person;
import com.microsoft.applicationinsights.TelemetryClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class SimpleController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Random rand = new Random();
	private TelemetryClient telemetry = new TelemetryClient();

	@RequestMapping(value = "/hello", method = { RequestMethod.GET })
	public @ResponseBody Person hello(@RequestParam(value = "name") String name) {
		Person person = new Person();
		person.setName(name);
		int mood = rand.nextInt(10);
		person.setMood(mood);

		// Output to Application Insights Appender
		try {
			logger.trace("Greetings - " + new ObjectMapper().writeValueAsString(person));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Custom telemetry to Applicaiton Insights
		Map<String, Double> metrics = new HashMap<String, Double>();
		metrics.put("mood", new Double(mood)); // random mood out of 10
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("name", name);
		telemetry.trackEvent("greeting", properties, metrics);

		return person;
	}

}