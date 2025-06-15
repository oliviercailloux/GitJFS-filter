package io.github.oliviercailloux.git.filter.wrapping;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.github.oliviercailloux.gitjfs.GitDfsFileSystem;
import io.github.oliviercailloux.gitjfs.GitFileFileSystem;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitFileSystemProvider;
import io.github.oliviercailloux.gitjfs.GitPath;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.IGitDfsFileSystem;
import io.github.oliviercailloux.gitjfs.IGitFileFileSystem;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Map;
import java.util.Set;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

public class GitWrappingFsProvider extends GitFileSystemProvider {
  private final GitFileSystemProvider delegate;

  static GitPathOnWrappingFs asGit(Path path) {
    checkArgument(path instanceof GitPathOnWrappingFs);
    return (GitPathOnWrappingFs) path;
  }

  static GitPathRootOnWrappingFs asGit(GitPathRoot path) {
    checkArgument(path instanceof GitPathRootOnWrappingFs);
    return (GitPathRootOnWrappingFs) path;
  }

  static Path delegateOrOriginal(Path path) {
    if (path instanceof GitPathOnWrappingFs) {
      return ((GitPathOnWrappingFs) path).delegate();
    }
    return path;
  }
  
  protected GitWrappingFsProvider(GitFileSystemProvider delegate) {
    this.delegate = checkNotNull(delegate);
  }

  protected GitFileSystemProvider delegate() {
    return delegate;
  }

  @Override
  public GitFileFileSystem newFileSystem(URI gitFsUri) throws FileSystemAlreadyExistsException,
      UnsupportedOperationException, NoSuchFileException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public GitFileFileSystem newFileSystemFromGitDir(Path gitDir)
      throws FileSystemAlreadyExistsException, UnsupportedOperationException, NoSuchFileException,
      IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public GitFileSystem newFileSystemFromRepository(Repository repository)
      throws FileSystemAlreadyExistsException, UnsupportedOperationException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public GitFileFileSystem newFileSystemFromFileRepository(FileRepository repository)
      throws FileSystemAlreadyExistsException, UnsupportedOperationException, IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public GitDfsFileSystem newFileSystemFromDfsRepository(DfsRepository repository)
      throws FileSystemAlreadyExistsException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public GitFileSystem getFileSystem(URI gitFsUri) throws FileSystemNotFoundException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IGitFileFileSystem getFileSystemFromGitDir(Path gitDir)
      throws FileSystemNotFoundException {
    throw new UnsupportedOperationException();
  }

  @Override
  public IGitDfsFileSystem getFileSystemFromRepositoryName(String name)
      throws FileSystemNotFoundException {
    throw new UnsupportedOperationException();
  }

  @Override
  public GitPath getPath(URI gitFsUri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getScheme() {
    return delegate().getScheme();
  }

  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
      FileAttribute<?>... attrs) throws IOException {
    return delegate().newByteChannel(asGit(path).delegate(), options, attrs);
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter)
      throws IOException {
    checkArgument(dir instanceof GitPathOnWrappingFs);
    // final GitPathOnWrappingFs gitDir = asGit(dir);
    // final DirectoryStream<Path> dirStream =
    //     delegate().newDirectoryStream(gitDir.delegate(), filter);
    // this should return wrapped paths!
    // return dirStream;
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSameFile(Path path, Path path2) throws IOException {
    return delegate().isSameFile(asGit(path).delegate(), asGit(path2).delegate());
  }

  @Override
  public boolean isHidden(Path path) throws IOException {
    return delegate().isHidden(asGit(path).delegate());
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    checkArgument(path instanceof GitPathOnWrappingFs);
    final GitPathOnWrappingFs gitPath = (GitPathOnWrappingFs) path;
    return delegate().getFileStore(gitPath.delegate());
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    final GitPathOnWrappingFs gitPath = asGit(path);
    delegate().checkAccess(gitPath.delegate(), modes);
  }

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type,
      LinkOption... options) {
    return delegate().getFileAttributeView(asGit(path).delegate(), type, options);
  }

  @Override
  public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type,
      LinkOption... options) throws IOException {
    return delegate().readAttributes(asGit(path).delegate(), type, options);
  }

  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
      throws IOException {
    return delegate().readAttributes(asGit(path).delegate(), attributes, options);
  }
}

