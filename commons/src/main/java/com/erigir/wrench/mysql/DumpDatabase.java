package com.erigir.wrench.mysql;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Thin wrapper around MysqlDump Command
 *
 * Created by chrweiss on 6/24/15.
 */
public class DumpDatabase {

    public static void main(String[] args) {
        dumpDatabase(args[0],args[1],args[2],System.out);
    }

    public static void dumpDatabase(String username, String password, String database, OutputStream output)
    {
        Runtime runtime = Runtime.getRuntime();
        String[] cmd = new String[]{
                "mysqldump",
                "-u",
                username,
                "-p"+password,
                database
        };

        try {
            Process process = runtime.exec(cmd);

            InputStream is = process.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            int read = bis.read();
            while (read!=-1)
            {
                output.write(read);
                read = bis.read();
            }
            bis.close();

        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Error processing mysqldump",ioe);
        }
    }


}
