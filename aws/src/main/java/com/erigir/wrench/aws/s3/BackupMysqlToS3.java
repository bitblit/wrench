package com.erigir.wrench.aws.s3;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.erigir.wrench.mysql.DumpDatabase;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * A bean that, when triggered, backs up the referenced mysql database to an s3 bucket
 *
 * Created by chrweiss on 6/24/15.
 */
public class BackupMysqlToS3 {
    private static final Logger LOG = LoggerFactory.getLogger(BackupMysqlToS3.class);
    private String dbUsername;
    private String dbPassword;
    private String dbName;
    private String bucketName;
    private String prefix;
    private AmazonS3 s3;
    private String dateFormat = "yyyy-MM-dd_hh-mm-ss";

    public static void main(String[] args) {
        BackupMysqlToS3 b = new BackupMysqlToS3();
        AmazonS3 s3 = new AmazonS3Client(new DefaultAWSCredentialsProviderChain().getCredentials());
        b.setS3(s3);
        b.setDbUsername(args[0]);
        b.setDbPassword(args[1]);
        b.setDbName(args[2]);
        b.setBucketName(args[3]);
        b.setPrefix(args[4]);

        b.backupDatabase();
    }

    public void backupDatabase()
    {
        Objects.requireNonNull(dbUsername);
        Objects.requireNonNull(dbPassword);
        Objects.requireNonNull(dbName);
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(s3);
        Objects.requireNonNull(dateFormat);

        try {

            StopWatch sw = new StopWatch();
            sw.start();
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            String fullBackupPath = String.format("%s%s.sql.gz", prefix, sdf.format(new Date()));
            LOG.info("Start: backing up database {} to S3: {}/{}", dbName, bucketName, fullBackupPath);

            File tempFile = File.createTempFile("mysqltmp", ".sql.gz");
            FileOutputStream fos = new FileOutputStream(tempFile);
            GZIPOutputStream gos = new GZIPOutputStream(fos);

            LOG.debug("-- Dumping db to intermediate file {}", tempFile);
            DumpDatabase.dumpDatabase(dbUsername, dbPassword, dbName, gos);

            gos.flush();
            gos.close();
            fos.flush();
            fos.close();

            LOG.debug("-- Finished dumping db to intermediate file, size is {}, uploading to s3", tempFile.length());
            ObjectMetadata omd = new ObjectMetadata();
            omd.setContentLength(tempFile.length());
            omd.setContentType("application/x-gzip");

            FileInputStream fis = new FileInputStream(tempFile);
            s3.putObject(bucketName, fullBackupPath, fis, omd);
            fis.close();

            sw.stop();
            LOG.info("Complete: backing up database {} to S3 in {}", dbName, sw);
        } catch (IOException ioe)
        {
            throw new RuntimeException("Error dumping database", ioe);
        }
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setS3(AmazonS3 s3) {
        this.s3 = s3;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
