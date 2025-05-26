package io.github.oliviercailloux.git.filter.wrapping;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import org.eclipse.jgit.lib.ObjectId;

/*
 * A GitPathRoot linked to a GitWrappingFs, that delegates to another GitPathRoot, except that all the paths created by the delegate are
 * wrapped by the linked FS, in order to be associated to the linked FS.
 * <p>
 * When the delegate produces a path, we wrap it. When we receive an existing path (thus produced by this fs), we get the delegate path and pass it to the delegate fs.
 */
public class GitPathRootOnWrappingFs extends GitPathOnWrappingFs implements GitPathRoot {

  public static GitPathRootOnWrappingFs wrap(GitWrappingFs fs, GitPathRoot delegate) {
    return new GitPathRootOnWrappingFs(fs, delegate);
  }

  protected GitPathRootOnWrappingFs(GitWrappingFs fs, GitPathRoot delegate) {
    super(fs, delegate);
  }

  @Override
  public GitPathRoot delegate() {
    return (GitPathRoot) super.delegate();
  }

  @Override
  public GitPathRootShaOnWrappingFs toSha() throws IOException, NoSuchFileException {
    return getFileSystem().wrap(delegate().toSha());
  }

  @Override
  public GitPathRootShaCachedOnWrappingFs toShaCached() throws IOException, NoSuchFileException {
    return getFileSystem().wrap(delegate().toShaCached());
  }

  @Override
  public boolean isCommitId() {
    return delegate().isCommitId();
  }

  @Override
  public ObjectId getStaticCommitId() {
    return delegate().getStaticCommitId();
  }

  @Override
  public boolean isRef() {
    return delegate().isRef();
  }

  @Override
  public String getGitRef() {
    return delegate().getGitRef();
  }

  @Override
  public Commit getCommit() throws IOException, NoSuchFileException {
    return delegate().getCommit();
  }

  @Override
  public ImmutableList<GitPathRootSha> getParentCommits() throws IOException, NoSuchFileException {
    return delegate().getParentCommits().stream()
        .map(p -> getFileSystem().wrap(p))
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  @Deprecated
  public GitPathRootOnWrappingFs toAbsolutePath() {
    return this;
  }

  @Override
  @Deprecated
  public GitPathRootOnWrappingFs getRoot() {
    return this;
  }

  @Override
  @Deprecated
  public GitPathRootOnWrappingFs getParent() {
    return null;
  }

  @Override
  @Deprecated
  public GitPathRootOnWrappingFs getFileName() {
    return null;
  }
}
