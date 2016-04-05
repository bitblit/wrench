package com.erigir.wrench.aws.ec2;

import com.erigir.wrench.SimpleHttpUtils;

/**
 * Fetches information about the EC2 instance you are running on
 *
 * See : http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html
 * Created by cweiss1271 on 4/4/16.
 */
public class InstanceInfo {
    private static final String METADATA_PREFIX="http://169.254.169.254/latest/meta-data/";

    private static String processMetaDataRequest(String suffix)
    {
        // Short timeout since you are talking to yourself, if at all
        return SimpleHttpUtils.quietFetchUrlAsString(METADATA_PREFIX+suffix,350,3);
    }

    public static String amiId()
    {
        return processMetaDataRequest("ami-id");
    }

    public static String amiLaunchIndex()
    {
        return processMetaDataRequest("ami-launch-index");
    }

    public static String amiManifestPath()
    {
        return processMetaDataRequest("ami-manifest-path");
    }


    public static String hostname()
    {
        return processMetaDataRequest("hostname");
    }

    public static String instanceAction()
    {
        return processMetaDataRequest("instance-action");
    }

    public static String instanceId()
    {
        return processMetaDataRequest("instance-id");
    }

    public static String instanceType()
    {
        return processMetaDataRequest("instance-type");
    }

    public static String kernelId()
    {
        return processMetaDataRequest("kernel-id");
    }

    public static String localHostname()
    {
        return processMetaDataRequest("local-hostname");
    }

    public static String localIpv4()
    {
        return processMetaDataRequest("local-ipv4");
    }

    public static String reservationId()
    {
        return processMetaDataRequest("reservation-id");
    }

    public static String securityGroups()
    {
        return processMetaDataRequest("security-groups");
    }

    public static String mac()
    {
        return processMetaDataRequest("mac");
    }

    public static String publicHostname()
    {
        return processMetaDataRequest("public-hostname");
    }

    public static String publicIpv4()
    {
        return processMetaDataRequest("public-ipv4");
    }


    //block-device-mapping/
    //network/
    //placement/
    //public-keys/
    //services/
}
