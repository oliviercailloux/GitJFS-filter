package io.github.oliviercailloux.git.filter.wrapping;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;
import static io.github.oliviercailloux.jaris.exceptions.Unchecker.IO_UNCHECKER;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootRef;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import io.github.oliviercailloux.gitjfs.IGitFileSystem;
import io.github.oliviercailloux.jaris.exceptions.CheckedStream;
import io.github.oliviercailloux.jaris.graphs.GraphUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
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
 * traits, each trait extends this class, then create trait 1 with base impl as delegate, then trait 2 with
 * trait 1 as delegate, …
 * <p>
 * When the delegate produces a path, we wrap it. When we receive an existing path (thus produced by this fs), we get the delegate path and pass it to the delegate fs.
 */
public class GitWrappingFs extends GitFileSystem {

  private final GitFileSystem delegate;
  private ImmutableGraph<GitPathRootShaCached> graph;

  protected GitWrappingFs(GitFileSystem delegate) {
    this.delegate = checkNotNull(delegate);
    graph = null;
  }

  protected GitFileSystem delegate() {
    return delegate;
  }

  protected GitPathOnWrappingFs wrap(GitPath path) {
    checkArgument(!path.getFileSystem().equals(this));
    return GitPathOnWrappingFs.wrap(this, path);
  }

  protected GitPathRootOnWrappingFs wrap(GitPathRoot path) {
    checkArgument(!path.getFileSystem().equals(this));
    if(path instanceof GitPathRootRef ref) {
      return wrap(ref);
    }
    if(path instanceof GitPathRootSha sha) {
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

  protected GitPathRootShaCachedOnWrappingFs wrap(GitPathRootShaCached path) throws IOException,
      NoSuchFileException {
    return wrapDoNotThrow(path);
  }

  protected GitPathRootShaCachedOnWrappingFs wrapDoNotThrow(GitPathRootShaCached path) {
    checkArgument(!path.getFileSystem().equals(this));
    return GitPathRootShaCachedOnWrappingFs.wrap(this, path);
  }

  @Override
  public GitPath getPath(String first, String... more) throws InvalidPathException {
    final IGitFileSystem iDelegate = delegate();
    return wrap(iDelegate.getPath(first, more));
  }

  @Override
  public GitPathRoot getPathRoot(String rootStringForm) throws InvalidPathException {
    return wrap(delegate().getPathRoot(rootStringForm));
  }

  @Override
  public GitPathRootSha getPathRoot(ObjectId commitId) {
    return wrap(delegate().getPathRoot(commitId));
  }

  @Override
  public GitPathRootRef getPathRootRef(String rootStringForm) throws InvalidPathException {
    return wrap(delegate().getPathRootRef(rootStringForm));
  }

  @Override
  public GitPath getAbsolutePath(String first, String... more) throws InvalidPathException {
    return wrap(delegate().getAbsolutePath(first, more));
  }

  @Override
  public GitPath getAbsolutePath(ObjectId commitId, String internalPath1, String... internalPath) {
    return wrap(delegate().getAbsolutePath(commitId, internalPath1, internalPath));
  }

  @Override
  public GitPath getRelativePath(String... names) throws InvalidPathException {
    return wrap(delegate().getRelativePath(names));
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
    return CheckedStream.<GitPathRootRef, IOException>wrapping(delegate().refs().stream())
        .map(p -> wrap(p)).collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public ImmutableSet<DiffEntry> diff(GitPathRoot first, GitPathRoot second)
      throws IOException, NoSuchFileException {
    return delegate().diff(GitWrappingFsProvider.asGit(first).delegate(), GitWrappingFsProvider.asGit(second).delegate());
  }

  @Override
  public URI toUri() {
    throw new UnsupportedOperationException();
  }

  @Override
  public GitWrappingFsProvider provider() {
    final IGitFileSystem iDelegate = delegate();
    return new GitWrappingFsProvider(iDelegate.provider());
  }

  @Override
  public ImmutableSet<Path> getRootDirectories() throws UncheckedIOException {
    return ImmutableSet.copyOf(IO_UNCHECKER.getUsing(this::graph).nodes());
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    return delegate().getFileStores();
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    return delegate().getPathMatcher(syntaxAndPattern);
  }

  @Override
  public String getSeparator() {
    return delegate().getSeparator();
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    return delegate().getUserPrincipalLookupService();
  }

  @Override
  public boolean isOpen() {
    return delegate().isOpen();
  }

  @Override
  public boolean isReadOnly() {
    return delegate().isReadOnly();
  }

  @Override
  public WatchService newWatchService() throws IOException {
    return delegate().newWatchService();
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    return delegate().supportedFileAttributeViews();
  }

  @Override
  public void close() throws IOException {
    delegate().close();
  }
}
