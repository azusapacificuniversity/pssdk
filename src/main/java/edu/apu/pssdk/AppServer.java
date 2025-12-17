package edu.apu.pssdk;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psft.pt8.joa.API;
import psft.pt8.joa.ISession;
import psft.pt8.joa.JOAException;
import psft.pt8.net.AppServerInfo;

/**
 * Main class to be called from client languages (Node.js, Python) to configure and connect to a
 * PeopleSoft Application Server. Uses environment variables or a configuration map to set
 * connection parameters. Provides a factory method to create CI objects.
 */
public class AppServer {

  private String strDomainConnectionPassword;
  private String strUserID;
  private String strPassword;
  private String strAppServerPath;
  private Logger logger = LoggerFactory.getLogger(AppServer.class);

  /**
   * Constructor to initialize AppServer with configuration parameters.
   *
   * @param config Map containing connection parameters:
   *     <ul>
   *       <li>- hostport: App Server host and port
   *       <li>- username: User ID
   *       <li>- password: User password
   *       <li>- domainpw: Domain connection password (optional)
   *     </ul>
   *
   * @throws RuntimeException if required parameters are missing
   */
  public AppServer(Map<String, String> config) throws RuntimeException {
    String version = new AppServerInfo("", "", false, "", false).getToolsRel();
    logger.info("Using PSJOA JAR file version " + version);
    strAppServerPath = config.getOrDefault("hostport", "");
    strDomainConnectionPassword = config.getOrDefault("domainpw", "");
    strUserID = config.getOrDefault("username", "");
    strPassword = config.getOrDefault("password", "");
    if ((strAppServerPath.isBlank()) || strUserID.isBlank() || strPassword.isBlank()) {
      throw new RuntimeException(
          "Connection information incomplete: Please provide all necessary environment variables. "
              + "AppServerPath (host:port): "
              + strAppServerPath
              + ", UserID: "
              + strUserID
              + ", Password: "
              + (strPassword.isBlank() ? "[not provided]" : "*".repeat(strPassword.length())));
    }
  }

  /**
   * Static method to create AppServer instance from environment variables.
   *
   * <p>Required environment variables:
   *
   * <ul>
   *   <li>- PS_APPSERVER_HOSTPORT: host:port of the App Server, separate by comma for multiple
   *       servers, ex: "appserver1:9000,appserver2:9000"
   *   <li>- PS_APPSERVER_USERNAME: user ID
   *   <li>- PS_APPSERVER_PASSWORD: user password
   * </ul>
   *
   * <p>Optional environment variable:
   *
   * <ul>
   *   <li>- PS_APPSERVER_DOMAINPW: domain connection password
   * </ul>
   *
   * @return AppServer instance
   * @throws RuntimeException if instantiation fails
   */
  public static AppServer fromEnv() throws RuntimeException {
    Map<String, String> config = new HashMap<>();
    config.put("hostport", System.getenv("PS_APPSERVER_HOSTPORT"));
    config.put("domainpw", System.getenv("PS_APPSERVER_DOMAINPW"));
    config.put("username", System.getenv("PS_APPSERVER_USERNAME"));
    config.put("password", System.getenv("PS_APPSERVER_PASSWORD"));

    return new AppServer(config);
  }

  /**
   * Factory method to create CI objects.
   *
   * @param ciName Name of the CI to create
   * @return CI object
   * @throws JOAException when JOAEception occurs
   * @throws PssdkException when PssdkException occurs
   */
  public CI ciFactory(String ciName) throws JOAException, PssdkException {
    return ciFactory(ciName, new HashMap<String, Boolean>());
  }

  /**
   * Factory method to create CI objects.
   *
   * @param ciName Name of the CI to create
   * @param options Map of options for CI creation, supported options:
   *     <ul>
   *       <li>- "InteractiveMode": Boolean to enable/disable interactive mode (default: false)
   *       <li>- "GetHistoryItems": Boolean to get history items in GET/SAVE (default: false)
   *       <li>- "EditHistoryItems": Boolean to enable editing of history items (default: false)
   *       <li>- "StopOnFirstError": Boolean to stop processing on first error (default: false)
   *       <li>- "GetDummyRows": Boolean to generate dummy rows in CREATE operations (default: true)
   *     </ul>
   *
   * @return CI object
   * @throws JOAException when JOAEception occurs
   * @throws PssdkException when it can not establish a connection to the App Server
   */
  public CI ciFactory(String ciName, Map<String, Boolean> options)
      throws JOAException, PssdkException {
    // ***** Create PeopleSoft Session Object *****
    ISession iSession = API.createSession(false, 0);

    Boolean establishConnection =
        (strDomainConnectionPassword.isBlank())
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
