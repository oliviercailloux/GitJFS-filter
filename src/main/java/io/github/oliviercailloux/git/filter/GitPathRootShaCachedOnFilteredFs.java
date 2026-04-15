package io.github.oliviercailloux.git.filter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static io.github.oliviercailloux.jaris.exceptions.Unchecker.IO_UNCHECKER;

import com.google.common.collect.ImmutableList;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.ForwardingGitPath;
import io.github.oliviercailloux.gitjfs.ForwardingGitPathRootShaCached;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
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
 * Similar to a {@link GitPathRootShaCached} (which it wraps and delegates to) except linked to a
 * filteredFs.
 */
@Deprecated
final class GitPathRootShaCachedOnFilteredFs extends ForwardingGitPathRootShaCached
    implements IGitPathRootOnFilteredFs {

  static GitPathRootShaCachedOnFilteredFs wrap(GitFilteringFs fs, GitPathRootShaCached delegate) {
    return new GitPathRootShaCachedOnFilteredFs(fs, delegate);
  }

  private final GitFilteringFs fs;
  private final GitPathRootShaCached delegate;

  private GitPathRootShaCachedOnFilteredFs(GitFilteringFs fs, GitPathRootShaCached delegate) {
    /*
     * We cannot compute the commit lazily in this class as getCommit may not throw IOE. Thus, we
     * need the fs graph at this stage. However, we cannot check that it is computed right now as we
     * serve as nodes of the graph being possibly built when calling this method.
     */
    // checkState(fs.computedGraph());
    this.fs = checkNotNull(fs);
    this.delegate = checkNotNull(delegate);
  }

  @Override
  public GitFilteringFs getFileSystem() {
    return fs;
  }

  @Deprecated
  @Override
  public GitPathRootShaCached toSha() {
    return this;
  }

  @Deprecated
  @Override
  public GitPathRootShaCached toShaCached() {
    return this;
  }

  @Override
  public GitPathRootShaCached delegate() {
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
  public GitPathRoot toAbsolutePath() {
    verify(delegate.toAbsolutePath().equals(delegate));
    return this;
  }

  @Override
  @Deprecated
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
  public GitPath resolve(String other) {
    return GitPathOnFilteredFs.wrap(fs, delegate.resolve(other));
  }

  @Override
  public GitPath resolve(Path other) {
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
  public Commit getCommit() {
    final Commit underlying = delegate().getCommit();
    verify(fs.computedGraph());
    final Set<GitPathRootShaCached> filteredParents =
        IO_UNCHECKER.getUsing(fs::graph).predecessors(this);
    final ImmutableList<ObjectId> filteredParentIds = filteredParents.stream()
        .map(GitPathRootSha::getStaticCommitId).collect(ImmutableList.toImmutableList());
    return Commit.from(underlying.id(), underlying.author(), underlying.committer(),
        filteredParentIds);
  }

  @Override
  public ImmutableList<GitPathRootSha> getParentCommits() {
    verify(fs.computedGraph());
    return ImmutableList.copyOf(IO_UNCHECKER.getUsing(fs::graph).predecessors(this));
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
