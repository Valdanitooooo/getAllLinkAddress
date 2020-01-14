package org.test;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        try {
            FileInputStream fis = new FileInputStream("your har json file path ");
            final String str = IOUtils.toString(fis, "UTF-8");
            List<String> flows = JsonPath.parse(str).read("$.log.entries[*].request.url");
            String urls = "";
            for (String flow : flows) {
                flow = flow.replaceAll("&", "&amp;");
                urls += "<collectionProp name=\"1357392467\">\n   <stringProp name=\"726316312\">"
                        + flow + "</stringProp>\n</collectionProp>\n";
            }
            File file = new File("your output path/urls.xml");
            FileOutputStream outputStream = new FileOutputStream(file);
            InputStream is = new ByteArrayInputStream(
                    urls.getBytes("utf8"));
            IOUtils.copy(is, outputStream);
            System.out.println("success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
