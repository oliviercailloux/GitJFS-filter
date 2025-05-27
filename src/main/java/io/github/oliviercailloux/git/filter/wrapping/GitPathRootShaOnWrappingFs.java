package io.github.oliviercailloux.git.filter.wrapping;

import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import org.eclipse.jgit.lib.ObjectId;

public class GitPathRootShaOnWrappingFs extends GitPathRootOnWrappingFs implements GitPathRootSha {

  public static GitPathRootShaOnWrappingFs wrap(GitWrappingFs fs, GitPathRootSha delegate) {
    return new GitPathRootShaOnWrappingFs(fs, delegate);
  }

  protected GitPathRootShaOnWrappingFs(GitWrappingFs fs, GitPathRootSha delegate) {
    super(fs, delegate);
  }

  @Override
  public GitPathRootSha delegate() {
    return (GitPathRootSha) super.delegate();
  }

  @Override
  @Deprecated
  public GitPathRootShaOnWrappingFs toSha() {
    return this;
  }

  @Override
  public GitPathRootShaCachedOnWrappingFs toShaCached() throws IOException, NoSuchFileException {
    return getFileSystem().wrap(delegate().toShaCached());
  }

  @Override
  @Deprecated
  public boolean isCommitId() {
    return true;
  }

  @Override
  public ObjectId getStaticCommitId() {
    return delegate().getStaticCommitId();
  }

  @Override
  @Deprecated
  public boolean isRef() {
    return false;
  }

  @Override
  @Deprecated
  public String getGitRef() throws IllegalStateException {
    throw new IllegalStateException();
  }
}
