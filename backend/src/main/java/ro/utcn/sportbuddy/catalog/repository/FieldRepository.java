package ro.utcn.sportbuddy.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.catalog.model.Field;

import java.util.List;

public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByCityIdOrderByNameAsc(Long cityId);

    List<Field> findByCityIdAndSportIdOrderByNameAsc(Long cityId, Long sportId);
}
