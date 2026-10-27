package ro.utcn.sportbuddy.catalog.dto;

import ro.utcn.sportbuddy.catalog.model.Field;

public record FieldDto(Long id, String name, String district, Long cityId, Long sportId) {
    public static FieldDto from(Field f) {
        return new FieldDto(f.getId(), f.getName(), f.getDistrict(), f.getCityId(), f.getSportId());
    }
}
