package org.jaubs;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;

import java.util.Map;

public class JaubsUtils {

    public static String prettyBody(String accessToken) {

        try {
            String json = decodeJSONBody(accessToken);

            ObjectMapper mapper = new ObjectMapper();

            // Use indentation of 4 spaces
            DefaultPrettyPrinter.Indenter indenter =
                    new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
            DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
            printer.indentObjectsWith(indenter);
            printer.indentArraysWith(indenter);

            // Format the JSON
            Map tokenBodyMap = mapper.readValue(json, Map.class);
            String prettyJson = mapper.writer(printer).writeValueAsString(tokenBodyMap);
            return prettyJson;
        } catch (Exception e) {

            // If token is not a JWT
            return accessToken;
        }
    }

    private static String decodeJSONBody(String accessToken) {

        // extract token body. Will throw exception if not a JWT
        String tokenBody = accessToken.split("\\.")[1];
        String json = new String(Base64.decodeBase64URLSafe(tokenBody));
        return json;
    }

}
