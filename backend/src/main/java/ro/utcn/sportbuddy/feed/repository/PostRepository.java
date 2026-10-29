package ro.utcn.sportbuddy.feed.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.feed.model.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDescIdDesc(Pageable page);

    List<Post> findBySportIdOrderByCreatedAtDescIdDesc(Long sportId, Pageable page);
}
