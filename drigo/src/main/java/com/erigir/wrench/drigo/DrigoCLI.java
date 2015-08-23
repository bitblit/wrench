package com.erigir.wrench.drigo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;

/**
 * Created by cweiss on 8/5/15.
 */
public class DrigoCLI {
    public static void main(String[] args) {
        try {
            ObjectMapper om = new ObjectMapper();
            om.configure(SerializationFeature.INDENT_OUTPUT, true);

            if (args.length == 2) {
                File config = new File(args[0]);
                File src = new File(args[1]);
                if (!config.exists() || config.isDirectory()) {
                    System.out.println("Config must exist and be a file");

                } else if (!src.exists()) {
                    System.out.println("Source must exist");
                } else {
                    DrigoConfiguration conf = om.readValue(config, DrigoConfiguration.class);
                    conf.setSrc(src);
                    Drigo drigo = new Drigo();
                    DrigoResults res = drigo.execute(conf);
                    String sRes = om.writeValueAsString(res);
                    System.out.println("Results: \n\n" + sRes);

                }

            } else {
                System.out.println("Usage: DrigoCLI {config file} {src}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
