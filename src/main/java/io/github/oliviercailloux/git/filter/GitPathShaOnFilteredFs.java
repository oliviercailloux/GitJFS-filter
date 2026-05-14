package io.github.oliviercailloux.git.filter;

import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathSha;
import org.eclipse.jgit.lib.ObjectId;

final class GitPathShaOnFilteredFs extends GitPathOnFilteredFs
    implements GitPathSha, IGitPathOnFilteredFs {

  public static GitPathShaOnFilteredFs wrap(GitFilteringFs fs, GitPathSha delegate) {
    return new GitPathShaOnFilteredFs(fs, delegate);
  }

  private GitPathShaOnFilteredFs(GitFilteringFs fs, GitPathSha delegate) {
    super(fs, delegate);
  }

  @Override
  public GitPathSha delegate() {
    return (GitPathSha) super.delegate();
  }

  @Override
  public ObjectId getStaticCommitId() {
    return delegate().getStaticCommitId();
  }
}
