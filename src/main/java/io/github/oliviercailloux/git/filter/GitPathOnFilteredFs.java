package io.github.oliviercailloux.git.filter;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.ForwardingGitPath;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathSha;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import org.eclipse.jgit.api.Git;

/**
 * Similar to a GitPath (which it wraps and delegates to) except linked to a filteredFs.
 */
@Deprecated
sealed class GitPathOnFilteredFs extends ForwardingGitPath implements IGitPathOnFilteredFs
    permits GitPathShaOnFilteredFs, GitPathRefOnFilteredFs {

  static GitPathOnFilteredFs wrap(GitFilteringFs fs, GitPath delegate) {
    return new GitPathOnFilteredFs(fs, delegate);
  }

  private final GitFilteringFs fs;
  private final GitPath delegate;

  private GitPathOnFilteredFs absolute;

  GitPathOnFilteredFs(GitFilteringFs fs, GitPath delegate) {
    this.fs = checkNotNull(fs);
    this.delegate = checkNotNull(delegate);
    absolute = null;
  }

  private GitPathOnFilteredFs newWrapper(GitPath newDelegate) {
    return new GitPathOnFilteredFs(fs, newDelegate);
  }

  @Override
  public GitFilteringFs getFileSystem() {
    return fs;
  }

  @Override
  public GitPath delegate() {
    return delegate;
  }

  @Override
  public boolean equals(Object o2) {
    return ForwardingGitPath.defaultEquals(this, o2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fs, toString());
  }

  @Override
  public String toString() {
    return delegate().toString();
  }

  @Override
  public GitPathOnFilteredFs toAbsolutePath() {
    if (absolute == null) {
      absolute =
          delegate.toAbsolutePath().equals(delegate) ? this : newWrapper(delegate.toAbsolutePath());
    }
    return absolute;
  }

  @Override
  public GitPathRootOnFilteredFs getRoot() {
    GitPathRoot root = delegate.getRoot();
    if (root == null) {
      return null;
    }
    return GitPathRootOnFilteredFs.wrap(fs, root);
  }

  @Override
  public GitPathOnFilteredFs getFileName() {
    return newWrapper(delegate.getFileName());
  }

  @Override
  public GitPathOnFilteredFs getParent() {
    return newWrapper(delegate.getParent());
  }

  @Override
  public GitPathOnFilteredFs getName(int index) {
    return newWrapper(delegate.getName(index));
  }

  @Override
  public GitPathOnFilteredFs subpath(int beginIndex, int endIndex) {
    return newWrapper(delegate.subpath(beginIndex, endIndex));
  }

  @Override
  public GitPathOnFilteredFs normalize() {
    return newWrapper(delegate.normalize());
  }

  @Override
  public GitPathOnFilteredFs resolve(Path other) {
    return newWrapper(delegate.resolve(other));
  }

  @Override
  public GitPathOnFilteredFs relativize(Path other) {
    return newWrapper(delegate.relativize(other));
  }

  @Override
  public GitPathOnFilteredFs toRealPath(LinkOption... options) throws IOException {
    return newWrapper(delegate.toRealPath(options));
  }

  @Override
  public ImmutableList<GitPathRootSha> getParentCommits() throws NoSuchFileException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ImmutableList<GitPathSha> getParentShas() throws IOException, NoSuchFileException {
    return delegate.getParentShas().stream().filter(p -> {
      try {
        return fs.visible(p.toShaPath().getCommit());
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }).map(p -> GitPathShaOnFilteredFs.wrap(fs, p)).collect(ImmutableList.toImmutableList());
  }

  @Override
  public GitPathSha toShaPath() throws IOException, NoSuchFileException {
    return GitPathShaOnFilteredFs.wrap(fs, delegate.toShaPath());
  }
}
