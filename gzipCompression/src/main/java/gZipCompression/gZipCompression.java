package gZipCompression;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class gZipCompression implements RequestHandler<Map<String, String>, Response> {

    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static boolean cold = true;

    @Override
    public Response handleRequest(Map<String, String> input, Context context) {
        String nString = input.getOrDefault("file_size", "");
        int n = Integer.parseInt(nString);
        String resultstring = null;
        try {
            resultstring= gZipCompressionFunc(n);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return createSuccessResponse(resultstring, context.getAwsRequestId());
    }
    public String gZipCompressionFunc(int n) throws IOException {
        long start= System.nanoTime();
        RandomAccessFile f = new RandomAccessFile("/tmp/t/", "rw");
        f.setLength(n * 1024 * 1024);
        byte[] strToBytes = new byte[0];
        new Random().nextBytes(strToBytes);
        f.write(strToBytes);
              ProcessBuilder pb = new ProcessBuilder("ls", "-alh", "/tmp/")
                .inheritIO();

Process p=null;
        try {
            pb.start();
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long disk_Latency= System.nanoTime()-start;
        start= System.nanoTime();
        try (GZIPOutputStream gos = new GZIPOutputStream(
                new FileOutputStream(new File("/tmp/test.gz/")));
             FileInputStream fis = new FileInputStream(f.getFD())) {
            gos.write(fis.readAllBytes());
        }
        long compress_Latency= System.nanoTime()-start;

        String res= "{'disk_Latency':"+String.valueOf(disk_Latency)+", 'compress_Latency':"+String.valueOf(compress_Latency)+"}";

        return  res;
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
