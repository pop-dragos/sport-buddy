package ro.utcn.sportbuddy.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.utcn.sportbuddy.feed.model.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAscIdAsc(Long postId);

    long countByPostId(Long postId);

    // [postId, număr] pentru toate postările dintr-o pagină, într-o singură interogare
    @Query("select c.postId, count(c) from Comment c where c.postId in :postIds group by c.postId")
    List<Object[]> countByPostIds(Collection<Long> postIds);
}
