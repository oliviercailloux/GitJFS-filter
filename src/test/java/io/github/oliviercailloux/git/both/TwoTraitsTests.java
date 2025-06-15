package io.github.oliviercailloux.git.both;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableGraph;
import io.github.oliviercailloux.git.factory.FactoGit;
import io.github.oliviercailloux.git.filter.date.CommitDates;
import io.github.oliviercailloux.git.filter.date.GitDaterFs;
import io.github.oliviercailloux.git.filter.pruning.GitPruningFs;
import io.github.oliviercailloux.gitjfs.GitDfsFileSystem;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitFileSystemProvider;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.nio.file.NoSuchFileException;
import java.time.ZonedDateTime;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwoTraitsTests {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(TwoTraitsTests.class);

  @Test
  void testPruneAndDate() throws Exception {
    FactoGit facto = FactoGit.empty();
    facto.setSubDag();
    try (InMemoryRepository repo = facto.build()) {
      try (GitDfsFileSystem underlying =
          GitFileSystemProvider.instance().newFileSystemFromDfsRepository(repo)) {
        final ImmutableList<ObjectId> commits = underlying.graph().nodes().stream()
            .map(p -> p.getCommit().id()).collect(ImmutableList.toImmutableList()).reverse();
        assertEquals(3, commits.size());
        ObjectId i0 = commits.get(0);
        ObjectId i1 = commits.get(1);
        ObjectId i2 = commits.get(2);

        try (GitPruningFs middle =
            GitPruningFs.prune(underlying, c -> c.getCommit().id().equals(i2))) {
          ZonedDateTime now = ZonedDateTime.now();
          final ImmutableMap<ObjectId, CommitDates> fakeDates = ImmutableMap.of(i0,
              CommitDates.given(now), i1, CommitDates.givenAuthorDate(now), i2, CommitDates.none());
          try (GitFileSystem twoTraits =
              GitDaterFs.date(middle, p -> fakeDates.get(p.getStaticCommitId()))) {
            final ImmutableGraph<GitPathRootShaCached> graph = twoTraits.graph();
            LOGGER.debug("Middle: {}.", graph);
            assertEquals(2, graph.nodes().size());
            final ImmutableSet<ObjectId> middleIds = graph.nodes().stream()
                .map(p -> p.getCommit().id()).collect(ImmutableSet.toImmutableSet());
            assertEquals(ImmutableSet.of(i0, i1), middleIds);
            final GitPathRootShaCached c0 = twoTraits.getPathRoot(i0).toShaCached();
            final GitPathRootShaCached c1 = twoTraits.getPathRoot(i1).toShaCached();
            assertThrows(NoSuchFileException.class, () -> twoTraits.getPathRoot(i2).toShaCached());
            assertThrows(NoSuchFileException.class, () -> twoTraits.getPathRoot(i2).getCommit());
            assertEquals(ImmutableSet.of(c0), graph.predecessors(c1));
            assertEquals(ImmutableSet.of(), graph.predecessors(c0));
            assertEquals(now, c0.getCommit().authorDate());
            assertEquals(now, c0.getCommit().committerDate());
            assertEquals(now, c1.getCommit().authorDate());
            assertNotEquals(now, c1.getCommit().committerDate());
          }
        }
      }
    }
  }

  @Test
  void testDateAndPrune() throws Exception {
    FactoGit facto = FactoGit.empty();
    facto.setSubDag();
    try (InMemoryRepository repo = facto.build()) {
      try (GitDfsFileSystem underlying =
          GitFileSystemProvider.instance().newFileSystemFromDfsRepository(repo)) {
        final ImmutableList<ObjectId> commits = underlying.graph().nodes().stream()
            .map(p -> p.getCommit().id()).collect(ImmutableList.toImmutableList()).reverse();
        assertEquals(3, commits.size());
        ObjectId i0 = commits.get(0);
        ObjectId i1 = commits.get(1);
        ObjectId i2 = commits.get(2);

        ZonedDateTime now = ZonedDateTime.now();
        final ImmutableMap<ObjectId, CommitDates> fakeDates = ImmutableMap.of(i0,
            CommitDates.given(now), i1, CommitDates.givenAuthorDate(now), i2, CommitDates.none());
        try (GitFileSystem dated =
            GitDaterFs.date(underlying, p -> fakeDates.get(p.getStaticCommitId()))) {
        try (GitPruningFs twoTraits =
            GitPruningFs.prune(dated, c -> c.getCommit().id().equals(i2))) {
            final ImmutableGraph<GitPathRootShaCached> graph = twoTraits.graph();
            LOGGER.debug("Middle: {}.", graph);
            assertEquals(2, graph.nodes().size());
            final ImmutableSet<ObjectId> middleIds = graph.nodes().stream()
                .map(p -> p.getCommit().id()).collect(ImmutableSet.toImmutableSet());
            assertEquals(ImmutableSet.of(i0, i1), middleIds);
            final GitPathRootShaCached c0 = twoTraits.getPathRoot(i0).toShaCached();
            final GitPathRootShaCached c1 = twoTraits.getPathRoot(i1).toShaCached();
            assertThrows(NoSuchFileException.class, () -> twoTraits.getPathRoot(i2).toShaCached());
            assertThrows(NoSuchFileException.class, () -> twoTraits.getPathRoot(i2).getCommit());
            assertEquals(ImmutableSet.of(c0), graph.predecessors(c1));
            assertEquals(ImmutableSet.of(), graph.predecessors(c0));
            assertEquals(now, c0.getCommit().authorDate());
            assertEquals(now, c0.getCommit().committerDate());
            assertEquals(now, c1.getCommit().authorDate());
            assertNotEquals(now, c1.getCommit().committerDate());
          }
        }
      }
    }
  }
}
