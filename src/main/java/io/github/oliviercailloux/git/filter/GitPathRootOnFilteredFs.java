package io.github.oliviercailloux.git.filter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.ForwardingGitPath;
import io.github.oliviercailloux.gitjfs.ForwardingGitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import io.github.oliviercailloux.gitjfs.GitPathSha;
import io.github.oliviercailloux.gitjfs.impl.GitPathImpl;
import io.github.oliviercailloux.jaris.exceptions.CheckedStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similar to a GitPathRoot (which it wraps and delegates to) except linked to a filteredFs.
 */
@Deprecated
final class GitPathRootOnFilteredFs extends ForwardingGitPathRoot
    implements IGitPathRootOnFilteredFs {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(GitPathRootOnFilteredFs.class);

  static GitPathRootOnFilteredFs wrap(GitFilteringFs fs, GitPathRoot delegate) {
    return new GitPathRootOnFilteredFs(fs, delegate);
  }

  private final GitFilteringFs fs;
  private final GitPathRoot delegate;

  private GitPathRootOnFilteredFs(GitFilteringFs fs, GitPathRoot delegate) {
    this.fs = checkNotNull(fs);
    this.delegate = checkNotNull(delegate);
  }

  @Override
  public GitFilteringFs getFileSystem() {
    return fs;
  }

  @Override
  public GitPathRoot delegate() {
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

  @Deprecated
  @Override
  public GitPathRootSha toSha() throws IOException, NoSuchFileException {
    GitPathRootSha delegateCached = delegate.toSha();
    if (!fs.visible(delegateCached.getCommit())) {
      throw new NoSuchFileException(this.toString());
    }

    return GitPathRootShaOnFilteredFs.wrap(getFileSystem(), delegateCached);
  }

  @Override
  public GitPathRootShaCachedOnFilteredFs toShaCached() throws IOException, NoSuchFileException {
    fs.graph();
    GitPathRootShaCached delegateCached = delegate.toShaCached();
    if (!fs.visible(delegateCached.getCommit())) {
      throw new NoSuchFileException(this.toString());
    }

    return GitPathRootShaCachedOnFilteredFs.wrap(getFileSystem(), delegateCached);
  }

  @Deprecated
  @Override
  public GitPathRoot toAbsolutePath() {
    verify(delegate.toAbsolutePath().equals(delegate));
    return this;
  }

  @Deprecated
  @Override
  public IGitPathRootOnFilteredFs getRoot() {
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
  public GitPathRoot getParent() {
    verify(delegate.getParent() == null);
    return null;
  }

  @Override
  public GitPath getName(int index) {
    return GitPathOnFilteredFs.wrap(fs, delegate.getName(index));
  }

  @Override
  public GitPath subpath(int beginIndex, int endIndex) {
    return GitPathOnFilteredFs.wrap(fs, delegate.subpath(beginIndex, endIndex));
  }

  @Override
  public GitPath normalize() {
    return GitPathOnFilteredFs.wrap(fs, delegate.normalize());
  }

  @Override
  public GitPath resolve(Path other) {
    /* We can probably return an IGitPathRootOnFilteredFs here */
    /*
     * If the general contract permits this, we could in principle also work with a non filtered
     * other path (then the return type cannot be a filtered path).
     */
    if (!getFileSystem().equals(other.getFileSystem())) {
      throw new IllegalArgumentException();
    }

    final GitPathRootOnFilteredFs p2 = (GitPathRootOnFilteredFs) other;

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
  public GitPath relativize(Path other) {
    return GitPathOnFilteredFs.wrap(fs, delegate.relativize(other));
  }

  @Override
  public GitPath toRealPath(LinkOption... options) throws IOException {
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
  public ImmutableList<GitPathRootSha> getParentCommits() throws NoSuchFileException, IOException {
    /*
     * Requires to compute a (local) transitive reduction. Unless going for very complex
     * implementation, this requires to compute the whole filtered graph.
     */
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
