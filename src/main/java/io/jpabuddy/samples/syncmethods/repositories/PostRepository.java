package io.jpabuddy.samples.syncmethods.repositories;

import io.jpabuddy.samples.syncmethods.entities.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths="tags")
    Optional<Post> findPostWithTagsById(Long id);

}