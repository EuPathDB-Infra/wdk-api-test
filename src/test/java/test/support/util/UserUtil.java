package test.support.util;

import test.support.Conf;

public class UserUtil {
  private static UserUtil instance;
  private UserUtil() {}

  public static final String BASE_PATH = Conf.SERVICE_PATH + "/users";
  public static final String BY_ID_PATH = BASE_PATH + "/{userId}";

  public static final UserUtil INSTANCE = new UserUtil();

  public static UserUtil getInstance() {
    if (instance == null) {
      instance = new UserUtil();
    }
    return instance;
  }
}
