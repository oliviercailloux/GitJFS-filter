package io.github.oliviercailloux.git.filter.wrapping;

import io.github.oliviercailloux.gitjfs.GitPathRootRef;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import org.eclipse.jgit.lib.ObjectId;

public class GitPathRootRefOnWrappingFs extends GitPathRootOnWrappingFs implements GitPathRootRef {

  public static GitPathRootRefOnWrappingFs wrap(GitWrappingFs fs, GitPathRootRef delegate) {
    return new GitPathRootRefOnWrappingFs(fs, delegate);
  }

  protected GitPathRootRefOnWrappingFs(GitWrappingFs fs, GitPathRootRef delegate) {
    super(fs, delegate);
  }

  @Override
  GitPathRootRef delegate() {
    return (GitPathRootRef) super.delegate();
  }

  @Override
  public GitPathRootShaOnWrappingFs toSha() throws IOException, NoSuchFileException {
    return getFileSystem().wrap(delegate().toSha());
  }

  @Override
  @Deprecated
  public boolean isCommitId() {
    return false;
  }

  @Override
  @Deprecated
  public ObjectId getStaticCommitId() throws IllegalStateException {
    throw new IllegalStateException();
  }

  @Override
  @Deprecated
  public boolean isRef() {
    return true;
  }

  @Override
  public String getGitRef() {
    return delegate().getGitRef();
  }
}
