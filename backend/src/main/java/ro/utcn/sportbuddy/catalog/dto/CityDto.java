package ro.utcn.sportbuddy.catalog.dto;

import ro.utcn.sportbuddy.catalog.model.City;

public record CityDto(Long id, String name, Double latitude, Double longitude) {
    public static CityDto from(City c) {
        return new CityDto(c.getId(), c.getName(), c.getLatitude(), c.getLongitude());
    }
}
