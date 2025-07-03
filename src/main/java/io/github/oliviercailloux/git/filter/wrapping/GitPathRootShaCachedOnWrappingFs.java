package io.github.oliviercailloux.git.filter.wrapping;

import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;

public class GitPathRootShaCachedOnWrappingFs extends GitPathRootShaOnWrappingFs
    implements GitPathRootShaCached {

  public static GitPathRootShaCachedOnWrappingFs wrap(GitWrappingFs fs,
      GitPathRootShaCached delegate) {
    return new GitPathRootShaCachedOnWrappingFs(fs, delegate);
  }

  protected GitPathRootShaCachedOnWrappingFs(GitWrappingFs fs, GitPathRootShaCached delegate) {
    super(fs, delegate);
  }

  @Override
  public GitPathRootShaCached delegate() {
    return (GitPathRootShaCached) super.delegate();
  }

  @Override
  @Deprecated
  public GitPathRootShaCachedOnWrappingFs toShaCached() {
    return this;
  }

  @Override
  public Commit getCommit() {
    return delegate().getCommit();
  }
}
