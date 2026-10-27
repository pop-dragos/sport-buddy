package ro.utcn.sportbuddy.catalog.dto;

import ro.utcn.sportbuddy.catalog.model.Sport;

public record SportDto(Long id, String code, String name, Integer defaultCapacity, String venueLabel,
        boolean indoor) {
    public static SportDto from(Sport s) {
        return new SportDto(s.getId(), s.getCode(), s.getName(), s.getDefaultCapacity(), s.getVenueLabel(),
                s.isIndoor());
    }
}
