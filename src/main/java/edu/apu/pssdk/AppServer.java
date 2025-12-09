package edu.apu.pssdk;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psft.pt8.joa.API;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;
import psft.pt8.net.AppServerInfo;

public class AppServer {

  private String strServerName;
  private String strServerPort;
  private String strDomainConnectionPassword;
  private String strUserID;
  private String strPassword;
  private String strAppServerPath;

  public AppServer(Map<String, String> config) throws JOAException {
    Logger logger = LoggerFactory.getLogger(AppServer.class);
    String version = new AppServerInfo("", "", false, "", false).getToolsRel();
    logger.info("Using PSJOA JAR file version " + version);
    strAppServerPath = config.get("hostport");
    strDomainConnectionPassword = config.get("domainpw");
    strUserID = config.get("username");
    strPassword = config.get("password");
    if (Strings.isNullOrEmpty(strServerName)
        || Strings.isNullOrEmpty(strServerPort)
        || Strings.isNullOrEmpty(strUserID)
        || Strings.isNullOrEmpty(strPassword)) {
      throw new JOAException(
          "Connect information provided is incomplete. Please provide all necessary environment variables. "
              + "AppServerPath (host:port): "
              + Strings.nullToEmpty(strAppServerPath)
              + ", UserID: "
              + Strings.nullToEmpty(strUserID)
              + ", Password: "
              + (Strings.isNullOrEmpty(strPassword)
                  ? "[not provided]"
                  : "*".repeat(strPassword.length())));
    }
  }

  public static AppServer fromEnv() throws JOAException {
    Map<String, String> config = new HashMap<>();
    config.put("hostport", System.getenv("PS_APPSERVER_HOSTPORT"));
    config.put("domainpw", System.getenv("PS_APPSERVER_DOMAINPW"));
    config.put("username", System.getenv("PS_APPSERVER_USERNAME"));
    config.put("password", System.getenv("PS_APPSERVER_PASSWORD"));

    return new AppServer(config);
  }

  public CI ciFactory(String ciName) throws JOAException, PssdkException {
    return ciFactory(ciName, new HashMap<String, Boolean>());
  }

  public CI ciFactory(String ciName, Map<String, Boolean> options)
      throws JOAException, PssdkException {
    // ***** Create PeopleSoft Session Object *****
    ISession iSession = API.createSession(false, 0);

    Boolean establishConnection =
        (Strings.isNullOrEmpty(strDomainConnectionPassword))
            ? iSession.connect(1, strAppServerPath, strUserID, strPassword, null)
            : iSession.connectS(
                1, strAppServerPath, strUserID, strPassword, null, strDomainConnectionPassword);

    if (!establishConnection) {
      throw new PssdkException(
          "Unable to Connect to the App Server. Please verify your credentials and make sure the App Server is running",
          iSession);
    }

    Session session = new Session(iSession);
    return session.ciFactory(ciName, options);
  }
}
