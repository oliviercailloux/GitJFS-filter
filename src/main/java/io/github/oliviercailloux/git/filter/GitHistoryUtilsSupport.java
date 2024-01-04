package io.github.oliviercailloux.git.filter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Verify;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.SuccessorsFunction;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

/** Copy of methods from st-project utils class. To be removed. */
public class GitHistoryUtilsSupport {

  /**
   * @deprecated use the jaris version (and remove the copy of this one in GitHistory?)
   */
  public static <E, F extends E> Graph<E> asGraph(SuccessorsFunction<F> successorsFunction,
      Set<F> roots) {
    checkNotNull(successorsFunction);
    checkNotNull(roots);
    checkArgument(roots.stream().allMatch(t -> t != null));

    final Queue<F> toConsider = new LinkedList<>(roots);
    final Set<F> seen = new LinkedHashSet<>(roots);

    final MutableGraph<E> mutableGraph = GraphBuilder.directed().build();
    while (!toConsider.isEmpty()) {
      final F current = toConsider.remove();
      Verify.verify(current != null);
      mutableGraph.addNode(current);
      final Iterable<? extends F> successors = successorsFunction.successors(current);
      for (F successor : successors) {
        checkArgument(successor != null);
        mutableGraph.putEdge(current, successor);
        if (!seen.contains(successor)) {
          toConsider.add(successor);
          seen.add(successor);
        }
      }
    }
    return mutableGraph;
  }

  public static <E> ImmutableGraph<E> asImmutableGraph(Graph<E> graph) {
    if (graph instanceof ImmutableGraph) {
      return (ImmutableGraph<E>) graph;
    }
    Function<E, E> transformer = Function.identity();
    return asImmutableGraph(graph, transformer);
  }

  /**
   * @deprecated see the Jaris version
   */
  public static <E, F> ImmutableGraph<F> asImmutableGraph(Graph<E> graph,
      Function<E, F> transformer) {
    final GraphBuilder<Object> startBuilder =
        graph.isDirected() ? GraphBuilder.directed() : GraphBuilder.undirected();
    startBuilder.allowsSelfLoops(graph.allowsSelfLoops());
    final ImmutableGraph.Builder<F> builder = startBuilder.immutable();
    final Set<E> nodes = graph.nodes();
    for (E node : nodes) {
      builder.addNode(transformer.apply(node));
    }
    final Set<EndpointPair<E>> edges = graph.edges();
    for (EndpointPair<E> edge : edges) {
      builder.putEdge(transformer.apply(edge.source()), transformer.apply(edge.target()));
    }
    return builder.build();
  }
}
