package io.github.oliviercailloux.git.filter.pruning;

import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaCachedOnWrappingFs;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class GitPathRootShaCachedOnPruningFs extends GitPathRootShaCachedOnWrappingFs {

  protected GitPathRootShaCachedOnPruningFs(GitPruningFs fs, GitPathRootShaCached delegate) {
    super(fs, delegate);
  }

  @Override
  public GitPruningFs getFileSystem() {
    return (GitPruningFs) super.getFileSystem();
  }

  @Override
  public GitPathRootShaOnPruningFs toSha() throws IOException, NoSuchFileException {
      GitPathRootShaCachedOnWrappingFs cached = toShaCached();
      getFileSystem().throwIfOwnInvisible(cached);
      return cached;
  }
}
