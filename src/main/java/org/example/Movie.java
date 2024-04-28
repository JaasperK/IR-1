package org.example;

import java.util.ArrayList;
import java.util.List;

public class Movie {

    private String title;
    private String year;
    private String url;
    private List<String> genreList = new ArrayList<>();
    private List<String> countryList = new ArrayList<>();
    private String description;
    private String budget;
    private String gross;
    private String ratingValue;
    private String ratingCount;
    private String duration;
    private List<String> castList = new ArrayList<>();
    private List<String> characterList = new ArrayList<>();
    private List<String> directorList = new ArrayList<>();

    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The year
     */
    public String getYear() {
        if (year == null || year.trim().equals("")) {
            return "0";
        }
        return year;
    }

    /**
     * @param year The year
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The genreList
     */
    public List<String> getGenreList() {
        return genreList;
    }

    /**
     * @param genreList The genreList
     */
    public void setGenreList(List<String> genreList) {
        this.genreList = genreList;
    }

    /**
     * @return The countryList
     */
    public List<String> getCountryList() {
        return countryList;
    }

    /**
     * @param countryList The countryList
     */
    public void setCountryList(List<String> countryList) {
        this.countryList = countryList;
    }

    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The budget
     */
    public String getBudget() {
        if (budget == null || budget.trim().equals("")) {
            return "0";
        }
        return budget;
    }

    /**
     * @param budget The budget
     */
    public void setBudget(String budget) {
        this.budget = budget;
    }

    /**
     * @return The gross
     */
    public String getGross() {
        if (gross == null || gross.trim().equals("")) {
            return "0";
        }
        return gross;
    }

    /**
     * @param gross The gross
     */
    public void setGross(String gross) {
        this.gross = gross;
    }

    /**
     * @return The ratingValue
     */
    public String getRatingValue() {
        if (ratingValue == null || ratingValue.trim().equals("")) {
            return "0";
        }
        return ratingValue;
    }

    /**
     * @param ratingValue The ratingValue
     */
    public void setRatingValue(String ratingValue) {
        this.ratingValue = ratingValue;
    }

    /**
     * @return The ratingCount
     */
    public String getRatingCount() {
        if (ratingCount == null || ratingCount.trim().equals("")) {
            return "0";
        }
        return ratingCount;
    }

    /**
     * @param ratingCount The ratingCount
     */
    public void setRatingCount(String ratingCount) {
        this.ratingCount = ratingCount;
    }

    /**
     * @return The duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @param duration The duration
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * @return The castList
     */
    public List<String> getCastList() {
        return castList;
    }

    /**
     * @param castList The castList
     */
    public void setCastList(List<String> castList) {
        this.castList = castList;
    }

    /**
     * @return The characterList
     */
    public List<String> getCharacterList() {
        return characterList;
    }

    /**
     * @param characterList The characterList
     */
    public void setCharacterList(List<String> characterList) {
        this.characterList = characterList;
    }

    /**
     * @return The directorList
     */
    public List<String> getDirectorList() {
        return directorList;
    }

    /**
     * @param directorList The directorList
     */
    public void setDirectorList(List<String> directorList) {
        this.directorList = directorList;
    }

    @Override
    public String toString() {
        return title;
    }
}
