package ru.flashsafe.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alexander Krysin
 */
public class ResourcesUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesUtil.class);
    
    public static String loadQSS(String name) {
        String qss = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ResourcesUtil.class.getResourceAsStream("/css/" + name + ".qss")));
            String line;
            while((line = reader.readLine()) != null) {
                qss += line;
            }
        } catch(IOException e) {
            LOGGER.error("");
        } finally {
            try {
                if(reader != null) {
                    reader.close();
                }
            } catch(IOException e) {
            }
        }
        return qss;
    }
    
}
