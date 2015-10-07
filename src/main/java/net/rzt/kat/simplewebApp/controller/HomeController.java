package net.rzt.kat.simplewebApp.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletResponse;

import net.rzt.kat.simplewebApp.service.CalendarSample;
import net.rzt.kat.simplewebApp.service.View;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import sun.net.www.URLConnection;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Lists;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

@Controller
public class HomeController {

	/**
	 * Be sure to specify the name of your application. If the application name
	 * is {@code null} or blank, the application will log a warning. Suggested
	 * format is "MyCompany-ProductName/1.0".
	 */
	private static final String APPLICATION_NAME = "go-to-meeting";

	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			System.getProperty("user.home"), ".store/calendar_sample");
	private static FileDataStoreFactory dataStoreFactory;
	private static HttpTransport httpTransport;
	private static final JsonFactory JSON_FACTORY = JacksonFactory
			.getDefaultInstance();
	private static com.google.api.services.calendar.Calendar client;
	static final java.util.List<Calendar> addedCalendarsUsingBatch = Lists
			.newArrayList();

	/** Authorizes the installed application to access user's protected data. */
	public static com.google.api.services.calendar.Calendar authorize(
			String accessToken, String clientId, String clientSecret,
			String refreshToken) {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		GoogleCredential credential = new GoogleCredential.Builder()
				.setClientSecrets(clientId, clientSecret)
				.setJsonFactory(jsonFactory).setTransport(httpTransport)
				.build();
		credential.setAccessToken(accessToken);
		credential.setRefreshToken(refreshToken);
		return new com.google.api.services.calendar.Calendar.Builder(
				httpTransport, jsonFactory, credential).build();

	}

	private static void showCalendars() throws IOException {
		View.header("Show Calendars");
		System.out.println("client" + client);
		System.out.println("Calendars Json--- >" + client.getJsonFactory());
		System.out.println("Calendars --- >"
				+ client.calendarList().list().execute());
		CalendarList feed = client.calendarList().list().execute();

		View.display(feed);
	}

	private static void addCalendarsUsingBatch() throws IOException {
		View.header("Add Calendars using Batch");
		BatchRequest batch = client.batch();

		// Create the callback.
		JsonBatchCallback<Calendar> callback = new JsonBatchCallback<Calendar>() {

			@Override
			public void onSuccess(Calendar calendar, HttpHeaders responseHeaders) {
				View.display(calendar);
				addedCalendarsUsingBatch.add(calendar);
			}

			@Override
			public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
				System.out.println("Error Message: " + e.getMessage());
			}
		};

		// Create 2 Calendar Entries to insert.
		Calendar entry1 = new Calendar().setSummary("Calendar for Testing 1");
		client.calendars().insert(entry1).queue(batch, callback);

		Calendar entry2 = new Calendar().setSummary("Calendar for Testing 2");
		client.calendars().insert(entry2).queue(batch, callback);

		batch.execute();
	}

	private static Calendar addCalendar() throws IOException {
		View.header("Add Calendar");
		Calendar entry = new Calendar();
		entry.setSummary("Calendar for Testing 3");
		Calendar result = client.calendars().insert(entry).execute();
		View.display(result);
		return result;
	}

	private static Calendar updateCalendar(Calendar calendar)
			throws IOException {
		View.header("Update Calendar");
		Calendar entry = new Calendar();
		entry.setSummary("Updated Calendar for Testing");
		Calendar result = client.calendars().patch(calendar.getId(), entry)
				.execute();
		View.display(result);
		return result;
	}

	private static void addEvent(Calendar calendar) throws IOException {
		View.header("Add Event");
		Event event = newEvent();
		Event result = client.events().insert(calendar.getId(), event)
				.execute();
		View.display(result);
	}

	private static Event newEvent() {
		Event event = new Event();
		event.setSummary("New Event");
		Date startDate = new Date();
		Date endDate = new Date(startDate.getTime() + 3600000);
		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
		event.setStart(new EventDateTime().setDateTime(start));
		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
		event.setEnd(new EventDateTime().setDateTime(end));
		return event;
	}

	private static void showEvents(Calendar calendar) throws IOException {
		View.header("Show Events");
		Events feed = client.events().list(calendar.getId()).execute();
		View.display(feed);
	}

	private static void deleteCalendarsUsingBatch() throws IOException {
		View.header("Delete Calendars Using Batch");
		BatchRequest batch = client.batch();
		for (Calendar calendar : addedCalendarsUsingBatch) {
			client.calendars().delete(calendar.getId())
					.queue(batch, new JsonBatchCallback<Void>() {

						@Override
						public void onSuccess(Void content,
								HttpHeaders responseHeaders) {
							System.out.println("Delete is successful!");
						}

						@Override
						public void onFailure(GoogleJsonError e,
								HttpHeaders responseHeaders) {
							System.out.println("Error Message: "
									+ e.getMessage());
						}
					});
		}

		batch.execute();
	}

	private static void deleteCalendar(Calendar calendar) throws IOException {
		View.header("Delete Calendar");
		client.calendars().delete(calendar.getId()).execute();
	}

	@RequestMapping(value = "/")
	public ModelAndView test(HttpServletResponse response) throws IOException {

		CalendarSample cs = new CalendarSample();
		cs.showCalendars();
		return new ModelAndView("home");
	}

	@RequestMapping(value = "/notification")
	public ModelAndView notification(HttpServletResponse request,
			HttpServletResponse response) throws IOException {
		System.out.println("request came for notification to pull the chages");
		ModelAndView mv = new ModelAndView("notificationPage");
		return mv;
	}

	@RequestMapping(value = "/postNotigy")
	public ModelAndView postNotigy(HttpServletResponse request,
			HttpServletResponse response) throws IOException {
		System.out.println("request came for notification to pull the chages");
		// <--------------------- Starting post ---------------------->
		String urlto = "https://www.googleapis.com/calendar/v3/users/me/calendarList/watch?maxResults=3&minAccessRole=owner&showDeleted=true&showHidden=true";
		
		String urlParameters = "&id=01234567-89ab-cdef-0123456789yi" + "&type=web_hook"
	    + "&address=https://ashwin.ind-cloud.everdata.com/notification";
		
	 
	   URL url = new URL(urlto);
	   URLConnection urlConn = (URLConnection) url.openConnection();
	   urlConn.setDoOutput(true);
	   OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
	   writer.write(urlParameters);
	   writer.flush();

	   String line, outputString = "";
	   BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
	   while( (line = reader.readLine()) != null )
	   {
	    outputString += line;
	    System.out.println("outputString" + outputString);
	   }
		// <--------------------- end of post ---------------------->
		ModelAndView mv = new ModelAndView("notification");
		return mv;
	}

	@RequestMapping(value = "/showCalendar")
	public ModelAndView showCalendar(HttpServletResponse request,
			HttpServletResponse response) throws IOException {
		showCalendars();

		ModelAndView mv = new ModelAndView("notified");
		mv.addObject("details", "ok ");
		return mv;
	}

	@RequestMapping(value = "/googlebab8ecfd012e8736.html")
	public ModelAndView googleCheck(HttpServletResponse request,
			HttpServletResponse response) throws IOException {

		ModelAndView mv = new ModelAndView("googlebab8ecfd012e8736");
		return mv;
	}

	@RequestMapping(value = "/dochanges")
	public ModelAndView dochanges(HttpServletResponse request,
			HttpServletResponse response) throws IOException {
		addCalendarsUsingBatch();
		Calendar calendar = addCalendar();
		updateCalendar(calendar);
		addEvent(calendar);
		showEvents(calendar);
		deleteCalendarsUsingBatch();
		deleteCalendar(calendar);
		ModelAndView mv = new ModelAndView("notified");
		mv.addObject("details", "ok ");
		return mv;
	}
}
