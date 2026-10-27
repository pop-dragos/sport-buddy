package ro.utcn.sportbuddy.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.catalog.model.Sport;

import java.util.List;

public interface SportRepository extends JpaRepository<Sport, Long> {
    List<Sport> findAllByOrderByIdAsc();
}
