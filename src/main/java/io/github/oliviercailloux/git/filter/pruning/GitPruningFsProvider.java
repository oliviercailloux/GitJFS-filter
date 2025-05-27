package io.github.oliviercailloux.git.filter.pruning;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import io.github.oliviercailloux.git.filter.wrapping.GitPathOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaCachedOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitWrappingFsProvider;
import io.github.oliviercailloux.gitjfs.GitFileSystemProvider;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.units.qual.A;

public class GitPruningFsProvider extends GitWrappingFsProvider {

  GitPruningFsProvider(GitFileSystemProvider delegate) {
    super(delegate);
  }
  
    GitPathRootShaCached asVisibleDelegate(Path path)
        throws IOException, NoSuchFileException {
      checkArgument(path.getFileSystem() instanceof GitPruningFs);
      verify(path instanceof GitPathOnWrappingFs);
      GitPathOnWrappingFs gitPath = (GitPathOnWrappingFs) path;
      return gitPath.getRoot().toShaCached().delegate();
    }

  @Override
  public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options,
      FileAttribute<?>... attrs) throws IOException {
    return delegate().newByteChannel(asVisibleDelegate(path), options, attrs);
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter)
      throws IOException {
    // FIXME this should return wrapped paths!
    return delegate().newDirectoryStream(asVisibleDelegate(dir), filter);
  }

  @Override
  public boolean isHidden(Path path) throws IOException {
    return delegate().isHidden(asVisibleDelegate(path));
  }

  @Override
  public FileStore getFileStore(Path path) throws IOException {
    return delegate().getFileStore(asVisibleDelegate(path));
  }

  @Override
  public void checkAccess(Path path, AccessMode... modes) throws IOException {
    delegate().checkAccess(asVisibleDelegate(path), modes);
  }

  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type,
      LinkOption... options) {
    /*
     * Not trivial to implement: we should return a class which itself returns NoSuchStuff if the
     * path does not exist.
     */
    return null;
  }

  @Override
  public <B extends BasicFileAttributes> B readAttributes(Path path, Class<B> type,
      LinkOption... options) throws IOException {
    return delegate().readAttributes(asVisibleDelegate(path), type, options);
  }

  @Override
  public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
      throws IOException {
    return delegate().readAttributes(asVisibleDelegate(path), attributes, options);
  }
}
