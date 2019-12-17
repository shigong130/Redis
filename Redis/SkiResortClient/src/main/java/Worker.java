import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker implements Runnable {
    final static Logger logger = Logger.getLogger(Worker.class);

    //private static final String BATH_PATH = "http://localhost:8080/SkiResortsServer_war_exploded/";
    private static final int RESORT_ID = 1;
    private static final String SEASON_ID = "1";
    private static final String DAY_ID = "1";

    private int startSkierId, endSkierId, startTime, endTime, startLiftId, endLiftId, numberOfRequest;
    private String threadName;
    private ApiClient client;
    private SkiersApi apiInstance;
    private CountDownLatch latch;

    private boolean isPhase3= false;

    AtomicInteger succeedRequestCount;
    AtomicInteger failedRequestCount;

    List<HttpRecord> recordList = new ArrayList<HttpRecord>();
    List<HttpRecord> fullList = null;


    public Worker(String threadName, int numberOfRequest,
                  int startSkierId, int endSkierId,
                  int startTime, int endTime,
                  int startLift, int endLiftId,
                  CountDownLatch latch,
                  AtomicInteger succeedRequestCount,
                  AtomicInteger failedRequestCount,
                  List<HttpRecord> fullList,
                  boolean isPhase3,
                  String serverAddress){
        this.startSkierId = startSkierId;
        this.endSkierId = endSkierId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLiftId = startLift;
        this.endLiftId = endLiftId;
        this.numberOfRequest = numberOfRequest;
        this.threadName = threadName;

        this.isPhase3 = isPhase3;

        this.latch = latch;
        this.succeedRequestCount = succeedRequestCount;
        this.failedRequestCount = failedRequestCount;

        this.fullList = fullList;

        recordList = new ArrayList<HttpRecord>();

        apiInstance = new SkiersApi();
        client = apiInstance.getApiClient();
        client.setBasePath(serverAddress);

        String message = "Thread %s will send %d requests, SkierId=[%d, %d], Time = [%d, %d], LiftId=[%d, %d]";
        logger.info(String.format(message,
                threadName, numberOfRequest, startSkierId, endSkierId, startTime, endTime, startLift, endLiftId));
        //logger.info("Thread "+threadName+" will send " + numberOfRequest + " Requests in total, ");
    }

    public void run(){
        logger.info(threadName+" started.");
        try {
            for (int i = 0; i < numberOfRequest; i++) {
                sendPostRequest(isPhase3);
            }
        } catch(Exception e){
        } finally{


            if (latch != null) {
                latch.countDown();
            }
            logger.debug(threadName + " : finished");

            synchronized (fullList){
                fullList.addAll(recordList);
            }

            logger.info(threadName+" is finished.");

        }
    }
    public void sendEmptyRequest(){
        succeedRequestCount.incrementAndGet();
    };

    public void sendPostRequest(boolean sentGetRequest){

        int skierId = ThreadLocalRandom.current().nextInt((endSkierId - startSkierId) + 1) + startSkierId;
        int time = ThreadLocalRandom.current().nextInt((endTime - startTime) + 1) + startTime;
        int liftId = ThreadLocalRandom.current().nextInt((endLiftId - startLiftId) + 1) + startLiftId;

        LiftRide lr = new LiftRide();
        lr.liftID(liftId).time(time);

        long tStart = System.currentTimeMillis();
        int statusCode = -1;
        try {
            ApiResponse<Void> response = apiInstance.writeNewLiftRideWithHttpInfo(
                    lr, RESORT_ID, SEASON_ID, DAY_ID, skierId);

            //logger.info("post request sent "+response.getStatusCode());
            if(response.getStatusCode()==201) {
                logger.debug(threadName+": Send one post request successfully");
                succeedRequestCount.incrementAndGet();
                statusCode = 201;
            } else{
                logger.error("Thread id :" + threadName+" " + "  Status code :" + response.getStatusCode() + "  Data : "
                        + response.getData());
                failedRequestCount.incrementAndGet();
                statusCode = response.getStatusCode();
            }
        } catch(ApiException e){
            logger.error("Api Exception. Thread id :" + threadName+" " + "  Status code :" + e.getCode() + "  Data : " + e.getResponseBody()+
                    " "+e.getCause()+" "+e.getMessage());
            failedRequestCount.incrementAndGet();
            statusCode = e.getCode();
        } catch (Exception e){
            logger.error("Thread id :" + threadName+" failed: ");
            e.printStackTrace();
            failedRequestCount.incrementAndGet();
        } finally{
            long latency = System.currentTimeMillis()-tStart;
            HttpRecord record = new HttpRecord(tStart, latency, HttpRequestType.POST, statusCode);
            recordList.add(record);
        }
    }

    public static void main(String[] args){
        for(int i=1; i<=1; i++) {
            //String url = "http://localhost:8080/SkiResortsServer_war_exploded/";
            String ec2 = Endpoint.ec2;


            Worker wk = new Worker("1", 1, 1, 1, i, i, 1, 1, null,
                    new AtomicInteger(0), new AtomicInteger(0), new ArrayList<HttpRecord>(), false, ec2);
            new Thread(wk).start();
        }
    }
}
