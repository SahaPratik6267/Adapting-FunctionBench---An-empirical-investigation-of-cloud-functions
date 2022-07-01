package randomDiskIO;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class randomDiskIO implements RequestHandler<Map<String, String>, Response> {
    private static final String directory = "/tmp/";


    private static final String CONTAINER_ID = UUID.randomUUID().toString();
    private static boolean cold = true;
    @Override
    public Response handleRequest(Map<String, String> parameters, Context context) {
        String fileSize = parameters.getOrDefault("file_size", "");
        String biteSize = parameters.getOrDefault("byte_size", "");

        int file_size= Integer.parseInt(fileSize);
        int byte_size= Integer.parseInt(biteSize);
        byte[] block = new byte[byte_size];
        int total_file_bytes = file_size * 1024 * 1024 - byte_size;

        RandomAccessFile f = null;
        long start= System.nanoTime();
        try {
            f = new RandomAccessFile(directory+"t.txt", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(int i=0;i<=total_file_bytes / byte_size;i++){
            try {
                f.seek(new Random().nextInt(total_file_bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
          new Random().nextBytes(block);
          f.write(block);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Long elapsedTimeInSecond = System.nanoTime() - start;
        double disk_write_latency = (double) elapsedTimeInSecond / 1_000_000_000;
        double disk_write_bandwidth = (double)file_size / elapsedTimeInSecond;
        ProcessBuilder pb = new ProcessBuilder("ls", "-alh", "/tmp/")
                .inheritIO();

        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int retval = 0;
        try {
            retval = p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        start= System.nanoTime();
        try {
            f = new RandomAccessFile(directory+"t.txt", "r");
            for(int i=0;i<=total_file_bytes / byte_size;i++){
                f.seek(new Random().nextInt(total_file_bytes));
                f.read(block);
            }
            f.close();

            pb = new ProcessBuilder("rm", "-rf", directory)
                    .inheritIO();

            p = pb.start();
            retval = p.waitFor();
        } catch (IOException | InterruptedException e){
            System.out.println(e.getMessage());
        }
         elapsedTimeInSecond = System.nanoTime() - start;
        double disk_read_latency = (double) elapsedTimeInSecond / 1_000_000_000;
        double disk_read_bandwidth = (double)file_size / elapsedTimeInSecond;

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