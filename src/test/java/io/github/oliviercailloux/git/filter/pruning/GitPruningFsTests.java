package io.github.oliviercailloux.git.filter.pruning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.util.Set;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.jupiter.api.Test;

public class GitPruningFsTests {

  @Test
  void testPruneGraphWithEmptyInputs() {
    Graph<GitPathRootShaCached> emptyGraph = GraphBuilder.directed().build();
    ImmutableSet<ObjectId> invisibleStarts = ImmutableSet.of(ObjectId.fromString("a1b2c3d4e5f6789012345678901234567890abcd"));
    
    Graph<GitPathRootShaCached> result = GitPruningFs.pruneGraph(emptyGraph, invisibleStarts);
    
    assertTrue(result.nodes().isEmpty());
    assertEquals(0, result.edges().size());
  }
}
