package com.erigir.steelpipe;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Builder;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by cweiss on 6/28/17.
 */
public class TunneledMariaDatabaseDataSource implements DataSource {
  public static final Logger LOG = LoggerFactory.getLogger(TunneledMariaDatabaseDataSource.class);
  private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";

  private final TunneledMariaDatabaseConfig cfg;

  private final Object lock = new Object();
  private final List<Connection> allConnections = new LinkedList<>();
  private Session cacheTunnel = null;
  private Integer localPort = null;

  public TunneledMariaDatabaseDataSource(TunneledMariaDatabaseConfig cfg, boolean connectTunnelImmediately) {
    super();
    this.cfg = Objects.requireNonNull(cfg);
    if (!this.cfg.isFullyConfigured())
    {
      throw new IllegalStateException("Cannot create this data source - the configuration is not complete");
    }
    if (connectTunnelImmediately)
    {
      this.verifyTunnel();
    }
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    Connection rval = null;
    LOG.debug("Fetching connection");

    // Make sure we've opened the tunnel
    verifyTunnel();
    try {
      String dbUrl = "jdbc:mysql://localhost:" + localPort + "/" + cfg.getDbDatabaseName();
      rval = DriverManager.getConnection(dbUrl, username, password);
      allConnections.add(rval);
    } catch (SQLException sqe) {
      LOG.warn("Could not open connection", sqe);
      rval = null;
    }

    return rval;
  }

  /**
   * Ripped straight from MariaDB Datasource
   *
   * Returns an object that implements the given interface to allow access to non-standard methods, or standard
   * methods not exposed by the proxy.
   * <p>
   * If the receiver implements the interface then the result is the receiver or a proxy for the receiver. If the
   * receiver is a wrapper and the wrapped object implements the interface then the result is the wrapped object or a
   * proxy for the wrapped object. Otherwise return the the result of calling <code>unwrap</code> recursively on the
   * wrapped object or a proxy for that result. If the receiver is not a wrapper and does not implement the interface,
   * then an <code>SQLException</code> is thrown.
   *
   * @param iface A Class defining an interface that the result must implement.
   * @return an object that implements the interface. May be a proxy for the actual implementing object.
   * @throws SQLException If no object found that implements the interface
   * @since 1.6
   */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    try {
      if (isWrapperFor(iface)) {
        return iface.cast(this);
      } else {
        throw new SQLException("The receiver is not a wrapper and does not implement the interface");
      }
    } catch (Exception e) {
      throw new SQLException("The receiver is not a wrapper and does not implement the interface");
    }
  }

  /**
   * Ripped straight from MariaDB Datasource
   *
   * Returns true if this either implements the interface argument or is directly or indirectly a wrapper for an
   * object that does. Returns false otherwise. If this implements the interface then return true, else if this is a
   * wrapper then return the result of recursively calling <code>isWrapperFor</code> on the wrapped object. If this
   * does not implement the interface and is not a wrapper, return false. This method should be implemented as a
   * low-cost operation compared to <code>unwrap</code> so that callers can use this method to avoid expensive
   * <code>unwrap</code> calls that may fail. If this method returns true then calling <code>unwrap</code> with the
   * same argument should succeed.
   *
   * @param interfaceOrWrapper a Class defining an interface.
   * @return true if this implements the interface or directly or indirectly wraps an object that does.
   * @throws SQLException if an error occurs while determining whether this is a wrapper for an object with
   *                      the given interface.
   * @since 1.6
   */
  @Override
  public boolean isWrapperFor(Class<?> interfaceOrWrapper) throws SQLException {
    return interfaceOrWrapper.isInstance(this);
  }

  /**
   * Ripped straight from MariaDB Datasource
   */
  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return null;
  }

  /**
   * Ripped straight from MariaDB Datasource
   */
  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return cfg.getDbLoginTimeoutInSeconds();
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    cfg.setDbLoginTimeoutInSeconds(seconds);
  }

  /**
   * Ripped straight from MariaDB Datasource
   */
  @Override
  public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }

  public void shutdown() {
    LOG.info("Shutting down");

    LOG.info("Closing all {} connections", allConnections.size());
    for (Connection c : allConnections) {
      try {
        c.close();
      } catch (Exception e) {
        LOG.warn("Error shutting down connection", e);
      }
    }

    if (cacheTunnel != null) {
      LOG.info("Stopping ssh tunnel");
      try {
        cacheTunnel.disconnect();
        cacheTunnel = null;
      } catch (Exception e) {
        LOG.warn("Error closing ssh tunnel", e);
      }
    }

  }

  public Connection getConnection()
    throws SQLException
  {
    return getConnection( cfg.getDbUsername(), cfg.getDbPassword());
  }

  private boolean verifyTunnel() {
    synchronized (lock) {
      LOG.info("Initializing");

      boolean rval = true;
      if (cacheTunnel == null) {
        if (cfg != null && cfg.isFullyConfigured()) {
          try {
            LOG.info("Loading JDBC driver");
            Class.forName(JDBC_DRIVER);

            LOG.info("Opening SSH tunnel");
            // Open up the ssh tunnel
            JSch jsch = new JSch();
            cacheTunnel = jsch.getSession(cfg.getSshUsername(), cfg.getSshRemoteHost(), cfg.getSshRemotePort());
            jsch.addIdentity("steelpipe", cfg.getSshPrivateKeyAsBytes(), cfg.getSshPublicKeyAsBytes(), cfg.getSshPrivateKeyPasswordAsBytes());
            Properties p = new Properties();
            p.setProperty("StrictHostKeyChecking", "no");
            p.setProperty("ConnectionAttempts", "3");
            cacheTunnel.setConfig(p);
            cacheTunnel.connect();

            localPort = cacheTunnel.setPortForwardingL(cfg.getSshLocalPort(), cfg.getSshLocalHost(), cfg.getDbDatabasePort());

            if (localPort != cfg.getSshLocalPort()) {
              LOG.warn("Assigned port was {} not matching requested {}", localPort, cfg.getSshLocalPort());
            }

            LOG.info("Session connected, adding shutdown hook");
            Runtime.getRuntime().addShutdownHook(new Thread() {
              @Override
              public void run() {
                shutdown();
              }
            });

          } catch (JSchException e) {
            LOG.warn("Failed to open connection", e);
            rval = false;
          } catch (ClassNotFoundException cnf) {
            LOG.error("Should not happen - no jdbc driver found for {}", JDBC_DRIVER, cnf);
            rval = false;
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
}
