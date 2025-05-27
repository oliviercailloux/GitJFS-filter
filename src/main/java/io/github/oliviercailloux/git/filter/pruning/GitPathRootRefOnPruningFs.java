package io.github.oliviercailloux.git.filter.pruning;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import io.github.oliviercailloux.git.filter.wrapping.GitPathRootRefOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaCachedOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitWrappingFs;
import io.github.oliviercailloux.gitjfs.GitPathRootRef;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class GitPathRootRefOnPruningFs extends GitPathRootRefOnWrappingFs {

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
