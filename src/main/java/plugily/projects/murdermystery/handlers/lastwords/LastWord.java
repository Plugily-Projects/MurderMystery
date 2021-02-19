package plugily.projects.murdermystery.handlers.lastwords;

public class LastWord {

  private final String message;
  private final String permission;

  public LastWord(String message, String permission) {
    this.message = message;
    this.permission = permission;
  }

  public String getMessage() {
    return message;
  }

  public String getPermission() {
    return permission;
  }

  public boolean hasPermission() {
    return !permission.isEmpty();
  }

}
