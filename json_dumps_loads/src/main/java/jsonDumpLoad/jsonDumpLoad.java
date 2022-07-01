package jsonDumpLoad;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

public class jsonDumpLoad implements RequestHandler<Map<String, String>, Response> {

    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static boolean cold = true;

    @Override
    public Response handleRequest(Map<String, String> input, Context context) {

        String link = input.getOrDefault("link", "");
        String result = null;
        try {
            result = jsonDumpsLoads(link);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return createSuccessResponse(result, context.getAwsRequestId());
    }

    public String jsonDumpsLoads(String link) throws IOException {

        String jsonString;
        long start= System.nanoTime();
        InputStream is = new URL(link).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonString = readAll(rd);
        } finally {
            is.close();
        }
        Long Network = System.nanoTime() - start;
        start= System.nanoTime();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(jsonString);

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter().withObjectIndenter(new DefaultIndenter("    ", "\n"));
        String jsondump= mapper.writer(printer).writeValueAsString(actualObj);
        Long serialization = System.nanoTime() - start;
        String result= (new StringBuilder())
                .append("{Network:")
                .append(Network)
                .append(",Serialization:")
                .append(serialization)
                .toString();

                return result;
    }
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public Response createErrorResponse(String message, String requestId) {
        Response response = new Response(changeColdWarm(),"[400] " + message, requestId, CONTAINER_ID);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String responseJSON = mapper.writeValueAsString(response);
            throw new LambdaException(responseJSON);
        } catch (JsonProcessingException e) {
            throw new LambdaException("{ \"result\": \"[400] Error while creating JSON response.\" }");
        }
    }

    private boolean changeColdWarm() {
        boolean coldWarm = this.cold;
        this.cold = false;
        return coldWarm;
    }

    public Response createSuccessResponse(String message, String requestId) {
        Response response = new Response(changeColdWarm(), message, requestId, CONTAINER_ID);
        response.addCPUAndVMInfo();
        return response;
    }

    public boolean isNumeric(String value) {
        return value.matches("\\d+");
    }
}



