package edu.apu.pssdk;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import psft.pt8.joa.API;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;

public class AppServer {

  private String strServerName;
  private String strServerPort;
  private String strDomainConnectionPassword;
  private String strUserID;
  private String strPassword;
  private String strAppServerPath;

  public AppServer(Map<String, String> config) throws JOAException {
    strServerName = config.get("hostname");
    strServerPort = config.get("joltport");
    strDomainConnectionPassword = config.get("dmncnpwd");
    strUserID = config.get("username");
    strPassword = config.get("password");
    if (Strings.isNullOrEmpty(strServerName)
        || Strings.isNullOrEmpty(strServerPort)
        || Strings.isNullOrEmpty(strUserID)
        || Strings.isNullOrEmpty(strPassword)) {
      throw new JOAException(
          "Connect information provided is incomplete. Please provide all necessary environment variables.");
    }
    // Build Application Server Path
    strAppServerPath = strServerName + ":" + strServerPort;
  }

  public CI ciFactory(String ciName) throws JOAException {
    return ciFactory(ciName, new HashMap<String, Boolean>());
  }

  public CI ciFactory(String ciName, Map<String, Boolean> options) throws JOAException {
    // ***** Create PeopleSoft Session Object *****
    ISession iSession = API.createSession(false, 0);

    Boolean establishConnection =
        (Strings.isNullOrEmpty(strDomainConnectionPassword))
            ? iSession.connect(1, strAppServerPath, strUserID, strPassword, null)
            : iSession.connectS(
                1, strAppServerPath, strUserID, strPassword, null, strDomainConnectionPassword);

    if (!establishConnection) {
      throw new JOAException(
          "Unable to Connect to the App Server. Please verify your credentials and make sure the App Server is running");
    }

    Session session = new Session(iSession);
    return session.ciFactory(ciName, options);
  }
}
