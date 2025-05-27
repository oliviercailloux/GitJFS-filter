package io.github.oliviercailloux.git.filter.pruning;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import io.github.oliviercailloux.git.filter.wrapping.GitPathRootRefOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaCachedOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaOnWrappingFs;
import io.github.oliviercailloux.gitjfs.GitPathRootRef;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class GitPathRootRefOnPruningFs extends GitPathRootRefOnWrappingFs {

  protected GitPathRootRefOnPruningFs(GitPruningFs fs, GitPathRootRef delegate) {
    super(fs, delegate);
  }

  @Override
  public GitPruningFs getFileSystem() {
    return (GitPruningFs) super.getFileSystem();
  }

  @Override
  public GitPathRootShaOnWrappingFs toSha() throws IOException, NoSuchFileException {
    return toShaCached();
  }

  void throwIfOwnInvisible(GitPathRootShaCachedOnPruningFs path)
      throws IOException, NoSuchFileException {
    checkArgument(path.getFileSystem() instanceof GitPruningFs);
    if (!getFileSystem().graph().nodes().contains(path)) {
      throw new NoSuchFileException(path.toString());
    }
  }

  @Override
  public GitPathRootShaCachedOnPruningFs toShaCached() throws IOException, NoSuchFileException {
    GitPathRootShaCachedOnWrappingFs shaCachedReturned = super.toShaCached();
    verify(shaCachedReturned instanceof GitPathRootShaCachedOnPruningFs);
    GitPathRootShaCachedOnPruningFs cached = (GitPathRootShaCachedOnPruningFs) shaCachedReturned;
    throwIfOwnInvisible(cached);
    return cached;
  }
}
