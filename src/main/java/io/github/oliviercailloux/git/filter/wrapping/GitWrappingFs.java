package io.github.oliviercailloux.git.filter.wrapping;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.github.oliviercailloux.jaris.exceptions.Unchecker.IO_UNCHECKER;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRef;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootRef;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import io.github.oliviercailloux.gitjfs.GitPathSha;
import io.github.oliviercailloux.jaris.exceptions.CheckedStream;
import io.github.oliviercailloux.jaris.graphs.GraphUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileStore;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;

/*
 * A GitFs that delegates to another GitFs except that all the paths created by the delegate are
 * wrapped in order to be associated to this GitFs instead of the delegate. <p> To add multiple
 * traits, each trait extends this class, then create trait 1 with base impl as delegate, then trait
 * 2 with trait 1 as delegate, … <p> When the delegate produces a path, we wrap it. When we receive
 * an existing path (thus produced by this fs), we get the delegate path and pass it to the delegate
 * fs.
 */
public class GitWrappingFs extends GitFileSystem {

  private final GitFileSystem delegate;
  private ImmutableGraph<GitPathRootShaCached> graph;
  protected boolean open;

  protected GitWrappingFs(GitFileSystem delegate) {
    this.delegate = checkNotNull(delegate);
    graph = null;
    open = true;
  }

  protected GitFileSystem delegate() {
    return delegate;
  }

  protected GitFileSystem delegateIfOpen() {
    if (open) {
      return delegate;
    }
    throw new ClosedFileSystemException();
  }

  protected GitPathOnWrappingFs wrap(GitPath path) {
    checkArgument(!path.getFileSystem().equals(this));
    return GitPathOnWrappingFs.wrap(this, path);
  }
  protected GitPathShaOnWrappingFs wrap(GitPathSha path) {
    checkArgument(!path.getFileSystem().equals(this));
    return new GitPathShaOnWrappingFs(this, path);
  }
  protected GitPathRefOnWrappingFs wrap(GitPathRef path) {
    checkArgument(!path.getFileSystem().equals(this));
    return new GitPathRefOnWrappingFs(this, path);
  }

  protected GitPathRootOnWrappingFs wrap(GitPathRoot path) {
    checkArgument(!path.getFileSystem().equals(this));
    if (path instanceof GitPathRootRef ref) {
      return wrap(ref);
    }
    if (path instanceof GitPathRootSha sha) {
      return wrap(sha);
    }
    throw new IllegalArgumentException();
  }

  protected GitPathRootRefOnWrappingFs wrap(GitPathRootRef path) {
    checkArgument(!path.getFileSystem().equals(this));
    return GitPathRootRefOnWrappingFs.wrap(this, path);
  }

  protected GitPathRootShaOnWrappingFs wrap(GitPathRootSha path) {
    checkArgument(!path.getFileSystem().equals(this));
    return GitPathRootShaOnWrappingFs.wrap(this, path);
  }

  protected GitPathRootShaCachedOnWrappingFs wrap(GitPathRootShaCached path)
      throws IOException, NoSuchFileException {
    return wrapDoNotThrow(path);
    /*
     * Both versions are needed: one because overrides might require possibility of throwing, one
     * because users of this class might want to call a version that does not throw if it makes
     * sense for them.
     */
  }

  protected GitPathRootShaCachedOnWrappingFs wrapDoNotThrow(GitPathRootShaCached path) {
    checkArgument(!path.getFileSystem().equals(this));
    return GitPathRootShaCachedOnWrappingFs.wrap(this, path);
  }

  @Override
  public GitPath getPath(String first, String... more) throws InvalidPathException {
    final GitFileSystem iDelegate = delegateIfOpen();
    return wrap(iDelegate.getPath(first, more));
  }

  @Override
  public GitPathRoot getPathRoot(String rootStringForm) throws InvalidPathException {
    return wrap(delegateIfOpen().getPathRoot(rootStringForm));
  }

  @Override
  public GitPathRootSha getPathRoot(ObjectId commitId) {
    return wrap(delegateIfOpen().getPathRoot(commitId));
  }

  @Override
  public GitPathRootRef getPathRootRef(String rootStringForm) throws InvalidPathException {
    return wrap(delegateIfOpen().getPathRootRef(rootStringForm));
  }

  @Override
  public GitPathRef getPathRootedRef(String rootStringForm, String... more) throws InvalidPathException {
    return wrap(delegateIfOpen().getPathRootedRef(rootStringForm, more));
  }

  @Override
  public GitPath getAbsolutePath(String first, String... more) throws InvalidPathException {
    return wrap(delegateIfOpen().getAbsolutePath(first, more));
  }

  @Override
  public GitPathSha getAbsolutePath(ObjectId commitId, String... internalPath) {
    return wrap(delegateIfOpen().getAbsolutePath(commitId, internalPath));
  }

  @Override
  public GitPathSha getPath(ObjectId commitId, String... internalPath) {
    return wrap(delegateIfOpen().getPath(commitId, internalPath));
  }

  @Override
  public GitPathRef getRelativePath(String... names) throws InvalidPathException {
    return wrap(delegateIfOpen().getRelativePath(names));
  }

  protected boolean computedGraph() {
    return graph != null;
  }

  @Override
  public ImmutableGraph<GitPathRootShaCached> graph() throws IOException {
    if (graph == null) {
      final MutableGraph<GitPathRootShaCached> wrapped =
          GraphUtils.transform(delegate.graph(), p -> wrap(p));
      graph = ImmutableGraph.copyOf(wrapped);
    }
    return graph;
  }

  @Override
  public ImmutableSet<GitPathRootRef> refs() throws IOException {
    return CheckedStream.<GitPathRootRef, IOException>wrapping(delegateIfOpen().refs().stream())
        .map(p -> wrap(p)).collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public ImmutableSet<DiffEntry> diff(GitPathRoot first, GitPathRoot second)
      throws IOException, NoSuchFileException {
    return delegateIfOpen().diff(GitWrappingFsProvider.asGit(first).delegate(),
        GitWrappingFsProvider.asGit(second).delegate());
  }

  @Override
  public URI toUri() {
    throw new UnsupportedOperationException();
  }

  @Override
  public GitWrappingFsProvider provider() {
    final GitFileSystem iDelegate = delegateIfOpen();
    return new GitWrappingFsProvider(iDelegate.provider());
  }

  @Override
  public ImmutableSet<Path> getRootDirectories() throws UncheckedIOException {
    return ImmutableSet.copyOf(IO_UNCHECKER.getUsing(this::graph).nodes());
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    return delegateIfOpen().getFileStores();
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    return delegateIfOpen().getPathMatcher(syntaxAndPattern);
  }

  @Override
  public String getSeparator() {
    return delegateIfOpen().getSeparator();
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    return delegateIfOpen().getUserPrincipalLookupService();
  }

  @Override
  public boolean isOpen() {
    return delegateIfOpen().isOpen();
  }

  @Override
  public boolean isReadOnly() {
    return delegateIfOpen().isReadOnly();
  }

  @Override
  public WatchService newWatchService() throws IOException {
    return delegateIfOpen().newWatchService();
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return delegateIfOpen().supportedFileAttributeViews();
  }

  @Override
  public void close() throws IOException {
    open = false;
  }
}
