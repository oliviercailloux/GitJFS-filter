package io.github.oliviercailloux.git.filter.pruning;

import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaOnWrappingFs;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

class GitPathRootShaOnPruningFs extends GitPathRootShaOnWrappingFs {

  public static GitPathRootShaOnPruningFs wrap(GitPruningFs fs, GitPathRootSha delegate) {
    return new GitPathRootShaOnPruningFs(fs, delegate);
  }

  protected GitPathRootShaOnPruningFs(GitPruningFs fs, GitPathRootSha delegate) {
    super(fs, delegate);
  }

  @Override
  public Commit getCommit() throws IOException, NoSuchFileException {
    return toShaCached().getCommit();
  }
  
  @Override
  public GitPathRootShaOnWrappingFs toSha() {
    return this;
  }
}
