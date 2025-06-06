package io.github.oliviercailloux.git.filter.pruning;

import io.github.oliviercailloux.git.filter.wrapping.GitPathRootRefOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaOnWrappingFs;
import io.github.oliviercailloux.gitjfs.GitPathRootRef;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

class GitPathRootRefOnPruningFs extends GitPathRootRefOnWrappingFs {

  public static GitPathRootRefOnPruningFs wrap(GitPruningFs fs, GitPathRootRef delegate) {
    return new GitPathRootRefOnPruningFs(fs, delegate);
  }

  protected GitPathRootRefOnPruningFs(GitPruningFs fs, GitPathRootRef delegate) {
    super(fs, delegate);
  }

  @Override
  public GitPathRootShaOnWrappingFs toSha() throws IOException, NoSuchFileException {
    return toShaCached();
  }
}
