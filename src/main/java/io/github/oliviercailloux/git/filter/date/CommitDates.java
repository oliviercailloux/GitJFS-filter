package io.github.oliviercailloux.git.filter.date;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.ZonedDateTime;

public record CommitDates(ZonedDateTime authorDate, ZonedDateTime committerDate) {
  /**
   * Returns a new instance of {@code CommitDates} with no dates.
   *
   * @return a new {@code CommitDates} instance with no author and no committer dates
   */
  public static CommitDates none() {
    return new CommitDates(null, null);
  }

  /**
   * Returns a new instance of {@code CommitDates} with the given date.
   * *
   * @param date the date to be used for both author and committer dates
   * @return a new {@code CommitDates} instance with the same date for both author and committer
   */
  public static CommitDates given(ZonedDateTime date) {
    return new CommitDates(checkNotNull(date), date);
  }
  
  /**
   * Returns a new instance of {@code CommitDates} with the specified author and
   * committer dates.
   *
   * @param authorDate    the date when the commit was authored
   * @param committerDate the date when the commit was committed
   * @return a new {@code CommitDates} instance
   */
  public static CommitDates given(ZonedDateTime authorDate, ZonedDateTime committerDate) {
    return new CommitDates(checkNotNull(authorDate), checkNotNull(committerDate));
  }

  /**
   * Returns a new instance of {@code CommitDates} with the specified author date.
   * *
   * @param authorDate the date when the commit was authored
   * @return a new {@code CommitDates} instance with the specified author date and no committer date
   */
  public static CommitDates givenAuthorDate(ZonedDateTime authorDate) {
    return new CommitDates(checkNotNull(authorDate), null);
  }

  /**
   * Returns a new instance of {@code CommitDates} with the specified committer date.
   * *
   * @param committerDate the date when the commit was committed
   * @return a new {@code CommitDates} instance with the specified committer date and no author date
   */
  public static CommitDates givenCommitterDate(ZonedDateTime committerDate) {
    return new CommitDates(null, checkNotNull(committerDate));
  }
}
