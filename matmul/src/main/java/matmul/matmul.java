package matmul;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class matmul implements RequestHandler<Map<String, String>, Response> {
	
	private static final String CONTAINER_ID = UUID.randomUUID().toString();
	private static boolean cold = true;

	@Override
	public Response handleRequest(Map<String, String> parameters, Context context) {
		String nString = parameters.getOrDefault("n", "");

		if (!isNumeric(nString)) {
			return createErrorResponse("Please pass a valid number 'n'.", context.getAwsRequestId());
		}

		int n = Integer.parseInt(nString);
		long result = matmul(n);

		return createSuccessResponse(String.valueOf(result), context.getAwsRequestId());
	}

	public long matmul(int n) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat matA = new Mat(n,n, CvType.CV_64F);
        Mat matB = new Mat(n,n, CvType.CV_64F);
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                matA.put(i,j,new Random().nextInt());
                matB.put(i,j,new Random().nextDouble());
            }
        }

        Mat matC = new Mat();
		long start= System.nanoTime();
        Core.multiply(matA,matB,matC);
		long latency= System.nanoTime()-start;
        return latency;
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
		boolean coldWarm = matmul.cold;
		matmul.cold = false;
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
