package io.github.oliviercailloux.git.filter.pruning;

import io.github.oliviercailloux.git.filter.wrapping.GitPathRootOnWrappingFs;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

abstract class GitPathRootOnPruningFs extends GitPathRootOnWrappingFs {

  protected GitPathRootOnPruningFs(GitPruningFs fs, GitPathRoot delegate) {
    super(fs, delegate);
  }

  @Override
  public Commit getCommit() throws IOException, NoSuchFileException {
    return toShaCached().getCommit();
  }
}
