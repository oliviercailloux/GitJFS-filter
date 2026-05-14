package io.github.oliviercailloux.git.filter;

import io.github.oliviercailloux.gitjfs.GitPathRef;

final class GitPathRefOnFilteredFs extends GitPathOnFilteredFs
    implements GitPathRef, IGitPathOnFilteredFs {

  public static GitPathRefOnFilteredFs wrap(GitFilteringFs fs, GitPathRef delegate) {
    return new GitPathRefOnFilteredFs(fs, delegate);
  }

  private GitPathRefOnFilteredFs(GitFilteringFs fs, GitPathRef delegate) {
    super(fs, delegate);
  }

  @Override
  public GitPathRef delegate() {
    return (GitPathRef) super.delegate();
  }

  @Override
  public String getGitRef() {
    return delegate().getGitRef();
  }
}
