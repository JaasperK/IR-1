package org.example;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;


public class IMDBSpider {

	private static final HttpClient client = HttpClient.newHttpClient();

	public IMDBSpider() {}

	/**
	 * Helper method to submit an HTTP GET request.
	 * @param url Url to be searched
	 * @return Html as String
	 * @throws IOException If operation is interrupted
	 */
	private static String SubmitHttpGetRequest(String url) throws IOException {
		HttpRequest request;
		HttpResponse<String> response;

		request = HttpRequest
				.newBuilder()
				.uri(URI.create(url))
				.version(HttpClient.Version.HTTP_1_1)
				.header("User-Agent", "Mozilla/5.0")
				.header("Accept-Language", "en")
				.GET()
				.build();

		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return response.body();
		} catch (InterruptedException ie) {
			throw new IOException("Interrupted", ie);
		}
	}

	/**
	 * Helper method to evaluate XPath and return the i-th element of the result
	 * @param node Node to be evaluated
	 * @param xpath XPath expression
	 * @param i of element in Object[]
	 * @return First element of the evaluation result
	 * @throws XPatherException if evaluation fails
	 */
	private static TagNode XPathEval(TagNode node, String xpath, int i) throws XPatherException {
		Object[] elems = node.evaluateXPath(xpath);
		try {
			return (TagNode) elems[i];
		} catch (ArrayIndexOutOfBoundsException e) {
			return new TagNode("empty");
		}
	}

	private static TagNode[] CumulativeXPathEval(TagNode node, String xpath) throws XPatherException {
		Object[] elems = node.evaluateXPath(xpath);
		TagNode[] nodes = new TagNode[elems.length];
		for (int i = 0; i < elems.length; i++) {
			nodes[i] = (TagNode) elems[i];
		}
		return nodes;
	}

	private static String formatDuration(String duration) {
		// Format duration to min
		duration = duration.replaceAll("\\s|\"", "");  // matches whitespace an " characters
		int dur = 0;
		if (duration.indexOf('h') != -1) {
			dur = Integer.parseInt(duration.substring(0, duration.indexOf('h')));  // get hours
		}
		dur *= 60;
		if (duration.indexOf('m') != -1) {
			dur += Integer.parseInt(duration.substring(duration.indexOf('h') + 1, duration.indexOf('m')));  // get minutes
		}
		return Integer.toString(dur);
	}

	private static String formatBudgetGross(String value) {
		return value.replaceAll("[^0-9]", "");  // matches anything that is no number
	}

	private static String formatText(String text) {
		return text.replaceAll("&#x27;", "'");  // turn " ' " hmtl char to actual " ' "
	}

	/**
	 * For each title in file movieListJSON:
	 *
	 * <pre>
	 * You should:
	 * - First, read a list of 500 movie titles from the JSON file in 'movieListJSON'.
	 *
	 * - Secondly, for each movie title, perform a web search on IMDB and retrieve
	 * movie’s URL: https://www.imdb.com/find?q=<MOVIE>&s=tt&ttype=ft
	 *
	 * - Thirdly, for each movie, extract metadata (actors, budget, description)
	 * from movie’s URL and store to a JSON file in directory 'outputDir':
	 *    https://www.imdb.com/title/tt0499549/?ref_=fn_al_tt_1 for Avatar - store
	 * </pre>
	 *
	 * @param movieListJSON JSON file containing movie titles
	 * @param outputDir     output directory for JSON files with metadata of movies.
	 * @throws IOException  when IOException is thrown
	 */
	public void fetchIMDBMovies(String movieListJSON, String outputDir) throws IOException {
		long startTime = System.currentTimeMillis();
		FileInputStream fis = new FileInputStream(movieListJSON);
		JsonReader jReader = Json.createReader(fis);
		JsonArray jArr = jReader.readArray();

		jReader.close();
		fis.close();

		int count = 0;
		for (JsonValue jsonValue : jArr) {
			jsonValue = jArr.get(count);
			count += 1;
			String movieName = jsonValue.asJsonObject().get("movie_name").toString();
			String encName = URLEncoder.encode(movieName, StandardCharsets.UTF_8);
			String query = "https://www.imdb.com/find/?ref_=nv_sr_fn&q=" + encName + "&s=tt&ttype=ft";
			System.out.println(count + ". Search query: " + query);
			String response = SubmitHttpGetRequest(query);

			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode searchNode = cleaner.clean(response);

			try {
				TagNode elem = XPathEval(searchNode, "//ul//a[@class=\"ipc-metadata-list-summary-item__t\"]", 0);
				String url = "https://www.imdb.com" + elem.getAttributeByName("href");
				String moviePage = SubmitHttpGetRequest(url);
				TagNode moviePageNode = cleaner.clean(moviePage);

				JsonObjectBuilder jBuilder = Json.createObjectBuilder();
				jBuilder.add("url", url);

				TagNode titleNode = XPathEval(moviePageNode, "//span[@data-testid=\"hero__primary-text\"]", 0);
				jBuilder.add("title", formatText(titleNode.getText().toString()));

				TagNode[] yearDurationNode = CumulativeXPathEval(moviePageNode, "//ul[@class=\"ipc-inline-list ipc-inline-list--show-dividers sc-d8941411-2 cdJsTz baseAlt\"]/li");
				jBuilder.add("year", yearDurationNode[0].getText().toString());
				// some movies dont have PG so duration is last <li> in <ul>
				jBuilder.add("duration", formatDuration(yearDurationNode[yearDurationNode.length - 1].getText().toString()));

				TagNode ratingValueNode = XPathEval(moviePageNode, "//div[@data-testid=\"hero-rating-bar__aggregate-rating__score\"]/span", 0);
				jBuilder.add("ratingValue", ratingValueNode.getText().toString());

				TagNode descriptionNode = XPathEval(moviePageNode, "//span[@data-testid=\"plot-xl\"]", 0);
				jBuilder.add("description", formatText(descriptionNode.getText().toString()));

				TagNode budgetNode = XPathEval(moviePageNode, "//li[@data-testid=\"title-boxoffice-budget\"]/div//span", 0);
				jBuilder.add("budget", formatBudgetGross(budgetNode.getText().toString()));

				TagNode grossNode = XPathEval(moviePageNode, "//li[@data-testid=\"title-boxoffice-cumulativeworldwidegross\"]//span[@class=\"ipc-metadata-list-item__list-content-item\"]", 0);
				jBuilder.add("gross", formatBudgetGross(grossNode.getText().toString()));

                TagNode directorsNode = XPathEval(moviePageNode, "//section//li[@data-testid=\"title-pc-principal-credit\"]//ul", 0);
				List<? extends TagNode> directors = directorsNode.getElementListByName("li", true);
				JsonArrayBuilder jDirectorsBuilder = Json.createArrayBuilder();
				for (TagNode n : directors) {
					jDirectorsBuilder.add(formatText(n.getText().toString()));
				}
				jBuilder.add("directors", jDirectorsBuilder);

				TagNode[] cast = CumulativeXPathEval(moviePageNode, "//a[@data-testid=\"title-cast-item__actor\"]");
				JsonArrayBuilder jCastBuilder = Json.createArrayBuilder();
				for (TagNode n : cast) {
					jCastBuilder.add(formatText(n.getText().toString()));
				}
				jBuilder.add("cast", jCastBuilder);

				TagNode[] characters = CumulativeXPathEval(moviePageNode, "//a[@data-testid=\"cast-item-characters-link\"]");
				JsonArrayBuilder jCharsBuilder = Json.createArrayBuilder();
				for (TagNode n : characters) {
					jCharsBuilder.add(formatText(n.getText().toString()));
				}
				jBuilder.add("characters", jCharsBuilder);

				TagNode[] genres = CumulativeXPathEval(moviePageNode, "//div[@data-testid=\"genres\"]//span");
				JsonArrayBuilder jGenresBuilder = Json.createArrayBuilder();
				for (TagNode n : genres) {
					jGenresBuilder.add(formatText(n.getText().toString()));
				}
				jBuilder.add("genres", jGenresBuilder);

				TagNode[] countries = CumulativeXPathEval(moviePageNode, "//li[@data-testid=\"title-details-origin\"]//ul/li");
				JsonArrayBuilder jCountryBuilder = Json.createArrayBuilder();
				for (TagNode n : countries) {
					jCountryBuilder.add(formatText(n.getText().toString()));
				}

				jBuilder.add("countries", jCountryBuilder);
				JsonObject jMetadata = jBuilder.build();

				// write data into json
				try (FileWriter fileWriter = new FileWriter(outputDir + "/" + count + ".json")) {
					JsonWriterFactory writerFactory = Json.createWriterFactory(java.util.Map.of(JsonGenerator.PRETTY_PRINTING, true));
					JsonWriter jsonWriter = writerFactory.createWriter(fileWriter);
					jsonWriter.write(jMetadata);
					jsonWriter.close();
				}
			} catch (XPatherException e) {
				throw new IOException(e);
			}
//			break;  // just 1 iteration for test purposes
		}
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) * 1000);
	}

	/**
	 * Helper method to remove html and formatting from text.
	 *
	 * @param text The text to be cleaned
	 * @return clean text
	 */
	protected static String cleanText(String text) {
		return text.replaceAll("\\<.*?>", "").replace("&nbsp;", " ")
				.replace("\n", " ").replaceAll("\\s+", " ").trim();
	}

	public static void main(String[] argv) throws IOException {
		String moviesPath = "./data/movies.json";
		String outputDir = "./data";

		if (argv.length == 2) {
			moviesPath = argv[0];
			outputDir = argv[1];
		} else if (argv.length != 0) {
			System.out.println("Call with: IMDBSpider.jar <moviesPath> <outputDir>");
			System.exit(0);
		}

		IMDBSpider sp = new IMDBSpider();
		sp.fetchIMDBMovies(moviesPath, outputDir);
	}
}
