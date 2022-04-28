package io.jpabuddy.samples.syncmethods.repositories;

import io.jpabuddy.samples.syncmethods.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}