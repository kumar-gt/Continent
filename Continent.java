import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Continent {

    private static final Logger LOGGER = Logger.getLogger(Continent.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Continen> continentNameAndContinen = new HashMap<>();
    private final String[] continentSequenceArr = {"oceania", "asia", "africa", "south-america", "north-america", "europe"};
    private Map<String, City> cityNameAndCityMap;
    private Map<String, Set<City>> continentNameAndCityMap;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String cityName = scanner.nextLine().toUpperCase();
        Continent continent = new Continent();
        continent.init();
        City startCity = continent.getCityDetails(cityName);
        continent.findPath(startCity);
    }

    private City getCityDetails(String cityName) {
        return this.cityNameAndCityMap.get(cityName);
    }

    private void init() {
        try {
            cityNameAndCityMap = Continent.mapper.readValue(
                    new File("/tmp/escape/cities.json"),
                    new TypeReference<Map<String, City>>() {
                    });
            this.continentNameAndCityMap = this.cityNameAndCityMap.values().stream().collect(Collectors.groupingBy(City::getContId, Collectors.toSet()));
            for (String continentName : continentNameAndCityMap.keySet()) {
                Set<City> cityDetails = this.continentNameAndCityMap.get(continentName);
                DoubleSummaryStatistics doubleSummaryStatistics = cityDetails.stream().map(e -> e.getLocation().getLon()).mapToDouble(Double::new).summaryStatistics();
                Continen continen = new Continen(doubleSummaryStatistics.getMin(), doubleSummaryStatistics.getMax());
                this.continentNameAndContinen.put(continentName, continen);
            }
        } catch (IOException e) {
            Continent.LOGGER.log(Level.SEVERE, "", e);
        }
    }

    private void findPath(City startCity) {
        List<City> traversalpath = new ArrayList<>();
        double distance = 0d;
        int loop = 0;
        String inputLocationContinent = startCity.getContId();
        boolean start = false;
        for (int i = 0; i < continentSequenceArr.length; i++) {

            if (loop == 0 && this.continentSequenceArr[i].equals(inputLocationContinent)) {
                start = true;
                traversalpath.add(startCity);
                if (i == continentSequenceArr.length - 1) {
                    loop = 1;
                    i = -1;
                }
                continue;
            }
            if (start) {
                if (loop >= 1 && this.continentSequenceArr[i].equals(inputLocationContinent)) {
                    start = false;
                    traversalpath.add(startCity);
                    break;
                }
                City previousCity = traversalpath.get(traversalpath.size() - 1);
                double latitude = previousCity.getLocation().getLat();
                City nearestCity = this.findNearestCity(latitude, this.continentSequenceArr[i]);
                distance += this.getDistanceFromLatLonInKm(previousCity.getLocation().getLat(), previousCity.getLocation().getLon(), nearestCity.getLocation().getLat(), nearestCity.getLocation().getLon());
                traversalpath.add(nearestCity);

                if (i == continentSequenceArr.length - 1) {
                    loop = 1;
                    i = -1;
                }
            }
        }
        System.out.println(traversalpath.stream().map(e -> e.toString()).collect(Collectors.joining(" -> ")));
        System.out.println(String.format("Distance travelled: %f KMS", distance));

    }

    private double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = this.deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = this.deg2rad(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(this.deg2rad(lat1)) * Math.cos(this.deg2rad(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distance in km
        return d;
    }

    private double deg2rad(double deg) {
        return (deg * (Math.PI / 180));
    }

    private City findNearestCity(final double latitude, String continent) {
        City city = null;
        Continen continen = continentNameAndContinen.get(continent);
        Set<City> set = this.continentNameAndCityMap.get(continent);

        double requiredLongitude = 0d;
        if (continen.getMinLat() > latitude) {
            requiredLongitude = continen.getMinLat();
        } else if (continen.getMinLat() <= latitude) {
            if (continen.getMaxLat() >= latitude) {
                requiredLongitude = latitude;
            } else if (continen.getMaxLat() < latitude) {
                requiredLongitude = continen.getMaxLat();
            }
        }
        city = set.stream().min(Comparator.comparing(e -> Math.abs((int) e.getLocation().getLat() - (int) latitude))).orElse(null);
        return city;
    }

}

class City {
    private String id;
    private String name;
    private Location location;
    private String countryName;
    private String iata;
    private int rank;
    private String countryId;
    private String dest;
    private List<String> airports;
    private List<String> images;
    private double popularity;
    private String regId;
    private String contId;
    private String subId;
    private String terId;
    private int con;

    public City() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public List<String> getAirports() {
        return airports;
    }

    public void setAirports(List<String> airports) {
        this.airports = airports;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getContId() {
        return contId;
    }

    public void setContId(String contId) {
        this.contId = contId;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public String getTerId() {
        return terId;
    }

    public void setTerId(String terId) {
        this.terId = terId;
    }

    public int getCon() {
        return con;
    }

    public void setCon(int con) {
        this.con = con;
    }

    @Override
    public String toString() {
        return this.id + " (" +
                this.name + ", " +
                this.contId + ")";
    }
}

class Location {
    private double lat;
    private double lon;

    public Location() {
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "Location{" +
                "lat=" + this.lat +
                ", lon=" + this.lon +
                '}';
    }
}

class Continen {
    private double minLat;
    private double maxLat;

    public Continen() {
    }

    public Continen(double minLat, double maxLat) {
        this.minLat = minLat;
        this.maxLat = maxLat;
    }

    public double getMinLat() {
        return minLat;
    }

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }

    @Override
    public String toString() {
        return "Continen{" +
                "minLon=" + this.minLat +
                ", maxLon=" + this.maxLat +
                '}';
    }
}
