package io.jpabuddy.samples.syncmethods;

import io.jpabuddy.samples.syncmethods.entities.Post;
import io.jpabuddy.samples.syncmethods.entities.Tag;
import io.jpabuddy.samples.syncmethods.repositories.PostRepository;
import io.jpabuddy.samples.syncmethods.repositories.TagRepository;
import net.ttddyy.dsproxy.asserts.PreparedExecution;
import net.ttddyy.dsproxy.asserts.ProxyTestDataSource;
import net.ttddyy.dsproxy.asserts.QueryExecution;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestProxyDatasourceConfiguration.class)
@TestPropertySource(locations = {"classpath:test-application.properties"})
@Sql(scripts = "create-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "delete-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class SyncronizedMethodsApplicationTests {

    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ProxyTestDataSource ds;

    @BeforeEach
    void setUp() {
        //Clean up previous SQL statements from datasource log
        ds.reset();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testRemoveTagFromPostNonTransactional_ShouldSucceed() {
        Post post = postRepository.findPostWithTagsById(1L).orElseThrow();
        Tag tag = post.getTags().stream()
                .filter(t -> t.getId() == 1L)
                .findFirst().orElseThrow();
        post.removeTag(tag);
        postRepository.save(post);

        Collection<Tag> tags = postRepository.findPostWithTagsById(1L).orElseThrow().getTags();
        assertFalse(tags.contains(tag), "The tag %s should be deleted from post %s".formatted(tag, post));
    }

    @Test
    void testAddTagToPostNonTransactional_ShouldSucceed() {
        Post post = postRepository.findPostWithTagsById(1L).orElseThrow();
        Tag tag = tagRepository.findById(3L).orElseThrow();
        post.addTag(tag);
        postRepository.save(post);

        Collection<Tag> tags = postRepository.findPostWithTagsById(1L).orElseThrow().getTags();
        assertTrue(tags.contains(tag), "The tag %s should be added to post %s".formatted(tag, post));
    }

    @Test
    @Transactional
    void testAddTagToPostTransactional_ShouldNotDeleteData() {
        Post post = postRepository.findPostWithTagsById(1L).orElseThrow();
        Tag tag = tagRepository.findById(3L).orElseThrow();
        post.addTag(tag);
        postRepository.saveAndFlush(post);

        List<String> queryExecutions = ds.getQueryExecutions().stream()
                .filter(qe -> qe instanceof PreparedExecution)
                .map(pe -> ((PreparedExecution) pe).getQuery()).toList();

        List<String> selectQueries = queryExecutions.stream().filter(s -> s.trim().toLowerCase().startsWith("select")).toList();
        assertTrue(selectQueries.size() <= 2, "There should be less than two selects");

        List<String> insertQueries = queryExecutions.stream().filter(s -> s.trim().toLowerCase().startsWith("insert")).toList();
        assertEquals(1, insertQueries.size(), "There should be one insert");

        assertTrue(queryExecutions.stream().filter(qe -> qe.toLowerCase().contains("delete")).findAny().isEmpty(),
                "We should not execute delete statements to add tags");
    }


}