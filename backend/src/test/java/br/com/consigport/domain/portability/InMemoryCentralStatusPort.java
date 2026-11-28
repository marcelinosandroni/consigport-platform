package br.com.consigport.domain.portability;

import br.com.consigport.domain.shared.DomainError;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of {@link CentralStatusPort}.
 *
 * <p>A hand-written fake: it answers the situation it was given, remembers the
 * protocols it was asked about and can be told to fail, to exercise the path of the
 * integration error.
 */
public final class InMemoryCentralStatusPort implements CentralStatusPort {

  private final List<String> polls = new ArrayList<>();
  private CentralStatus answer;
  private boolean failing;
  private Runnable beforeAnswer = () -> {};

  /**
   * Makes the port answer the given situation.
   *
   * @param answer the situation the Central answers
   */
  public void answerWith(CentralStatus answer) {
    this.answer = answer;
    this.failing = false;
  }

  /**
   * Runs an action before the port answers, to simulate a Central that takes time.
   *
   * @param action the action run before the answer
   */
  public void beforeAnswer(Runnable action) {
    this.beforeAnswer = action;
  }

  /** Makes the port fail, like a Central that does not answer. */
  public void fail() {
    this.failing = true;
  }

  @Override
  public CentralStatus statusOf(String protocol) {
    polls.add(protocol);
    beforeAnswer.run();
    if (failing) {
      throw new DomainError.CentralUnavailable();
    }
    if (answer == null) {
      throw new DomainError.CentralUnavailable();
    }
    return answer;
  }

  /**
   * How many times the status was read.
   *
   * @return the number of reads
   */
  public int polls() {
    return polls.size();
  }

  /**
   * The protocol of the last read.
   *
   * @return the protocol asked about
   */
  public String lastPoll() {
    return polls.get(polls.size() - 1);
  }
}
