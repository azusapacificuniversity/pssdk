package edu.apu.pssdk;

import com.google.common.base.Strings;
import java.util.Map;
import psft.pt8.joa.API;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;

public class Env {
  private static String strServerName, strServerPort, strAppServerPath, strDomainConnPswd;
  private static String strUserID, strPassword;

  static {
    strServerName = System.getenv("APP_SERVER_HOSTNAME");
    strServerPort = System.getenv("APP_SERVER_PORT");
    strDomainConnPswd = System.getenv("APP_SERVER_DOMAIN_CONNECTION_PASSWORD");
    strUserID = System.getenv("APP_SERVER_USERNAME");
    strPassword = System.getenv("APP_SERVER_PASSWORD");
  }

  public static Ci ciFactory(String ciName, Map<String, Boolean> options) throws JOAException {
    Session session;
    ISession iSession;
    if (Strings.isNullOrEmpty(strServerName)
        || Strings.isNullOrEmpty(strServerPort)
        || Strings.isNullOrEmpty(strUserID)
        || Strings.isNullOrEmpty(strPassword)) {
      throw new JOAException(
          "Connect information provided is incomplete. Please provide all necessary environment variables.");
    }

    // Build Application Server Path
    strAppServerPath = strServerName + ":" + strServerPort;

    // ***** Create PeopleSoft Session Object *****
    iSession = API.createSession(false, 0);

    Boolean isConnected;
    if (Strings.isNullOrEmpty(strDomainConnPswd)) {
      isConnected = iSession.connect(1, strAppServerPath, strUserID, strPassword, null);
    } else {
      isConnected =
          iSession.connectS(1, strAppServerPath, strUserID, strPassword, null, strDomainConnPswd);
    }
    if (!isConnected) {
      throw new JOAException(
          "Unable to Connect to the App Server. Please verify your credentials and make sure the App Server is running");
    }

    session = new Session(iSession);
    return session.ciFactory(ciName, options);
  }
}
