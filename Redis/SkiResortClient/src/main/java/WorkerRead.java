import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerRead implements Runnable {
    final static Logger logger = Logger.getLogger(WorkerRead.class);

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

    HttpClientExample httpClient;


    public WorkerRead(String threadName, int numberOfRequest,
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


        httpClient = new HttpClientExample();
    }

    public void run(){
        logger.info(threadName+" started.");
        try {
            for (int i = 0; i < numberOfRequest; i++) {
                sendGetRequest(isPhase3);
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

    public void sendGetRequest(boolean sentGetRequest){

        int skierId = ThreadLocalRandom.current().nextInt((endSkierId - startSkierId) + 1) + startSkierId;

        long tStart = System.currentTimeMillis();
        int statusCode = -1;
        try {
            String urlTemplate = Endpoint.ec2+"skiers/%s/seasons/%s/days/%s/skiers/%s";
            String url = String.format(urlTemplate, RESORT_ID, SEASON_ID, DAY_ID, skierId);
            String result = httpClient.sendGet(url);
            System.out.println(result);
            try{
                Integer.valueOf(result);
                succeedRequestCount.incrementAndGet();
            }catch(Exception e){
                failedRequestCount.incrementAndGet();
            }

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

            WorkerRead wk = new WorkerRead("1", 1, 1, 1, i, i, 1, 1, null,
                    new AtomicInteger(0), new AtomicInteger(0), new ArrayList<HttpRecord>(), false, ec2);
            new Thread(wk).start();
        }
    }
}
