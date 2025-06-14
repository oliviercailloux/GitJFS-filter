package io.github.oliviercailloux.git.filter.date;

import io.github.oliviercailloux.git.filter.wrapping.GitPathRootShaCachedOnWrappingFs;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.CommitSignature;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.time.ZonedDateTime;

public class GitPathRootShaCachedOnDaterFs extends GitPathRootShaCachedOnWrappingFs {

  private final CommitDates commitDates;

  public static GitPathRootShaCachedOnDaterFs wrap(GitDaterFs fs, GitPathRootShaCached delegate,
      CommitDates commitDates) {
    return new GitPathRootShaCachedOnDaterFs(fs, delegate, commitDates);
  }

  protected GitPathRootShaCachedOnDaterFs(GitDaterFs fs, GitPathRootShaCached delegate,
      CommitDates commitDates) {
    super(fs, delegate);
    this.commitDates = commitDates;
  }

  @Override
  public Commit getCommit() {
    Commit delegate = super.getCommit();
    return Commit.from(delegate.id(), toSignature(delegate.author(), commitDates.authorDate()),
        toSignature(delegate.committer(), commitDates.committerDate()), delegate.parents());
  }

  private CommitSignature toSignature(CommitSignature original, ZonedDateTime override) {
    if(override == null) {
      return original;
    }
    return CommitSignature.from(original.name(), original.email(), override);
  }
}
