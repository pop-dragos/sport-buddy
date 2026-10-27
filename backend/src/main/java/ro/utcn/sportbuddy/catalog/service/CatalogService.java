package ro.utcn.sportbuddy.catalog.service;

import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.catalog.dto.CityDto;
import ro.utcn.sportbuddy.catalog.dto.FieldDto;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.model.Field;
import ro.utcn.sportbuddy.catalog.repository.CityRepository;
import ro.utcn.sportbuddy.catalog.repository.FieldRepository;
import ro.utcn.sportbuddy.catalog.repository.SportRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogService {

    private final SportRepository sportRepository;
    private final CityRepository cityRepository;
    private final FieldRepository fieldRepository;

    public CatalogService(SportRepository sportRepository, CityRepository cityRepository,
            FieldRepository fieldRepository) {
        this.sportRepository = sportRepository;
        this.cityRepository = cityRepository;
        this.fieldRepository = fieldRepository;
    }

    public List<SportDto> getSports() {
        return sportRepository.findAllByOrderByIdAsc().stream().map(SportDto::from).toList();
    }

    public List<CityDto> searchCities(String query) {
        if (query == null || query.isBlank()) {
            return cityRepository.findAllByOrderByNameAsc().stream().map(CityDto::from).toList();
        }
        return cityRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(query.trim())
                .stream().map(CityDto::from).toList();
    }

    public List<FieldDto> getFields(Long cityId, Long sportId) {
        List<Field> fields = sportId == null
                ? fieldRepository.findByCityIdOrderByNameAsc(cityId)
                : fieldRepository.findByCityIdAndSportIdOrderByNameAsc(cityId, sportId);
        return fields.stream().map(FieldDto::from).toList();
    }

    // Folosite de alte module (lobbies, weather), care nu au voie să acceseze repository-urile de aici.
    public Optional<CityDto> findCity(Long id) {
        return cityRepository.findById(id).map(CityDto::from);
    }

    public Optional<SportDto> findSport(Long id) {
        return sportRepository.findById(id).map(SportDto::from);
    }

    public Optional<FieldDto> findField(Long id) {
        return fieldRepository.findById(id).map(FieldDto::from);
    }
}
