import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AssignmentRead {

    int maxNumThreads;
    int numSkiers;
    int numLifts;
    int numRuns;
    String serverAddress;

    AtomicInteger succeedRequestCount;
    AtomicInteger failedRequestCount;

    List<HttpRecord> httpRecordFullList;


    private static final int SKI_DAY_LENGTH = 420;
    private static final int DEFAULT_NUM_OF_RUNS = 10;
    private static final int DEFAULT_NUM_OF_LIFTS = 40;
    final static Logger logger = Logger.getLogger(AssignmentRead.class);


    public static void main(String[] args){

        //String local="http://localhost:8080/SkiResortsServer_war_exploded/";
        //String    ec2="http://ec2-34-205-134-138.compute-1.amazonaws.com:8080/SkiResort_war/";
        String west_ec2="http://ec2-34-219-93-122.us-west-2.compute.amazonaws.com:8080/SkiResort_war/";


        AssignmentRead obj = new AssignmentRead(256, 500, 20, 40, west_ec2);
        obj.runAssignment();
    }

    public AssignmentRead(int maxNumThreads, int numSkiers, String address){
        this(maxNumThreads, numSkiers, DEFAULT_NUM_OF_RUNS, DEFAULT_NUM_OF_LIFTS, address);
    }

    public AssignmentRead(int maxNumThreads, int numSkiers, int numRuns, int numLifts, String serverAddress) {
        if(maxNumThreads>100256 || numSkiers>50000 || numRuns>20 || numLifts>60 || numLifts<5){
            throw new IllegalArgumentException("Invalid arguments");
        }

        succeedRequestCount = new AtomicInteger(0);
        failedRequestCount = new AtomicInteger(0);

        httpRecordFullList = new ArrayList<HttpRecord>();

        this.maxNumThreads = maxNumThreads;
        this.numSkiers = numSkiers;
        this.numRuns = numRuns;
        this.numLifts = numLifts;
        this.serverAddress = serverAddress;
    }

    public void runAssignment(){

        int[] liftIdRange = new int[]{1, numLifts};

        List<int[]> idRangeList = generatePhaseOneAndThreeIdRangeLists();
        int[] timeRangeList = new int[]{1, 90};

        List<int[]> idRangeList2 = generatePhaseTwoIdRangeLists();
        int[] timeRangeList2 = new int[]{91, 360};

        List<int[]> idRangeList3 = generatePhaseOneAndThreeIdRangeLists();
        int[] timeRangeList3 = new int[]{361, 420};


        int phase1NumOfThread = idRangeList.size();
        int phase2NumOfThread = idRangeList2.size();
        int phase3NumOfThread = idRangeList3.size();

        int phase1RequestNumPerThread = (int) (numRuns * 0.1 * numSkiers / phase1NumOfThread);
        int phase2RequestNumPerThread = (int) (numRuns * 0.8 * numSkiers / phase2NumOfThread);
        int phase3RequestNumPerThread = (int) (numRuns * 0.1 * numSkiers / phase3NumOfThread);

        CountDownLatch phase1Latch = new CountDownLatch((int) Math.round(phase1NumOfThread * 0.1));
        CountDownLatch phase2Latch = new CountDownLatch((int) Math.round(phase2NumOfThread * 0.1));

        logger.info("Latch1 count: " + phase1Latch.getCount());
        logger.info("Latch2 count " + phase2Latch.getCount());
        logger.info("Thread size: " + phase1NumOfThread + " " + phase2NumOfThread + " " + phase3NumOfThread);
        logger.info("Request number of threads: " + phase1RequestNumPerThread + " " + phase2RequestNumPerThread + " " + phase3RequestNumPerThread);

        List<Thread> phase1Thread = generateThreads(idRangeList, phase1RequestNumPerThread, timeRangeList,
                liftIdRange, "Phase1", phase1Latch, false);

        List<Thread> phase2Thread = generateThreads(idRangeList2, phase2RequestNumPerThread, timeRangeList2,
                liftIdRange, "Phase2", phase2Latch, false);

        List<Thread> phase3Thread = generateThreads(idRangeList3, phase3RequestNumPerThread, timeRangeList3,
                liftIdRange, "Phase3", null, true);



        ExecutorService executorService1 = Executors.newFixedThreadPool(phase1NumOfThread);
        ExecutorService executorService2 = Executors.newFixedThreadPool(phase2NumOfThread);
        ExecutorService executorService3 = Executors.newFixedThreadPool(phase3NumOfThread);


        long tStart = System.currentTimeMillis();
        for(Thread t : phase1Thread){
            //executorService1.execute(t);
            executorService1.submit(t);
        }


        try {
            phase1Latch.await();
        } catch (InterruptedException e){

        }


        for(Thread t : phase2Thread){
            executorService2.submit(t);
        }

        try {
            phase2Latch.await();
        } catch (InterruptedException e){

        }

        for(Thread t : phase3Thread){
            executorService3.submit(t);
        }



        executorService1.shutdown();
        executorService2.shutdown();
        executorService3.shutdown();



        try {
            executorService1.awaitTermination(10, TimeUnit.MINUTES);
            executorService2.awaitTermination(10, TimeUnit.MINUTES);
            executorService3.awaitTermination(10, TimeUnit.MINUTES);
        }catch (InterruptedException e){
            logger.error("Executor InterruptedException");
        }

        long tEnd = System.currentTimeMillis();

        logger.info("Total number of request: " + (phase1NumOfThread*phase1RequestNumPerThread +
                phase2NumOfThread*phase2RequestNumPerThread + phase3NumOfThread*phase3RequestNumPerThread));
        logger.info(phase1NumOfThread+" "+phase1RequestNumPerThread);
        logger.info(phase2NumOfThread+" "+phase2RequestNumPerThread);
        logger.info(phase3NumOfThread+" "+phase3RequestNumPerThread);

        logger.info("Num of succeed requests: " + succeedRequestCount.get());
        logger.info("Num of failed requests:" + failedRequestCount.get());
        logger.info("Time in millisecond to run all requests: :" + (tEnd-tStart));



        long totalLatency = 0;
        //List<HttpRecord> succeedList = new ArrayList<HttpRecord>();
        for(HttpRecord r : httpRecordFullList){
            if(r.getCode()==201){
                //succeedList.add(r);
                totalLatency+=r.getLatency();
            }
        }


        Collections.sort(httpRecordFullList, new Comparator<HttpRecord>(){
            public int compare(HttpRecord r1, HttpRecord r2){
                if (r1.getLatency()==r2.getLatency()) return 0;
                else if (r1.getLatency()<r2.getLatency()) return -1;
                else return 1;
            }
        });



        logger.info("------Latency in millisecond-------");
        logger.info("Average: "+(totalLatency/httpRecordFullList.size()));
        long Median = httpRecordFullList.size()%2==0?
                (httpRecordFullList.get(httpRecordFullList.size()/2).getLatency()+
                        httpRecordFullList.get(httpRecordFullList.size()/2+1).getLatency())/2:
                httpRecordFullList.get(httpRecordFullList.size()/2).getLatency();
        logger.info("Median: "+Median);
        logger.info("Throughput per second: " + (double)httpRecordFullList.size()/(tEnd-tStart)*1000);


        int tp99 = (int)((float)httpRecordFullList.size()/100*99);
        logger.info("Tp99: "+ httpRecordFullList.get(tp99).getLatency());

        logger.info("Max: " + httpRecordFullList.get(httpRecordFullList.size()-1).getLatency());


        Collections.sort(httpRecordFullList, new Comparator<HttpRecord>(){
            public int compare(HttpRecord r1, HttpRecord r2){
                if (r1.getStartTime()==r2.getStartTime()) return 0;
                else if (r1.getStartTime()<r2.getStartTime()) return -1;
                else return 1;
            }
        });

        Map<Long, List<Long>> map = new TreeMap<Long, List<Long>>();
        for(HttpRecord r : httpRecordFullList){
            long t = r.getStartTime()/1000;
            if(!map.containsKey(t)) map.put(t, new ArrayList<Long>());
            map.get(t).add(r.getLatency());
        }

        long start = map.keySet().iterator().next();
        for(Long key : map.keySet()){
            System.out.println((key-start)+" : "+getAverage(map.get(key)));
        }
        System.out.println();
        for(Long key : map.keySet()){
            System.out.println((key-start)+" : "+getTp99(map.get(key)));
        }
        System.out.println();
        for(Long key : map.keySet()){
            System.out.println((key-start)+" : "+ map.get(key).size());
        }

//            long start = map.keySet().iterator().next();
//            for(Long key : map.keySet()){
//                System.out.println((key-start)*2+" : "+getAverage(map.get(key)));
//                System.out.println(((key-start)*2+1)+" : "+genetateJitter(getAverage(map.get(key))));
//            }
//            System.out.println();
//            for(Long key : map.keySet()){
//                System.out.println((key-start)*2+" : "+getTp99(map.get(key)));
//                System.out.println(((key-start)*2+1)+" : "+genetateJitter(getTp99(map.get(key))));
//            }
//            System.out.println();
//            for(Long key : map.keySet()){
//                System.out.println((key-start)*2+" : "+ map.get(key).size());
//                System.out.println(((key-start)*2+1)+" : "+ genetateJitter2(map.get(key).size()));
//            }

    }

    private long getAverage(List<Long> list){
        long sum=0;
        for(Long l : list){
            sum+=l;
        }
        return sum/list.size();
    }

    private long getTp99(List<Long> list){
        Collections.sort(list);

        int tp99 = (int)((float)list.size()/100*99);
        return list.get(tp99);
    }

    private List<Thread> generateThreads(List<int[]> idRangeList, int numberOfRequest, int[] timeRange,
                                               int[] liftIdRange, String phaseName, CountDownLatch latch, boolean isPhase3){
        List<Thread> list = new ArrayList<Thread>();
        for(int i=0; i<idRangeList.size(); i++){
            String name = phaseName+"_"+i;
            Thread phase1Thread = new Thread(new WorkerRead(
                name, numberOfRequest, idRangeList.get(i)[0], idRangeList.get(i)[1], timeRange[0], timeRange[1],
                    liftIdRange[0], liftIdRange[1], latch, succeedRequestCount, failedRequestCount,
                    httpRecordFullList, isPhase3, serverAddress
            ));
            list.add(phase1Thread);
        }

        return list;
    }


    public List<int[]> generatePhaseOneAndThreeIdRangeLists(){
        int numThreads = maxNumThreads/4;
        int skierNum = numSkiers/numThreads;
        int id=1;
        List<int[]> res = new ArrayList<int[]>();
        for(int i=0; i<numThreads; i++){
            int start=id;
            id+=skierNum;
            int end = id-1;
            int[] interval = new int[]{start, end};
            res.add(interval);
        }

        res.get(res.size()-1)[1]=numSkiers;

        return res;
    }

    public List<int[]> generatePhaseTwoIdRangeLists(){
        int numThreads = maxNumThreads;
        int skierNum = numSkiers/numThreads;
        int id=1;
        List<int[]> res = new ArrayList<int[]>();
        for(int i=0; i<numThreads; i++){
            int start=id;
            id+=skierNum;
            int end = id-1;
            int[] interval = new int[]{start, end};
            res.add(interval);
        }

        res.get(res.size()-1)[1]=numSkiers;

        return res;
    }
}
