package io.github.oliviercailloux.git.filter.date;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableGraph;
import io.github.oliviercailloux.gitjfs.ForwardingGitFileSystem;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitFileSystemProvider;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootRef;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
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

public class GitDaterFs extends GitFileSystem{

  @Override
  public GitPath getPath(String first, String... more) throws InvalidPathException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPath'");
  }

  @Override
  public GitPathRoot getPathRoot(String rootStringForm) throws InvalidPathException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPathRoot'");
  }

  @Override
  public GitPathRootSha getPathRoot(ObjectId commitId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPathRoot'");
  }

  @Override
  public GitPathRootRef getPathRootRef(String rootStringForm) throws InvalidPathException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPathRootRef'");
  }

  @Override
  public GitPath getAbsolutePath(String first, String... more) throws InvalidPathException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAbsolutePath'");
  }

  @Override
  public GitPath getAbsolutePath(ObjectId commitId, String internalPath1, String... internalPath) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAbsolutePath'");
  }

  @Override
  public GitPath getRelativePath(String... names) throws InvalidPathException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getRelativePath'");
  }

  @Override
  public ImmutableGraph<GitPathRootShaCached> graph() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'graph'");
  }

  @Override
  public ImmutableSet<GitPathRootRef> refs() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'refs'");
  }

  @Override
  public ImmutableSet<DiffEntry> diff(GitPathRoot first, GitPathRoot second)
      throws IOException, NoSuchFileException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'diff'");
  }

  @Override
  public URI toUri() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'toUri'");
  }

  @Override
  public GitFileSystemProvider provider() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'provider'");
  }

  @Override
  public ImmutableSet<Path> getRootDirectories() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getRootDirectories'");
  }

  @Override
  public Iterable<FileStore> getFileStores() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getFileStores'");
  }

  @Override
  public PathMatcher getPathMatcher(String syntaxAndPattern) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPathMatcher'");
  }

  @Override
  public String getSeparator() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getSeparator'");
  }

  @Override
  public UserPrincipalLookupService getUserPrincipalLookupService() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getUserPrincipalLookupService'");
  }

  @Override
  public boolean isOpen() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isOpen'");
  }

  @Override
  public boolean isReadOnly() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'isReadOnly'");
  }

  @Override
  public WatchService newWatchService() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'newWatchService'");
  }

  @Override
  public Set<String> supportedFileAttributeViews() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'supportedFileAttributeViews'");
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'close'");
  }
  
}
