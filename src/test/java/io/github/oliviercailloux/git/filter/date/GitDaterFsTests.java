package io.github.oliviercailloux.git.filter.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.github.oliviercailloux.git.factory.FactoGit;
import io.github.oliviercailloux.gitjfs.GitDfsFileSystem;
import io.github.oliviercailloux.gitjfs.GitFileSystemProvider;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.junit.jupiter.api.Test;

public class GitDaterFsTests {
  @Test
  public void testCommitDates() throws Exception {
    ZonedDateTime now = ZonedDateTime.now();
    FactoGit facto = FactoGit.empty();
    facto.setSubDag();
    try (InMemoryRepository repo = facto.build()) {
      try (GitDfsFileSystem underlying =
          GitFileSystemProvider.instance().newFileSystemFromDfsRepository(repo)) {
        Set<GitPathRootShaCached> nodes = underlying.graph().nodes();
        try (GitDaterFs fs = GitDaterFs.date(underlying, p -> CommitDates.given(now))) {
          Iterator<GitPathRootShaCached> iterator = nodes.iterator();
          assertTrue(iterator.hasNext());
          iterator.next();
          assertTrue(iterator.hasNext());
          iterator.next();
          assertTrue(iterator.hasNext());
          final GitPathRootShaCached thirdNode = iterator.next();
          assertEquals(3, fs.graph().nodes().size());
          assertFalse(iterator.hasNext());
          final GitPathRootShaCached c0 =
              fs.getPathRoot(thirdNode.getStaticCommitId()).toShaCached();
          assertEquals(now, c0.getCommit().authorDate());
        }
      }
    }
  }

  @Test
  public void testCommitDatesEmpty() throws Exception {
    ZonedDateTime now = ZonedDateTime.now();
    FactoGit facto = FactoGit.empty();
    facto.setSubDag();
    try (InMemoryRepository repo = facto.build()) {
      try (GitDfsFileSystem underlying =
          GitFileSystemProvider.instance().newFileSystemFromDfsRepository(repo)) {
        Set<GitPathRootShaCached> nodes = underlying.graph().nodes();
        Iterator<GitPathRootShaCached> iterator = nodes.iterator();
        assertTrue(iterator.hasNext());
        GitPathRootShaCached u1 = iterator.next();
        assertTrue(iterator.hasNext());
        GitPathRootShaCached u2 = iterator.next();
        assertTrue(iterator.hasNext());
        final GitPathRootShaCached u3 = iterator.next();
        assertEquals(3, underlying.graph().nodes().size());
        ImmutableMap<GitPathRootShaCached, CommitDates> commitDatesMap = ImmutableMap.of(u1,
            CommitDates.given(now), u2, CommitDates.none(), u3, CommitDates.givenAuthorDate(now));
        try (GitDaterFs fs = GitDaterFs.date(underlying, commitDatesMap::get)) {
          assertFalse(iterator.hasNext());
          final GitPathRootShaCached d1 = fs.getPathRoot(u1.getStaticCommitId()).toShaCached();
          assertEquals(now, d1.getCommit().authorDate());
          assertEquals(now, d1.getCommit().committerDate());
          final GitPathRootShaCached d2 = fs.getPathRoot(u2.getStaticCommitId()).toShaCached();
          assertNotEquals(now, d2.getCommit().authorDate());
          assertNotEquals(now, d2.getCommit().committerDate());
          final GitPathRootShaCached d3 = fs.getPathRoot(u3.getStaticCommitId()).toShaCached();
          assertEquals(now, d3.getCommit().authorDate());
          assertNotEquals(now, d3.getCommit().committerDate());
        }
      }
    }
  }
}
