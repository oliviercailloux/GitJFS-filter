package io.github.oliviercailloux.git.filter;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.oliviercailloux.gitjfs.ForwardingGitPath;
import io.github.oliviercailloux.gitjfs.GitPath;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Similar to a GitPath (which it wraps and delegates to) except linked to a filteredFs.
 */
final class GitPathOnFilteredFs extends ForwardingGitPath implements IGitPathOnFilteredFs {

  static GitPathOnFilteredFs wrap(GitFilteringFs fs, GitPath delegate) {
    return new GitPathOnFilteredFs(fs, delegate);
  }

  private final GitFilteringFs fs;
  private final GitPath delegate;

  private GitPathOnFilteredFs absolute;

  private GitPathOnFilteredFs(GitFilteringFs fs, GitPath delegate) {
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
    return GitPathRootOnFilteredFs.wrap(fs, delegate.getRoot());
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
}
