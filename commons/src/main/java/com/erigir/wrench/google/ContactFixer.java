package com.erigir.wrench.google;

import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Takes as input a contacts export from google contacts
 * (use outlook format), and fixes the "everything in the notes field"
 * problem
 * <p>
 * Created by cweiss on 8/8/15.
 */
public class ContactFixer {
    private static final Logger LOG = LoggerFactory.getLogger(ContactFixer.class);
    private File src;
    private File dst;

    //private List<String> headers;
    //private List<Map<String,String>> data;

    public ContactFixer(File src, File dst) {
        super();
        this.src = src;
        this.dst = dst;
        if (src == null || dst == null || !src.exists() || !src.isFile()) {
            throw new IllegalArgumentException("Src and Dst may not be null, and src must exist and be a file");
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: ContactFixer {src} {dst}");
        }
        try {
            ContactFixer inst = new ContactFixer(new File(args[0]), new File(args[1]));
            inst.process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process()
            throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(src));
        FileWriter fw = new FileWriter(dst);

        List<String> headers = Arrays.asList(br.readLine().split(","));

        CSVParser parser = new CSVParser(br, CSVFormat.RFC4180.withHeader(headers.toArray(new String[0])));
        CSVPrinter out = new CSVPrinter(fw, CSVFormat.RFC4180.withQuoteMode(QuoteMode.MINIMAL));
        List<CSVRecord> list = parser.getRecords();

        out.printRecord(headers);

        LOG.info("Header:{}", parser.getHeaderMap());

        LOG.info("Found {} records", list.size());
        for (CSVRecord record : list) {
            Map<String, String> rMap = record.toMap();
            String notes = rMap.get("Notes");
            if (notes != null && notes.length() > 0) {
                List<String> lines = new LinkedList<>(Arrays.asList(notes.split("\n")));
                boolean notesModified = false;
                for (Iterator<String> i = lines.iterator(); i.hasNext(); ) {
                    String val = i.next();
                    int idx = val.indexOf(":");
                    if (idx != -1) {
                        String inHVal = val.substring(0, idx);
                        String nVal = val.substring(idx + 1).trim();
                        if (nVal.length() > 0 && headers.contains(inHVal) && !"Notes".equals(inHVal)) {
                            if (headers.contains(inHVal)) {
                                String curVal = rMap.get(inHVal);
                                if (!curVal.equals(nVal)) {
                                    if (curVal.trim().length() == 0) {
                                        rMap.put(inHVal, nVal);
                                        i.remove();
                                        notesModified = true;
                                    } else {
                                        LOG.info("Didn't replace nonempty val '{}' with '{}' for '{}'", curVal, nVal, inHVal);
                                    }
                                }
                                //LOG.info("Found match field {} in row {} old:{} new: {}", inHVal, record.getRecordNumber(), curVal, nVal);
                                //LOG.info("Old map:{}", record.toMap());
                            } else {
                                LOG.info("Nomatch field found: {}", inHVal);
                            }
                        }
                    }

                }
                if (notesModified) {
                    StringBuffer sb = new StringBuffer();
                    for (String s : lines) {
                        sb.append(s).append("\n");
                    }
                    rMap.put("Notes", sb.toString().trim());

                    //LOG.info("Old notes : {} to new notes {}", notes, sb.toString().trim());

                }


                //LOG.info("Record : {} : Lines:{} Notes: {}", record.getRecordNumber(), lines.length, notes);
            }



            /*


            //LOG.info("{} : Found {} entries : First : {}",record.getRecordNumber(),record.size(), record.get("First Name"));
            //LOG.info("Map: {}",record.toMap().size());
//            String lastName = record.get("Last Name");
            */


            out.printRecord(toOutput(rMap, headers));

        }


        br.close();
        out.close();


    }

    private String[] toOutput(Map<String, String> data, List<String> headers) {
        String[] rval = new String[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            String hName = headers.get(i);
            String dVal = data.get(hName);
            rval[i] = dVal;
        }
        return rval;
    }


}
