package io.github.oliviercailloux.git.filter.date;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.graph.ImmutableGraph;
import io.github.oliviercailloux.git.filter.pruning.GitPruningFs;
import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaCachedOnWrappingFs;
import io.github.oliviercailloux.git.filter.wrapping.GitWrappingFs;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Predicate;

public class GitDaterFs extends GitWrappingFs {

  public static GitDaterFs date(GitFileSystem delegate,
  Function<GitPathRootShaCached, CommitDates> dateFunction)  {
    return new GitDaterFs(delegate, dateFunction);
  }

  private final Function<GitPathRootShaCached, CommitDates> dateFunction;

  private GitDaterFs(GitFileSystem delegate, Function<GitPathRootShaCached, CommitDates> dateFunction) {
    super(delegate);
    this.dateFunction = checkNotNull(dateFunction);
  }

  @Override
  protected GitPathRootShaCachedOnWrappingFs wrap(GitPathRootShaCached path)
      throws IOException, NoSuchFileException {
    return GitPathRootShaCachedOnDaterFs.wrap(this, path, dateFunction.apply(checkNotNull(path)));
  }
}
