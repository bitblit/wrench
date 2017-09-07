package com.erigir.steelpipe;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by cweiss on 6/28/17.
 */
public class TunneledMariaDatabaseDataSource implements DataSource{
  public static final Logger LOG = LoggerFactory.getLogger(TunneledMariaDatabaseDataSource.class);
  private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";

  private final Object lock = new Object();
  private final TunneledMariaDatabaseConfig cfg;
  private final List<Connection> allConnections = new LinkedList<>();

  private Session tunnel;
  private Integer localPort;

  public TunneledMariaDatabaseDataSource()
  {
    super();
    LOG.info("Creating tunnel config from environment");
    cfg = TunneledMariaDatabaseConfig.fromEnvironment();
  }

  public TunneledMariaDatabaseDataSource(TunneledMariaDatabaseConfig config)
  {
    super();
    Objects.requireNonNull(config);
    this.cfg = config;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return null;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return null;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return null;
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {

  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {

  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  @Override
  public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }

  public void shutdown()
  {
    LOG.info("Shutting down");

    LOG.info("Closing all {} connections", allConnections.size());
    for (Connection c:allConnections)
    {
      try
      {
        c.close();
      }
      catch (Exception e)
      {
        LOG.warn("Error shutting down connection",e);
      }
    }

    if (tunnel!=null)
    {
      LOG.info("Stopping ssh tunnel");
      try
      {
        tunnel.disconnect();
        tunnel = null;
      }
      catch (Exception e)
      {
        LOG.warn("Error closing ssh tunnel",e);
      }
    }

  }

  public Connection getConnection()
  {
    Connection rval = null;
    LOG.debug("Fetching connection");

    initialize();
    try {
      String dbUrl = "jdbc:mysql://localhost:" + localPort + "/" + cfg.getDbDatabaseName();
      rval = DriverManager.getConnection(dbUrl, cfg.getDbUser(), cfg.getDbPassword());
      allConnections.add(rval);
    }
    catch (SQLException sqe)
    {
      LOG.warn("Could not open connection",sqe);
      rval = null;
    }

    return rval;
  }

  private boolean initialize()
  {
    synchronized (lock) {
      LOG.info("Initializing");

      boolean rval = true;
      if (tunnel == null) {
        if (cfg!=null && cfg.isFullyConfigured()) {
          try {
            LOG.info("Loading JDBC driver");
            Class.forName(JDBC_DRIVER);

            LOG.info("Opening SSH tunnel");
            // Open up the ssh tunnel
            JSch jsch = new JSch();
            Session session = jsch.getSession(cfg.getSshUsername(), cfg.getSshRemoteHost(), cfg.getSshRemotePort());
            jsch.addIdentity("xenon", cfg.getSshPrivateKeyAsBytes(), cfg.getSshPublicKeyAsBytes(), cfg.getSshPrivateKeyPasswordAsBytes());
            Properties p = new Properties();
            p.setProperty("StrictHostKeyChecking", "no");
            p.setProperty("ConnectionAttempts", "3");
            session.setConfig(p);
            session.connect();

            localPort = session.setPortForwardingL(cfg.getSshLocalPort(), cfg.getSshLocalHost(), cfg.getDbPort());

            if (localPort != cfg.getSshLocalPort()) {
              LOG.warn("Assigned port was {} not matching requested {}", localPort, cfg.getSshLocalPort());
            }

            LOG.info("Session connected");
          } catch (JSchException e) {
            LOG.warn("Failed to open connection", e);
            rval=false;
          }
          catch (ClassNotFoundException cnf)
          {
            LOG.error("Should not happen - no jdbc driver found for {}",JDBC_DRIVER, cnf);
            rval=false;
          }
        } else {
          LOG.warn("Cannot create connection - incomplete configuration");
          rval = false;
        }
      }
      return rval;
    }
  }

  public TunneledMariaDatabaseConfig getCfg() {
    return cfg;
  }

  public static void main(String[] args) {
    try
    {
      //adomni_dev
      TunneledMariaDatabaseDataSource inst = new TunneledMariaDatabaseDataSource();

      Connection connection = inst.getConnection();
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * from boards");

      while (rs.next())
      {
        LOG.info("Got : {} = {}",rs.getInt("id"), rs.getString("k"));
      }

      rs.close();
      stmt.close();

      inst.shutdown();

      System.exit(0);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
