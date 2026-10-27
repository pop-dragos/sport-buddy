package ro.utcn.sportbuddy.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.catalog.model.City;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findAllByOrderByNameAsc();

    List<City> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String query);
}
