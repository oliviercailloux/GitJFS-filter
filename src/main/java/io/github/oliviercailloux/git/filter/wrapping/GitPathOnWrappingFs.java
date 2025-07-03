package io.github.oliviercailloux.git.filter.wrapping;

import static com.google.common.base.Preconditions.checkNotNull;

import io.github.oliviercailloux.gitjfs.ForwardingGitPath;
import io.github.oliviercailloux.gitjfs.GitPath;
import java.io.IOException;
import java.net.URI;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;

/*
 * A GitPath linked to a GitWrappingFs, that delegates to another GitPath, except that all the paths
 * created by the delegate are wrapped by the linked FS, in order to be associated to the linked FS.
 * <p> When the delegate produces a path, we wrap it. When we receive an existing path produced by
 * this FS, we get the delegate path and pass it to the delegate FS.
 */
public class GitPathOnWrappingFs implements GitPath {

  public static GitPathOnWrappingFs wrap(GitWrappingFs fs, GitPath delegate) {
    return new GitPathOnWrappingFs(fs, delegate);
  }

  private final GitWrappingFs fs;
  private final GitPath delegate;

  protected GitPathOnWrappingFs(GitWrappingFs fs, GitPath delegate) {
    this.fs = checkNotNull(fs);
    this.delegate = checkNotNull(delegate);
  }

  @Override
  public GitWrappingFs getFileSystem() {
    return fs;
  }

  public GitPath delegate() {
    return delegate;
  }

  @Override
  public GitPathOnWrappingFs toAbsolutePath() {
    return delegate.toAbsolutePath().equals(delegate) ? this
        : getFileSystem().wrap(delegate.toAbsolutePath());
  }

  @Override
  public GitPathRootOnWrappingFs getRoot() {
    return getFileSystem().wrap(delegate.getRoot());
  }

  @Override
  public int getNameCount() {
    return delegate.getNameCount();
  }

  @Override
  public GitPathOnWrappingFs getFileName() {
    return getFileSystem().wrap(delegate.getFileName());
  }

  @Override
  public GitPathOnWrappingFs getParent() {
    return getFileSystem().wrap(delegate.getParent());
  }

  @Override
  public GitPathOnWrappingFs getName(int index) {
    return getFileSystem().wrap(delegate.getName(index));
  }

  @Override
  public GitPathOnWrappingFs subpath(int beginIndex, int endIndex) {
    return getFileSystem().wrap(delegate.subpath(beginIndex, endIndex));
  }

  @Override
  public GitPathOnWrappingFs normalize() {
    return getFileSystem().wrap(delegate.normalize());
  }

  @Override
  public GitPathOnWrappingFs resolve(Path other) {
    return getFileSystem().wrap(delegate.resolve(GitWrappingFsProvider.delegateOrOriginal(other)));
  }

  @Override
  public GitPath resolve(String other) {
    return getFileSystem().wrap(delegate.resolve(other));
  }

  @Override
  public GitPathOnWrappingFs relativize(Path other) {
    return getFileSystem()
        .wrap(delegate.relativize(GitWrappingFsProvider.delegateOrOriginal(other)));
  }

  @Override
  public GitPathOnWrappingFs toRealPath(LinkOption... options) throws IOException {
    return getFileSystem().wrap(delegate.toRealPath(options));
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
  public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers)
      throws IOException {
    return delegate.register(watcher, events, modifiers);
  }

  @Override
  public boolean isAbsolute() {
    return delegate.isAbsolute();
  }

  @Override
  public boolean startsWith(Path other) {
    return delegate.startsWith(GitWrappingFsProvider.delegateOrOriginal(other));
  }

  @Override
  public boolean endsWith(Path other) {
    return delegate.endsWith(GitWrappingFsProvider.delegateOrOriginal(other));
  }

  @Override
  public URI toUri() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(Path other) {
    return delegate.compareTo(GitWrappingFsProvider.delegateOrOriginal(other));
  }
}
