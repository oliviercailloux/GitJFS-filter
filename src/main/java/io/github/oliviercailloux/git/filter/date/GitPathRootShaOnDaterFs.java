package io.github.oliviercailloux.git.filter.date;

import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaOnWrappingFs;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

class GitPathRootShaOnDaterFs extends GitPathRootShaOnWrappingFs {

  public static GitPathRootShaOnDaterFs wrap(GitDaterFs fs, GitPathRootSha delegate) {
    return new GitPathRootShaOnDaterFs(fs, delegate);
  }

  protected GitPathRootShaOnDaterFs(GitDaterFs fs, GitPathRootSha delegate) {
    super(fs, delegate);
  }

  @Override
  public Commit getCommit() throws IOException, NoSuchFileException {
    return toShaCached().getCommit();
  }
  
}
