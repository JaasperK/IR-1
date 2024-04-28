package org.example;

import javax.json.*;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MovieReader {

    public MovieReader() {
    }

    /**
     * Read movies from JSON files in directory 'moviesDir' formatted according to
     * 'example_movie_avatar.json'.
     * <p>
     * Each movie should contain the attributes: url, title, year, genreList,
     * countryList, description, budget, gross, ratingValue, ratingCount,
     * duration, castList, characterList
     * <p>
     * Each attribute is treated as a String and names ending in 'list' like
     * 'genreList' refer to JSON lists.
     *
     * @param moviesDir The directory containing the set of JSON files, each ending with a
     *                  suffix ".json".
     * @return A list of movies
     * @throws IOException
     */
    public List<Movie> readMoviesFrom(Path moviesDir) throws IOException {
        List<Movie> movies = new ArrayList<>();

        for (Path dirEntry : Files.newDirectoryStream(moviesDir)) {
            if (dirEntry.getFileName().toString().endsWith(".json")) {
                try (Reader fileReader = Files.newBufferedReader(dirEntry, StandardCharsets.UTF_8);
                     JsonReader jsonReader = Json.createReader(fileReader)) {


                    JsonObject m = jsonReader.readObject();
                    if (m instanceof JsonArray) {
                        if (((JsonArray) m).size() > 0) {
                            m = (JsonObject) m.get(0);
                        }
                    }
                    Movie obj = new Movie();
                    obj.setTitle(getString(m, "title"));
                    obj.setYear(getString(m, "year"));
                    obj.setUrl(getString(m, "url"));
                    obj.setGenreList(getJsonArray(m, "genreList"));
                    obj.setCountryList(getJsonArray(m, "countryList"));
                    obj.setDescription(getString(m, "description"));
                    obj.setBudget(getString(m, "budget"));
                    obj.setGross(getString(m, "gross"));
                    obj.setRatingValue(getString(m, "ratingValue"));
                    obj.setRatingCount(getString(m, "ratingCount"));
                    obj.setDuration(getString(m, "duration"));
                    obj.setCastList(getJsonArray(m, ("castList")));
                    obj.setCharacterList(getJsonArray(m, ("characterList")));
                    obj.setDirectorList(getJsonArray(m, "directorList"));
                    movies.add(obj);
                }
            }
        }

        return movies;
    }

    /**
     * A helper function to parse a JSON array.
     *
     * @param m   The JSON object, containing an array under the attribute 'key'.
     * @param key The key of the array
     * @return A list containing the Strings in the JSON object.
     */
    private List<String> getJsonArray(JsonObject m, String key) {
        try {
            JsonArray array = m.getJsonArray(key);
            List<String> result = new ArrayList<>();
            for (JsonValue v : array) {
                result.add(((JsonString) v).getString());
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * A helper function to parse a JSON String.
     *
     * @param m   The JSON object, containing a String under the attribute 'key'.
     * @param key The key of the array
     * @return The String in the JSON object.
     */
    private String getString(JsonObject m, String key) {
        try {
            String o = m.getString(key);
            if (o != null) {
                return o;
            }
        } finally {
            // Do nothing!
        }

        return "";
    }
}
