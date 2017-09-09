package com.erigir.steelpipe;

import com.erigir.wrench.EnvironmentUtils;
import lombok.Builder;
import lombok.Data;

/**
 * Created by cweiss on 6/28/17.
 */
@Data
@Builder
public class TunneledMariaDatabaseConfig {
  /**
   * The SSH port we are connecting to on the host machine (defaults to 22)
   */
  @Builder.Default
  private int sshRemotePort = 22;
  /**
   * The port we are opening on the local machine to tunnel through - should be over 1024 (defaults to 33006)
   */
  @Builder.Default
  private int sshLocalPort = 33006;
  /**
   * The local host name (defaults to localhost)
   */
  @Builder.Default
  private String sshLocalHost = "localhost";
  /**
   * The port on the remote machine the database listens to (defaults to 3306)
   */
  @Builder.Default
  private int dbDatabasePort = 3306;
  /**
   * Max time to wait on the database when logging in (defaults to 30)
   */
  @Builder.Default
  private int dbLoginTimeoutInSeconds = 30;

  /**
   * The hostname of the machine running ssh remotely
   */
  private String sshRemoteHost;
  /**
   * The SSH username to use for connection (NOT the DB user name)
   */
  private String sshUsername;
  /**
   * A string containing the private key in the case of ssh keypair authentication
   */
  private String sshPrivateKeyContents;
  /**
   * A string containing the public key in the case of ssh keypair authentication
   */
  private String sshPublicKeyContents;
  /**
   * A string containing the password guarding the private key in the case of ssh keypair authentication (if not used, leave null)
   */
  private String sshPrivateKeyPassword;
  /**
   * A string containing the DATABASE username (NOT the SSH username)
   */
  private String dbUsername;
  /**
   * A string containing the DATABASE password (NOT the SSH password)
   */
  private String dbPassword;
  /**
   * A string containing the name of the database to connect to on the remote host
   */
  private String dbDatabaseName;

  public static TunneledMariaDatabaseConfig fromEnvironment() {
    TunneledMariaDatabaseConfig defaults = TunneledMariaDatabaseConfig.builder().build();

    TunneledMariaDatabaseConfig rval = TunneledMariaDatabaseConfig.builder()
        .sshLocalHost(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_LOCAL_HOST", defaults.sshLocalHost))
        .sshRemoteHost(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_REMOTE_HOST", defaults.sshRemoteHost))
        .sshRemotePort(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_REMOTE_PORT", defaults.sshRemotePort, Integer.class))
        .sshLocalHost(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_LOCAL_HOST", defaults.sshLocalHost))
        .sshLocalPort(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_LOCAL_PORT", defaults.sshLocalPort, Integer.class))
        .sshUsername(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_USERNAME", defaults.sshUsername))

        .sshPrivateKeyContents(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_PRIVATE_KEY_CONTENTS", defaults.sshPrivateKeyContents))
        .sshPublicKeyContents(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_PUBLIC_KEY_CONTENTS", defaults.sshPublicKeyContents))
        .sshPrivateKeyPassword(EnvironmentUtils.envOrSysProperty("STEELPIPE_SSH_PRIVATE_KEY_PASSWORD", defaults.sshPrivateKeyPassword))

        .dbUsername(EnvironmentUtils.envOrSysProperty("STEELPIPE_DB_USERNAME", defaults.dbUsername))
        .dbPassword(EnvironmentUtils.envOrSysProperty("STEELPIPE_DB_PASSWORD", defaults.dbPassword))
        .dbDatabaseName(EnvironmentUtils.envOrSysProperty("STEELPIPE_DB_DATABASE_NAME", defaults.dbDatabaseName))
        .dbDatabasePort(EnvironmentUtils.envOrSysProperty("STEELPIPE_DB_DATABASE_PORT", defaults.dbDatabasePort, Integer.class))
        .dbLoginTimeoutInSeconds(EnvironmentUtils.envOrSysProperty("STEELPIPE_DB_LOGIN_TIMEOUT", defaults.dbLoginTimeoutInSeconds, Integer.class))
        .build();
    return rval;
  }

  public boolean isFullyConfigured() {
    return sshRemoteHost != null &&
        sshRemotePort > 0 &&
        sshUsername != null &&
        sshPrivateKeyContents != null &&
        sshPublicKeyContents != null &&

        sshLocalPort > 0 &&
        sshLocalHost != null &&

        dbUsername != null &&
        dbPassword != null &&
        dbDatabaseName != null &&
        dbDatabasePort > 0 &&
        dbLoginTimeoutInSeconds > 0;
  }

  public byte[] getSshPrivateKeyAsBytes() {
    return (sshPrivateKeyContents == null) ? null : sshPrivateKeyContents.getBytes();
  }

  public byte[] getSshPublicKeyAsBytes() {
    return (sshPublicKeyContents == null) ? null : sshPublicKeyContents.getBytes();
  }

  public byte[] getSshPrivateKeyPasswordAsBytes() {
    return (sshPrivateKeyPassword == null) ? null : sshPrivateKeyPassword.getBytes();
  }


}
