package io.github.oliviercailloux.git.filter.pruning;

import static com.google.common.base.Preconditions.checkArgument;
import static io.github.oliviercailloux.jaris.exceptions.Unchecker.IO_UNCHECKER;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootRefOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaCachedOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitWrappingFs;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootRef;
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

/*
 * Prunes the graph of commits at given nodes called the invisible starts: all the children of the
 * invisible starts are invisible.<p>This implementation builds the graph once and use it to
 * determine whether a node is invisible (it’s also possible to traverse the DAG to
 * determine whether a node is invisible but this is
 * inefficient if multiple commits have to be considered). Because doing almost anything useful
 * with a commit (such as reading a file) requires to determine whether it is visible, we will most
 * probably have to build the graph, so to simplify implementation, we build it from the start.
 */
public class GitPruningFs extends GitWrappingFs {

  public static GitPruningFs prune(GitWrappingFs delegate,
      Predicate<GitPathRootShaCached> invisibleStarts) throws IOException {
    ImmutableGraph<GitPathRootShaCached> full = delegate.graph();
    return new GitPruningFs(delegate, pruneGraph(full, invisibleStarts));
  }

  /**
   * Keeps only the nodes that are not the invisible starts or their children (successors)
   * 
   * @param fullGraph a DAG
   * @param invisibleStarts may contain object ids that are not in the graph
   * @return
   */
  static Graph<GitPathRootShaCached> pruneGraph(Graph<GitPathRootShaCached> fullGraph,
      Predicate<GitPathRootShaCached> invisibleStarts) {

    Set<GitPathRootShaCached> visited = new HashSet<>();
    Set<GitPathRootShaCached> invisiblesSoFar = new LinkedHashSet<>();
    MutableGraph<GitPathRootShaCached> prunedGraph = GraphBuilder.from(fullGraph).build();

    ImmutableSet<GitPathRootShaCached> roots =
        fullGraph.nodes().stream().filter(node -> fullGraph.predecessors(node).isEmpty())
            .collect(ImmutableSet.toImmutableSet());

    /*
     * We could simplify by having one queue, with pairs (Sha, boolean) to keep context about
     * visibility of the part. Similarly, we could simplify by enqueing the predecessor together
     * with the node (thus storing triples)
     */
    Deque<GitPathRootShaCached> lifo = new ArrayDeque<>(roots);
    Deque<GitPathRootShaCached> lifoInvisible = new ArrayDeque<>();

    while (!lifoInvisible.isEmpty() || !lifo.isEmpty()) {
      GitPathRootShaCached current;
      boolean visiblePart;
      if (!lifoInvisible.isEmpty()) {
        current = lifoInvisible.pop();
        visiblePart = false;
      } else {
        current = lifo.pop();
        visiblePart = !invisibleStarts.test(current);
      }
      if (visited.contains(current) && !prunedGraph.nodes().contains(current)) {
        continue;
      }
      visited.add(current);
      if (visiblePart) {
        prunedGraph.addNode(current);
      } else {
        invisiblesSoFar.add(current);
        prunedGraph.removeNode(current);
      }
      Deque<GitPathRootShaCached> destination = visiblePart ? lifo : lifoInvisible;
      for (GitPathRootShaCached successor : ImmutableList.copyOf(fullGraph.successors(current))
          .reverse()) {
        destination.push(successor);
        if (visiblePart) {
          prunedGraph.putEdge(current, successor);
        }
      }
    }

    return ImmutableGraph.copyOf(prunedGraph);
  }

  private final ImmutableGraph<GitPathRootShaCached> graph;

  private GitPruningFs(GitWrappingFs delegate, Graph<GitPathRootShaCached> graph) {
    super(delegate);
    this.graph = ImmutableGraph.copyOf(GraphUtils.transform(graph, p -> super.wrapDoNotThrow(p)));
  }

  @Override
  protected GitPathRootShaCachedOnWrappingFs wrap(GitPathRootShaCached path)
      throws IOException, NoSuchFileException {
    GitPathRootShaCachedOnWrappingFs wrapped = super.wrap(path);
    if (graph.nodes().contains(path)) {
      throw new NoSuchFileException(path.toString());
    }
    return wrapped;
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
    final IGitFileSystem iDelegate = delegate();
    return new GitPruningFsProvider(iDelegate.provider());
  }
}
