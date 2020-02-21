package master;

public enum ResultMessage {
  WIN(0),
  DRAW(1),
  LOSE(2);

  private final int value;
  ResultMessage(final int newValue) {
    value = newValue;
  }

  public int getValue() {
    return value;
  }
}
