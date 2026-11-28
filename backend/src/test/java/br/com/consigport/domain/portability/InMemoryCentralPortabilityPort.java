package br.com.consigport.domain.portability;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of {@link CentralPortabilityPort}.
 *
 * <p>A hand-written fake: it answers the same protocol for every submission, like the
 * Central does for the same idempotency key, and it can be told to fail to exercise the
 * path of the integration error.
 */
public final class InMemoryCentralPortabilityPort implements CentralPortabilityPort {

  /** The protocol answered by the fake. */
  public static final String PROTOCOL = "PRT-20261123-0001";

  private final List<PortabilityOperation> submissions = new ArrayList<>();
  private CentralSubmissionException failure;

  @Override
  public String submit(PortabilityOperation operation) {
    submissions.add(operation);
    if (failure != null) {
      throw failure;
    }
    return PROTOCOL;
  }

  /**
   * Makes the next submissions fail.
   *
   * @param failure the failure answered by the fake
   */
  public void failWith(CentralSubmissionException failure) {
    this.failure = failure;
  }

  /**
   * How many times the operation was submitted.
   *
   * @return the number of submissions
   */
  public int submissions() {
    return submissions.size();
  }

  /**
   * The operation of the last submission.
   *
   * @return the submitted operation
   */
  public PortabilityOperation lastSubmission() {
    return submissions.get(submissions.size() - 1);
  }
}
