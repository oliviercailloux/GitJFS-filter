package io.github.oliviercailloux.git.filter;

import static com.google.common.base.Preconditions.checkArgument;
import static io.github.oliviercailloux.jaris.exceptions.Unchecker.IO_UNCHECKER;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHistoryUtils {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GitHistoryUtils.class);

  /**
   * TODO seems like an incorrect assumption (corrected somewhere else!)
   */
  public static ZonedDateTime getCreationTime(RevCommit commit) {
    /* https://stackoverflow.com/questions/11856983 */
    final PersonIdent authorIdent = commit.getAuthorIdent();
    final ZonedDateTime authorCreationTime = getCreationTime(authorIdent);
    final PersonIdent committerIdent = commit.getCommitterIdent();
    final ZonedDateTime committerCreationTime = getCreationTime(committerIdent);
    checkArgument(!authorCreationTime.isAfter(committerCreationTime),
        String.format("Author %s, creation time: %s, committer %s, creation time: %s",
            authorIdent.getName(), authorCreationTime, committerIdent.getName(),
            committerCreationTime));
    return committerCreationTime;
  }

  public static ZonedDateTime getCreationTime(PersonIdent ident) {
    final Date creationInstant = ident.getWhen();
    final TimeZone creationZone = ident.getTimeZone();
    final ZonedDateTime creationTime =
        ZonedDateTime.ofInstant(creationInstant.toInstant(), creationZone.toZoneId());
    return creationTime;
  }

  @SuppressWarnings("AbbreviationAsWordInName")
  public static ImmutableList<String> toOIds(Collection<RevCommit> commits) {
    return commits.stream().map(RevCommit::getName).collect(ImmutableList.toImmutableList());
  }

  /**
   * @param repository
   * @throws IOException
   * @throws IllegalArgumentException iff the given repository has no object database
   */
  public static GitHistory getHistory(Repository repository) throws IOException {
    checkArgument(repository.getObjectDatabase().exists());
    /*
     * Log command fails (with org.eclipse.jgit.api.errors.NoHeadException) if “No HEAD exists and
     * no explicit starting revision was specified”.
     */
    // if (!repository.getRefDatabase().hasRefs()) {
    // return GitHistory.create(GraphBuilder.directed().build(), ImmutableMap.of());
    // }

    final ImmutableSet<RevCommit> allCommits;
    /* Taken from GitFileSystem. */
    try (RevWalk walk = new RevWalk(repository)) {
      final List<Ref> refs = repository.getRefDatabase().getRefsByPrefix(Constants.R_REFS);
      walk.setRetainBody(true);
      for (Ref ref : refs) {
        walk.markStart(walk.parseCommit(ref.getLeaf().getObjectId()));
      }
      allCommits = ImmutableSet.copyOf(walk);
    }

    final Graph<ObjectId> graph = Graphs
        .transpose(GitHistoryUtilsSupport.asGraph(c -> Arrays.asList(c.getParents()), allCommits));
    final ImmutableMap<ObjectId, Instant> dates = allCommits.stream()
        .collect(ImmutableMap.toImmutableMap(c -> c, c -> getCreationTime(c).toInstant()));

    allCommits.stream().forEach(RevCommit::disposeBody);

    return GitHistory.create(graph, dates);
  }

  @Deprecated
  public static GitHistory getHistory(GitFileSystem gitFs) throws IOException {
    final ImmutableGraph<GitPathRootShaCached> graphOfPaths = gitFs.graph();

    final Function<GitPathRoot, Instant> getDate =
        IO_UNCHECKER.wrapFunction(p -> p.getCommit().committerDate().toInstant());

    try {
      final ImmutableGraph<ObjectId> graphOfIds =
          GitHistoryUtilsSupport.asImmutableGraph(graphOfPaths, GitPathRootSha::getStaticCommitId);

      final ImmutableMap<ObjectId, Instant> dates = graphOfPaths.nodes().stream()
          .collect(ImmutableMap.toImmutableMap(GitPathRootSha::getStaticCommitId, getDate));

      return GitHistory.create(graphOfIds, dates);
    } catch (UncheckedIOException e) {
      throw new IOException(e.getCause());
    }
  }
}
