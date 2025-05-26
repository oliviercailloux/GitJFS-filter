package io.github.oliviercailloux.git.filter.pruning;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import io.github.oliviercailloux.git.filter.wrapping.GitWrappingFs;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.jgit.lib.ObjectId;

/*
 * Prunes the graph of commits at given nodes called the invisible starts: all the children of the
 * invisible starts are invisible.<p>This implementation builds the graph once and use it to
 * determine whether a node is invisible (it’s also possible to traverse the DAG but this is
 * inefficient if multiple commits have to be considered).<p>Because doing almost anything useful
 * with a commit (such as reading a file) requires to determine whether it is visible, we will most
 * probably have to build the graph, so to simplify implementation, we build it from the start.
 */
public class GitPruningFs extends GitWrappingFs {

  public static GitPruningFs prune(GitWrappingFs delegate, Set<ObjectId> invisibleStarts)
      throws IOException {
    ImmutableGraph<GitPathRootShaCached> full = delegate.graph();
    return new GitPruningFs(delegate, pruneGraph(full, invisibleStarts));
  }

  static Graph<GitPathRootShaCached> pruneGraph(Graph<GitPathRootShaCached> fullGraph,
      Set<ObjectId> invisibleStarts) {

    Set<GitPathRootShaCached> visited = new HashSet<>();
    Set<ObjectId> invisiblesSoFar = new LinkedHashSet<>(invisibleStarts);
    MutableGraph<GitPathRootShaCached> prunedGraph = GraphBuilder.from(fullGraph).build();

    ImmutableSet<GitPathRootShaCached> roots =
        fullGraph.nodes().stream().filter(node -> fullGraph.predecessors(node).isEmpty())
            .collect(ImmutableSet.toImmutableSet());

    ArrayDeque<GitPathRootShaCached> lifo = new ArrayDeque<>(roots);
    ArrayDeque<GitPathRootShaCached> lifoInvisible = new ArrayDeque<>();

    while (!lifoInvisible.isEmpty() || !lifo.isEmpty()) {
      GitPathRootShaCached current;
      boolean visiblePart;
      if (!lifoInvisible.isEmpty()) {
        current = lifoInvisible.pop();
        visiblePart = false;
      } else {
        current = lifo.pop();
        visiblePart = !invisiblesSoFar.contains(current.getStaticCommitId());
      }
      if (visited.contains(current)) {
        continue;
      }
      visited.add(current);
      if (visiblePart) {
        prunedGraph.addNode(current);
      } else {
        invisiblesSoFar.add(current.getStaticCommitId());
      }
      ArrayDeque<GitPathRootShaCached> destination = visiblePart ? lifo : lifoInvisible;
      for (GitPathRootShaCached successor : fullGraph.successors(current)) {
        destination.push(successor);
        if (visiblePart && !invisiblesSoFar.contains(successor.getStaticCommitId())) {
          prunedGraph.putEdge(current, successor);
        }
      }
    }

    return ImmutableGraph.copyOf(prunedGraph);
  }

  private final ImmutableGraph<GitPathRootShaCached> graph;

  private GitPruningFs(GitWrappingFs delegate, Graph<GitPathRootShaCached> graph) {
    super(delegate);
    this.graph = ImmutableGraph.copyOf(graph);
  }

  @Override
  public ImmutableGraph<GitPathRootShaCached> graph() throws IOException {
    return graph;
  }
}
