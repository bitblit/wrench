package com.erigir.steelpipe;

import lombok.Data;

import java.util.Objects;

/**
 * Created by cweiss on 6/28/17.
 */
@Data
public class TunneledMariaDatabaseConfig {
  private String sshRemoteHost;
  private int sshRemotePort;
  private String sshUsername;
  private String sshPrivateKeyContents;
  private String sshPublicKeyContents;
  private String sshPrivateKeyPassword; // only thing that is optional for now

  private int sshLocalPort;
  private String sshLocalHost;

  private String dbUser;
  private String dbPassword;
  private String dbDatabaseName;
  private int dbPort;

  public static TunneledMariaDatabaseConfig fromEnvironment() {
    TunneledMariaDatabaseConfig rval = new TunneledMariaDatabaseConfig();
    rval.setSshRemoteHost(envOrSysProperty("STEELPIPE_SSH_REMOTE_HOST", null));
    rval.setSshRemotePort(Integer.parseInt(envOrSysProperty("STEELPIPE_SSH_REMOTE_PORT", "22")));
    rval.setSshLocalHost(envOrSysProperty("STEELPIPE_SSH_LOCAL_HOST", "localhost"));
    rval.setSshLocalPort(Integer.parseInt(envOrSysProperty("STEELPIPE_SSH_LOCAL_PORT", "33006")));
    rval.setSshUsername(envOrSysProperty("STEELPIPE_SSH_USERNAME", null));

    rval.setSshPrivateKeyContents(envOrSysProperty("STEELPIPE_SSH_PRIVATE_KEY_CONTENTS", null));
    rval.setSshPublicKeyContents(envOrSysProperty("STEELPIPE_SSH_PUBLIC_KEY_CONTENTS", null));
    rval.setSshPrivateKeyPassword(envOrSysProperty("STEELPIPE_SSH_PRIVATE_KEY_PASSWORD", null));

    rval.setDbUser(envOrSysProperty("STEELPIPE_DB_USERNAME", null));
    rval.setDbPassword(envOrSysProperty("STEELPIPE_DB_PASSWORD", null));
    rval.setDbDatabaseName(envOrSysProperty("STEELPIPE_DB_DATABASE_NAME", null));
    rval.setDbPort(Integer.parseInt(envOrSysProperty("STEELPIPE_DB_PORT", "3306")));
    return rval;
  }

  private static String envOrSysProperty(String propName, String defaultVal) {
    Objects.requireNonNull(propName);
    String rval = System.getenv(propName);
    rval = (rval == null) ? System.getProperty(propName) : rval;
    rval = (rval == null) ? defaultVal : rval;

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

        dbUser != null &&
        dbPassword != null &&
        dbDatabaseName != null &&
        dbPort>0;
  }

  public byte[] getSshPrivateKeyAsBytes()
  {
    return (sshPrivateKeyContents==null)?null:sshPrivateKeyContents.getBytes();
  }
  public byte[] getSshPublicKeyAsBytes()
  {
    return (sshPublicKeyContents==null)?null:sshPublicKeyContents.getBytes();
  }
  public byte[] getSshPrivateKeyPasswordAsBytes()
  {
    return (sshPrivateKeyPassword==null)?null:sshPrivateKeyPassword.getBytes();
  }



}
