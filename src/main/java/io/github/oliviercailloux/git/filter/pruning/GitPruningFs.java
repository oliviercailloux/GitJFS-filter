package io.github.oliviercailloux.git.filter.pruning;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static io.github.oliviercailloux.jaris.exceptions.Unchecker.IO_UNCHECKER;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootRefOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaCachedOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitWrappingFs;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootRef;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import io.github.oliviercailloux.gitjfs.IGitFileSystem;
import io.github.oliviercailloux.jaris.exceptions.CheckedStream;
import io.github.oliviercailloux.jaris.graphs.GraphUtils;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import org.eclipse.jgit.diff.DiffEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Prunes the graph of commits at given nodes called the invisible starts: all the children of the
 * invisible starts are invisible.<p>This implementation builds the graph once and use it to
 * determine whether a node is invisible (it’s also possible to traverse the DAG to determine
 * whether a node is invisible but this is inefficient if multiple commits have to be considered).
 * Because doing almost anything useful with a commit (such as reading a file) requires to determine
 * whether it is visible, we will most probably have to build the graph, so to simplify
 * implementation, we build it from the start.
 */
public class GitPruningFs extends GitWrappingFs {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GitPruningFs.class);

  public static GitPruningFs prune(GitFileSystem delegate,
      Predicate<GitPathRootShaCached> invisibleStarts) throws IOException {
    ImmutableGraph<GitPathRootShaCached> full = delegate.graph();
    return new GitPruningFs(delegate, pruneGraph(full, invisibleStarts));
  }

  /**
   * Keeps only the nodes that are not the invisible starts or their children (successors)
   *
   * @param fullGraph a DAG
   * @param invisibleStarts
   * @return
   */
  static Graph<GitPathRootShaCached> pruneGraph(Graph<GitPathRootShaCached> fullGraph,
      Predicate<GitPathRootShaCached> invisibleStarts) {
    Set<GitPathRootShaCached> visiblesSoFar = new LinkedHashSet<>(fullGraph.nodes());
    Set<GitPathRootShaCached> seen = new HashSet<>();

    Deque<GitPathRootShaCached> lifo = new ArrayDeque<>();
    fullGraph.nodes().stream().filter(node -> fullGraph.predecessors(node).isEmpty())
        .forEach(lifo::push);

    while (!lifo.isEmpty()) {
      GitPathRootShaCached current = lifo.pop();

      if (invisibleStarts.test(current)) {
        Graphs.reachableNodes(fullGraph, current).forEach(visiblesSoFar::remove);
      }

      seen.add(current);

      for (GitPathRootShaCached successor : fullGraph.successors(current)) {
        if (!seen.contains(successor)) {
          lifo.push(successor);
        }
      }
    }

    LOGGER.debug("Started with nodes {}, ended with nodes {}.", fullGraph.nodes(), visiblesSoFar);
    return Graphs.inducedSubgraph(fullGraph, visiblesSoFar);
  }

  private final ImmutableGraph<GitPathRootShaCached> graph;

  private GitPruningFs(GitFileSystem delegate, Graph<GitPathRootShaCached> graph) {
    super(delegate);
    this.graph = ImmutableGraph.copyOf(GraphUtils.transform(graph, p -> super.wrapDoNotThrow(p)));
  }

  @Override
  protected GitPathRootShaCachedOnWrappingFs wrap(GitPathRootShaCached path)
      throws IOException, NoSuchFileException {
    GitPathRootShaCachedOnWrappingFs wrapped = super.wrap(path);
    if (!graph.nodes().contains(wrapped)) {
      throw new NoSuchFileException(wrapped.toString());
    }
    return wrapped;
  }

  @Override
  protected GitPathRootShaOnWrappingFs wrap(GitPathRootSha path) {
    return GitPathRootShaOnPruningFs.wrap(this, path);
  }

  @Override
  protected GitPathRootRefOnWrappingFs wrap(GitPathRootRef path) {
    return GitPathRootRefOnPruningFs.wrap(this, path);
  }

  @Override
  public ImmutableGraph<GitPathRootShaCached> graph() throws IOException {
    return graph;
  }

  @Override
  public ImmutableSet<GitPathRootRef> refs() throws IOException {
    final ImmutableSet<GitPathRootRef> refsWhole = super.refs();
    // return
    // CheckedStream.wrapping(refsWhole.stream()).map(GitPathRoot::toShaCached).map(GitPathRootSha
    // Cached::getCommit).filter(filter::test).collect(ImmutableSet.toImmutableSet());
    return CheckedStream.<GitPathRootRef, IOException>wrapping(refsWhole.stream())
        .filter(p -> graph.nodes().contains(p.toShaCached()))
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public ImmutableSet<DiffEntry> diff(GitPathRoot first, GitPathRoot second)
      throws IOException, NoSuchFileException {
    checkArgument(this.equals(first.getFileSystem()));
    checkArgument(this.equals(second.getFileSystem()));
    GitPathRootShaCached c1 = first.toShaCached();
    GitPathRootShaCached c2 = second.toShaCached();
    return super.diff(c1, c2);
  }

  @Override
  public ImmutableSet<Path> getRootDirectories() {
    final ImmutableGraph<GitPathRootShaCached> commitsGraph = IO_UNCHECKER.getUsing(this::graph);
    return ImmutableSet.copyOf(commitsGraph.nodes());
  }

  @Override
  public GitPruningFsProvider provider() {
    final GitFileSystem iDelegate = delegate();
    return new GitPruningFsProvider(iDelegate.provider());
  }
}
