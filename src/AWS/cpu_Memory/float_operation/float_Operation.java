package AWS.cpu_Memory.float_operation;

import java.util.Date;


public class float_Operation {

    public static void main(String[] args) throws InterruptedException {

        int n=180;
        Date now = new Date();
        long start = System.nanoTime();
        System.out.println(System.currentTimeMillis());

        for(int i=1; i<n; i++){
           double sin_i = Math.sin(i);
            double cos_i = Math.cos(i);
            double sqrt_i = Math.sqrt(i);


        }

        System.out.println(System.currentTimeMillis());
        long latency = now.getTime()- start;
//        System.out.println(latency);
    }







}


