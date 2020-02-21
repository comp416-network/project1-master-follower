package master;

public enum Message {
  WANT_GAME(0),
  GAME_START(1),
  PLAY_CARD(2),
  PLAY_RESULT(3),
  GAME_RESULT(4),
  QUIT_GAME(5);

  private final int value;
  Message(final int newValue) {
    value = newValue;
  }

  public int getValue() {
    return value;
  }

}
