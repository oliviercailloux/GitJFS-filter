package io.github.oliviercailloux.git.filter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.ForwardingGitPath;
import io.github.oliviercailloux.gitjfs.ForwardingGitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import io.github.oliviercailloux.gitjfs.GitPathSha;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jgit.lib.ObjectId;

/**
 * Similar to a {@link GitPathRootSha} (which it wraps and delegates to) except linked to a
 * filteredFs.
 */
@Deprecated
final class GitPathRootShaOnFilteredFs extends ForwardingGitPathRootSha
    implements IGitPathRootOnFilteredFs {

  static GitPathRootShaOnFilteredFs wrap(GitFilteringFs fs, GitPathRootSha delegate) {
    return new GitPathRootShaOnFilteredFs(fs, delegate);
  }

  private final GitFilteringFs fs;
  private final GitPathRootSha delegate;

  private GitPathRootShaOnFilteredFs(GitFilteringFs fs, GitPathRootSha delegate) {
    this.fs = checkNotNull(fs);
    this.delegate = checkNotNull(delegate);
  }

  @Deprecated
  @Override
  public GitPathRootSha toSha() {
    return this;
  }

  @Override
  public GitPathRootShaCachedOnFilteredFs toShaCached() throws IOException, NoSuchFileException {
    fs.graph();
    GitPathRootShaCached delegateCached = delegate.toShaCached();
    if (!fs.visible(delegateCached.getCommit())) {
      throw new NoSuchFileException(this.toString());
    }

    return GitPathRootShaCachedOnFilteredFs.wrap(fs, delegateCached);
  }

  @Override
  public GitFilteringFs getFileSystem() {
    return fs;
  }

  @Override
  public GitPathRootSha delegate() {
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
  @Deprecated
  public GitPathRootShaOnFilteredFs toAbsolutePath() {
    verify(delegate.toAbsolutePath().equals(delegate));
    return this;
  }

  @Override
  @Deprecated
  public GitPathRootShaOnFilteredFs getRoot() {
    verify(delegate.getRoot().equals(delegate));
    return this;
  }

  @Override
  @Deprecated
  public GitPathRootOnFilteredFs getFileName() {
    verify(delegate.getFileName() == null);
    return null;
  }

  @Override
  @Deprecated
  public GitPathRootShaOnFilteredFs getParent() {
    verify(delegate.getParent() == null);
    return null;
  }

  @Override
  public GitPathOnFilteredFs getName(int index) {
    return GitPathOnFilteredFs.wrap(fs, delegate.getName(index));
  }

  @Override
  public GitPathOnFilteredFs subpath(int beginIndex, int endIndex) {
    return GitPathOnFilteredFs.wrap(fs, delegate.subpath(beginIndex, endIndex));
  }

  @Override
  public GitPathOnFilteredFs normalize() {
    return GitPathOnFilteredFs.wrap(fs, delegate.normalize());
  }

  @Override
  public IGitPathOnFilteredFs resolve(Path other) {
    if (!getFileSystem().equals(other.getFileSystem())) {
      throw new IllegalArgumentException();
    }

    final IGitPathOnFilteredFs p2 = (IGitPathOnFilteredFs) other;

    if (other.isAbsolute()) {
      return p2;
    }

    return GitPathOnFilteredFs.wrap(fs, delegate.resolve(p2.delegate()));
  }

  @Override
  public GitPath resolve(String other) {
    return GitPathOnFilteredFs.wrap(fs, delegate.resolve(other));
  }

  @Override
  public GitPathOnFilteredFs relativize(Path other) {
    return GitPathOnFilteredFs.wrap(fs, delegate.relativize(other));
  }

  @Override
  public GitPathOnFilteredFs toRealPath(LinkOption... options) throws IOException {
    return GitPathOnFilteredFs.wrap(fs, delegate.toRealPath(options));
  }

  @Override
  public Commit getCommit() throws IOException, NoSuchFileException {
    final GitPathRootShaCachedOnFilteredFs cached = toShaCached();
    final Commit underlying = cached.delegate().getCommit();
    final Set<GitPathRootShaCached> filteredParents = fs.graph().predecessors(cached);
    final ImmutableList<ObjectId> filteredParentIds = filteredParents.stream()
        .map(GitPathRootSha::getStaticCommitId).collect(ImmutableList.toImmutableList());
    return Commit.from(underlying.id(), underlying.author(), underlying.committer(),
        filteredParentIds);
  }

  @Override
  public ImmutableList<GitPathRootSha> getParentCommits() throws IOException, NoSuchFileException {
    return ImmutableList.copyOf(fs.graph().predecessors(toShaCached()));
  }

  @Override
  public ImmutableList<GitPathSha> getParentShas() throws IOException, NoSuchFileException {
    return delegate.getParentShas().stream().filter(p -> {
      try {
        return fs.visible(p.getCommit());
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
