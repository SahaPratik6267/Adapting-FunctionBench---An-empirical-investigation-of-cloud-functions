package sequentialDiskIO;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class sequentialDiskIO implements RequestHandler<Map<String, String>, Response> {
    private static final String directory = "/tmp/";


    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static boolean cold = true;
    @Override
    public Response handleRequest(Map<String, String> parameters, Context context) {
        String fileSize = parameters.getOrDefault("file_size", "");
        String biteSize = parameters.getOrDefault("byte_size", "");

        int file_size= Integer.parseInt(fileSize);
        int byte_size= Integer.parseInt(biteSize);



        BufferedOutputStream bw = null;
        try {
            bw = new BufferedOutputStream(
                    new FileOutputStream(new File(directory+"t5"),true),byte_size);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] block = new byte[file_size * 1024 * 1024];
        new Random().nextBytes(block);
        long start= System.nanoTime();
        try {
            bw.write(block);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Long elapsedTimeInSecond = System.nanoTime() - start;
        double disk_write_latency = (double) elapsedTimeInSecond / 1_000_000_000;
        double disk_write_bandwidth = (double)file_size / (double)disk_write_latency;;
        ProcessBuilder pb = new ProcessBuilder("ls", "-alh", "/tmp/")
                .inheritIO();

        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        start= System.nanoTime();

        try {
            BufferedReader reader =
                    new BufferedReader(new FileReader(directory+"t5"),byte_size);

            while(reader.readLine()!= null){

            }
            } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        elapsedTimeInSecond = System.nanoTime() - start;
        double disk_read_latency = (double) elapsedTimeInSecond / 1_000_000_000;
        double disk_read_bandwidth = (double)file_size / disk_read_latency;


        pb = new ProcessBuilder("rm", "-rf", directory)
                    .inheritIO();

        try {
            p = pb.start();

        p.waitFor();
        } catch (IOException | InterruptedException e){
            System.out.println(e.getMessage());
        }


        String result= (new StringBuilder())
                .append("{disk_write_latency:")
                .append(String.valueOf(disk_write_latency))
                .append(",disk_write_bandwidth:")
                .append(String.valueOf(disk_write_bandwidth))
                .append(",disk_read_latency:")
                .append(String.valueOf(disk_read_latency))
                .append(",disk_read_bandwidth:")
                .append(String.valueOf(disk_read_bandwidth))
                .append("}")
                .toString();

        return createSuccessResponse(result, context.getAwsRequestId());
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