package io.github.oliviercailloux.git.filter.pruning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GitPruneTests {

  @Test
  void testPruneGraphWithEmptyInputs() {
    Graph<GitPathRootShaCached> emptyGraph = GraphBuilder.directed().build();
    ImmutableSet<ObjectId> invisibleStarts =
        ImmutableSet.of(ObjectId.fromString("a1b2c3d4e5f6789012345678901234567890abcd"));

    Graph<GitPathRootShaCached> result =
        GitPruningFs.pruneGraph(emptyGraph, p -> invisibleStarts.contains(p.getStaticCommitId()));

    assertTrue(result.nodes().isEmpty());
    assertEquals(0, result.edges().size());
  }

  private static GitPathRootShaCached mockNode(String sha) {
    String fullSha = sha.repeat(40).substring(0, 40);
    GitPathRootShaCached node = Mockito.mock(GitPathRootShaCached.class);
    Mockito.when(node.getStaticCommitId()).thenReturn(ObjectId.fromString(fullSha));
    return node;
  }

  @Test
  void testPruneGraphIdentical() {
    MutableGraph<GitPathRootShaCached> graph = GraphBuilder.directed().build();

    GitPathRootShaCached root1 = mockNode("1");
    GitPathRootShaCached diamond1 = mockNode("2");
    GitPathRootShaCached diamond2 = mockNode("3");
    GitPathRootShaCached diamond3 = mockNode("4");

    GitPathRootShaCached root2 = mockNode("5");

    GitPathRootShaCached root3 = mockNode("6");
    GitPathRootShaCached line1 = mockNode("7");

    graph.addNode(root1);
    graph.addNode(diamond1);
    graph.addNode(diamond2);
    graph.addNode(diamond3);
    graph.putEdge(root1, diamond1);
    graph.putEdge(root1, diamond2);
    graph.putEdge(diamond1, diamond3);
    graph.putEdge(diamond2, diamond3);

    graph.addNode(root2);

    graph.addNode(root3);
    graph.addNode(line1);
    graph.putEdge(root3, line1);

    ImmutableSet<ObjectId> invisibleStarts = ImmutableSet.of();

    Graph<GitPathRootShaCached> result =
        GitPruningFs.pruneGraph(graph, p -> invisibleStarts.contains(p.getStaticCommitId()));

    assertEquals(7, result.nodes().size());
    assertEquals(5, result.edges().size());
    assertEquals(graph, result);
  }

  @Test
  void testPruneGraphRemoveRoot1() {
    MutableGraph<GitPathRootShaCached> graph = GraphBuilder.directed().build();

    GitPathRootShaCached root1 = mockNode("1");
    GitPathRootShaCached diamond1 = mockNode("2");
    GitPathRootShaCached diamond2 = mockNode("3");
    GitPathRootShaCached diamond3 = mockNode("4");

    GitPathRootShaCached root2 = mockNode("5");

    GitPathRootShaCached root3 = mockNode("6");
    GitPathRootShaCached line1 = mockNode("7");

    graph.putEdge(root1, diamond1);
    graph.putEdge(root1, diamond2);
    graph.putEdge(diamond1, diamond3);
    graph.putEdge(diamond2, diamond3);

    graph.addNode(root2);

    graph.putEdge(root3, line1);

    ImmutableSet<ObjectId> invisibleStarts = ImmutableSet.of(root1.getStaticCommitId());

    final Graph<GitPathRootShaCached> result =
        GitPruningFs.pruneGraph(graph, p -> invisibleStarts.contains(p.getStaticCommitId()));

    MutableGraph<GitPathRootShaCached> expected = GraphBuilder.directed().build();
    expected.addNode(root2);
    expected.addNode(root3);
    expected.addNode(line1);
    expected.putEdge(root3, line1);
    assertEquals(expected, result);
  }

  @Test
  void testPruneGraphRemoveRoot2() {
    MutableGraph<GitPathRootShaCached> graph = GraphBuilder.directed().build();

    GitPathRootShaCached root1 = mockNode("1");
    GitPathRootShaCached diamond1 = mockNode("2");
    GitPathRootShaCached diamond2 = mockNode("3");
    GitPathRootShaCached diamond3 = mockNode("4");

    GitPathRootShaCached root2 = mockNode("5");

    GitPathRootShaCached root3 = mockNode("6");
    GitPathRootShaCached line1 = mockNode("7");

    graph.putEdge(root1, diamond1);
    graph.putEdge(root1, diamond2);
    graph.putEdge(diamond1, diamond3);
    graph.putEdge(diamond2, diamond3);

    graph.addNode(root2);

    graph.putEdge(root3, line1);

    ImmutableSet<ObjectId> invisibleStarts = ImmutableSet.of(root2.getStaticCommitId());

    final Graph<GitPathRootShaCached> result =
        GitPruningFs.pruneGraph(graph, p -> invisibleStarts.contains(p.getStaticCommitId()));

    MutableGraph<GitPathRootShaCached> expected = GraphBuilder.directed().build();
    expected.putEdge(root1, diamond1);
    expected.putEdge(root1, diamond2);
    expected.putEdge(diamond1, diamond3);
    expected.putEdge(diamond2, diamond3);
    expected.addNode(root3);
    expected.addNode(line1);
    expected.putEdge(root3, line1);
    assertEquals(expected, result);
  }

  @Test
  void testPruneGraphRemoveDiamond2() {
    MutableGraph<GitPathRootShaCached> graph = GraphBuilder.directed().build();

    GitPathRootShaCached root1 = mockNode("1");
    GitPathRootShaCached diamond1 = mockNode("2");
    GitPathRootShaCached diamond2 = mockNode("3");
    GitPathRootShaCached diamond3 = mockNode("4");

    GitPathRootShaCached root2 = mockNode("5");

    GitPathRootShaCached root3 = mockNode("6");
    GitPathRootShaCached line1 = mockNode("7");

    graph.putEdge(root1, diamond1);
    graph.putEdge(root1, diamond2);
    graph.putEdge(diamond1, diamond3);
    graph.putEdge(diamond2, diamond3);

    graph.addNode(root2);

    graph.putEdge(root3, line1);

    ImmutableSet<ObjectId> invisibleStarts =
        ImmutableSet.of(diamond2.getStaticCommitId(), root3.getStaticCommitId());

    Graph<GitPathRootShaCached> result =
        GitPruningFs.pruneGraph(graph, p -> invisibleStarts.contains(p.getStaticCommitId()));

    MutableGraph<GitPathRootShaCached> expected = GraphBuilder.directed().build();
    expected.putEdge(root1, diamond1);
    expected.addNode(root2);
    assertEquals(expected, result);
  }

  @Test
  void testPruneGraphFailsToAddEdges() {
    MutableGraph<GitPathRootShaCached> graph = GraphBuilder.directed().build();

    GitPathRootShaCached root = mockNode("1");
    GitPathRootShaCached child1 = mockNode("2");
    GitPathRootShaCached child2 = mockNode("3");

    graph.putEdge(root, child1);
    graph.putEdge(child1, child2);

    ImmutableSet<ObjectId> invisibleStarts = ImmutableSet.of();

    Graph<GitPathRootShaCached> result =
        GitPruningFs.pruneGraph(graph, p -> invisibleStarts.contains(p.getStaticCommitId()));

    assertEquals(3, result.nodes().size());
    assertEquals(2, result.edges().size());
    assertTrue(result.hasEdgeConnecting(root, child1));
    assertTrue(result.hasEdgeConnecting(child1, child2));
  }

  @Test
  void testPruneGraphWithCyclicRevisit() {
    MutableGraph<GitPathRootShaCached> graph = GraphBuilder.directed().build();

    GitPathRootShaCached root1 = mockNode("1");
    GitPathRootShaCached root2 = mockNode("2");
    GitPathRootShaCached shared = mockNode("3");
    GitPathRootShaCached child = mockNode("4");

    graph.putEdge(root1, shared);
    graph.putEdge(root2, shared);
    graph.putEdge(shared, child);

    ImmutableSet<ObjectId> invisibleStarts = ImmutableSet.of(root2.getStaticCommitId());

    Graph<GitPathRootShaCached> result =
        GitPruningFs.pruneGraph(graph, p -> invisibleStarts.contains(p.getStaticCommitId()));

    MutableGraph<GitPathRootShaCached> expected = GraphBuilder.directed().build();
    expected.addNode(root1);

    assertEquals(expected, result);
    assertEquals(1, result.nodes().size());
    assertEquals(0, result.edges().size());
    assertTrue(result.nodes().contains(root1));
  }
}
