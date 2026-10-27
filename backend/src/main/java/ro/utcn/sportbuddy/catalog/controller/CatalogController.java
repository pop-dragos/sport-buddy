package ro.utcn.sportbuddy.catalog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.utcn.sportbuddy.catalog.dto.CityDto;
import ro.utcn.sportbuddy.catalog.dto.FieldDto;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/sports")
    public List<SportDto> sports() {
        return catalogService.getSports();
    }

    @GetMapping("/cities")
    public List<CityDto> cities(@RequestParam(required = false) String query) {
        return catalogService.searchCities(query);
    }

    @GetMapping("/fields")
    public List<FieldDto> fields(@RequestParam Long cityId,
                                 @RequestParam(required = false) Long sportId) {
        return catalogService.getFields(cityId, sportId);
    }
}
