package s3DownloadUpload;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.lang.System.out;

public class s3DownloadUpload implements RequestHandler<Map<String, String>, Response> {

    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static boolean cold = true;
    @Override
    public Response handleRequest(Map<String, String> input, Context context) {

        String input_bucket  = input.getOrDefault("input_bucket", "");
        String object_key  = input.getOrDefault("object_key", "");
        String output_bucket  = input.getOrDefault("output_bucket", "");

        String result= downloadAndUpload(input_bucket,object_key,output_bucket);

        return createSuccessResponse(result, context.getAwsRequestId());
    }
    public String downloadAndUpload(String input_bucket,String object_key, String output_bucket) {

        String bucket_name = input_bucket;
        String key_name = object_key;
        String downloadPath = "/tmp/";


        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
        long start = 0;
        try {

            start = System.nanoTime();
            S3Object o = s3.getObject(bucket_name, key_name);

            S3ObjectInputStream s3is = o.getObjectContent();

            FileOutputStream fos = new FileOutputStream(downloadPath + key_name);
            byte[] read_buf = s3is.readAllBytes();
            fos.write(read_buf);

            Set<PosixFilePermission> chmodfullaccess = PosixFilePermissions.fromString("rwxrwxrwx");

            Files.setPosixFilePermissions(Paths.get("/tmp/"), chmodfullaccess);
            s3is.close();
            fos.close();
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            out.println("ex1");
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            out.println("ex2s");
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            out.println("ex3");
            System.exit(1);
        }

        Long download_time = System.nanoTime() - start;

        start = System.nanoTime();
        PutObjectRequest request = new PutObjectRequest(output_bucket, key_name, new File(downloadPath + key_name));
        s3.putObject(request);

        Long upload_time = System.nanoTime() - start;

        String result= (new StringBuilder())
                .append("{download_time:")
                .append(download_time)
                .append(",upload_time:")
                .append(upload_time)
                .toString();

        return result;

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

