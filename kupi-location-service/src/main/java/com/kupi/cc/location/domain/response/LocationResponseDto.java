package com.kupi.cc.location.domain.response;

import com.kupi.cc.location.domain.entity.Branch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Location ResponseDto
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDto implements ResponseDto {

    private String locationMapUrl;
    private Double latitude;
    private Double longitude;
    private String globalCode;
    private String city;
    private String country;
    /**
     * Distance from the user's location to this branch, in kilometers.
     */
    private Double distanceKm;
    /**
     * Distance from the user's location to this branch, in meters.
     */
    private Double distanceInMeters;

    public LocationResponseDto(Branch branch) {
        this.locationMapUrl = branch.getMapUrl();
        this.latitude = branch.getLatitude();
        this.longitude = branch.getLongitude();
        this.globalCode = branch.getGlobalCode();
        this.city = branch.getCity();
        this.country = branch.getCountry();
    }

    /**
     * Convenience constructor that also calculates distance from the given user location.
     *
     * @param branch   branch entity
     * @param userLat  user latitude
     * @param userLong user longitude
     */
    public LocationResponseDto(Branch branch, double userLat, double userLong) {
        this(branch);
        Double rawDistanceKm = calculateDistanceKm(userLat, userLong, branch.getLatitude(), branch.getLongitude());
        if (rawDistanceKm != null) {
            this.distanceKm = roundToTwoDecimals(rawDistanceKm);
            this.distanceInMeters = roundToTwoDecimals(rawDistanceKm * 1000);
        } else {
            this.distanceKm = null;
            this.distanceInMeters = null;
        }
    }

    /**
     * Calculates the great-circle distance between two points on Earth using the Haversine formula.
     *
     * @param lat1 latitude of point 1
     * @param lon1 longitude of point 1
     * @param lat2 latitude of point 2
     * @param lon2 longitude of point 2
     * @return distance in kilometers
     */
    private Double calculateDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }
        final int EARTH_RADIUS_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Rounds a double value to two decimal places.
     *
     * @param value value to round
     * @return rounded value with two decimals
     */
    private Double roundToTwoDecimals(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * This method converts object to json string for logging purpose.
     * PII data should be obfuscated.
     *
     * @return json string
     */
    @Override
    public String toLogJson() {
        return toJson();
    }
}
