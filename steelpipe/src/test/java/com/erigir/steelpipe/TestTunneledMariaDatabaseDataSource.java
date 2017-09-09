package com.erigir.steelpipe;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestTunneledMariaDatabaseDataSource {
  private static final Logger LOG = LoggerFactory.getLogger(TestTunneledMariaDatabaseDataSource.class);

  /**
   * Note - this test is ignored since it relies on a local config being setup to hit an actual
   * mysql instance.
   *
   * @throws Exception
   */
  @Test
  @Ignore
  public void testConnection()
      throws Exception {
    TunneledMariaDatabaseConfig cfg = TunneledMariaDatabaseConfig.fromEnvironment();
    TunneledMariaDatabaseDataSource inst = new TunneledMariaDatabaseDataSource(cfg, true);

    Connection connection = inst.getConnection();
    assertNotNull(connection);
    Statement stmt = connection.createStatement();
    assertNotNull(stmt);
    ResultSet rs = stmt.executeQuery("SELECT * from boards");
    assertNotNull(rs);

    int count = 0;
    while (rs.next()) {
      count++;
      LOG.info("Got : {} = {}", rs.getInt("id"), rs.getString("k"));
    }

    assertTrue(count > 0);

    rs.close();
    stmt.close();
  }


}
