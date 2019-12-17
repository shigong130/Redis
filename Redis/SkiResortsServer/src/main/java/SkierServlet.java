import com.google.gson.Gson;
import com.neu.Util.TestUtil;
import com.neu.dao.RedisHelper;
import com.neu.manager.SkierManager;
import com.neu.manager.StatManager;
import com.neu.pojo.Ride;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
    private final static int StatUpdateThreshold = 1000;
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SkierServlet.class);

    private SkierManager skierManager = new SkierManager(queue);
    private static StatManager statManager = new StatManager();

    private static ArrayBlockingQueue<Ride> queue = new ArrayBlockingQueue<Ride>(100000);
    private static long getCount = 0;
    private static double getMean = 0;
    private static long getMax = 0;
    private static long postCount = 0;
    private static double postMean = 0;
    private static long postMax = 0;

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        //logger.info("Post : " + urlPath);

        // check we have a URL!
        if (urlPath == null || urlPath.length() == 0) {
            setNotFoundResponse(res);
            return;
        }

        String[] urlParts = urlPath.split("/");


        if (!isUrlValid(urlParts)) {
            setNotFoundResponse(res);
        } else if (urlParts[2].equals("seasons")) {
            handlePostSeasons(req, res, urlParts);
            calculatePostStat(startTime);
        } else{
            setNotFoundResponse(res);
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        logger.info(urlPath);

        // check we have a URL!
        if (urlPath == null || urlPath.length() == 0) {
            setNotFoundResponse(res);
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            setNotFoundResponse(res);
        } else if(urlParts[2].equals("vertical")) {
            handleGetVertical(req, res, urlParts);
            calculateGetStat(startTime);
        } else if (urlParts[2].equals("seasons")) {
            long latency = System.currentTimeMillis() - startTime;
            handleGetSeasons(res, urlParts);
            calculateGetStat(startTime);
        } else{
            setNotFoundResponse(res);
        }
    }

    private void handleGetVertical(HttpServletRequest req, HttpServletResponse res,  String[] url) throws IOException{
        String message = null;
        Integer resort, season=null;

        String resortStr = req.getParameter("resort");
        String seasonStr = req.getParameter("season");

        // Parameters validations
        if(resortStr==null || resortStr.length()==0) {
            setInvalidParaResponse(res);
            return;
        }

        try{
            resort = Integer.valueOf(resortStr);
            if(seasonStr!=null){
                season = Integer.valueOf(seasonStr);
            }
        }catch(Exception e) {
            setInvalidParaResponse(res);
            return;
        }

        int skierId;
        try{
            skierId = Integer.valueOf(url[1]);
        }catch(Exception e){
            setInvalidParaResponse(res);
            return;
        }

        if(skierId<0){
            setNotFoundResponse(res);
            return;
        }


        // Query
        try{
            if(season!=null ){
                message = skierManager.getVerticalBySkier(skierId, resort, season);
            }else {
                message = skierManager.getVerticalBySkier(skierId, resort);
            }
        }catch (Exception e) {
            setNotFoundResponse(res);
            return;
        }

        res.getWriter().write(message);
        res.setStatus(HttpServletResponse.SC_OK);
    }

    private void handleGetSeasons(HttpServletResponse res, String[] url) throws IOException{
        int vertical = 0;
        int resortId, seasonId, dayId, skierId;
        try{
            resortId = Integer.valueOf(url[1]);
            seasonId = Integer.valueOf(url[3]);
            dayId = Integer.valueOf(url[5]);
            skierId = Integer.valueOf(url[7]);
        }catch(Exception e){
            setInvalidParaResponse(res);
            return;
        }

        if(resortId<0 || seasonId<0 || dayId<0 || skierId<0){
            setNotFoundResponse(res);
            return;
        }

        try{
            try {
                long start = System.currentTimeMillis();

                if(TestUtil.doUseRedisDb) {
                    vertical = RedisHelper.getVerticalFromRedis(String.valueOf(skierId), String.valueOf(resortId),
                            String.valueOf(seasonId), String.valueOf(dayId));
                }else {
                    vertical = skierManager.getVerticalFromMysql(String.valueOf(skierId), String.valueOf(resortId),
                            String.valueOf(seasonId), String.valueOf(dayId));

                }

            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        }catch(Exception e){
            setNotFoundResponse(res);
            return;
        }

        res.getWriter().write(String.valueOf(vertical));
        res.setStatus(HttpServletResponse.SC_OK);
    }

    private void handlePostSeasons(HttpServletRequest req, HttpServletResponse res, String[] url) throws IOException{
        long time, liftId;
        long resortId, seasonId, dayId, skierId;

        String str, wholeStr = "";
        try {
            resortId = Long.valueOf(url[1]);
            seasonId = Long.valueOf(url[3]);
            dayId = Long.valueOf(url[5]);
            skierId = Long.valueOf(url[7]);

            BufferedReader br = req.getReader();
            while ((str = br.readLine()) != null) {
                wholeStr += str;
            }

            Object obj = new JSONParser().parse(wholeStr);
            JSONObject jo = (JSONObject) obj;

            Object timeObj = jo.get("time");
            Object liftObj = jo.get("liftID");
            time = (Long) timeObj;
            liftId = (Long) liftObj;

            if(resortId<0 || seasonId<0 || dayId<0 || skierId<0 || time<0 || liftId<0){
                setNotFoundResponse(res);
                return;
            }

            try{
                boolean result = skierManager.addRide((int)skierId, (int)resortId, (int)seasonId,
                        (int)dayId, (int)liftId, (int)time);
                if(!result) new Exception("Failed to save ride to database");
            }catch(Exception e){

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                setNotFoundResponse(res, sw.toString());
                return;
            }

            res.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            logger.error(e.getMessage());
            setInvalidParaResponse(res);
            return;
        }
    }

    private void setInvalidParaResponse(HttpServletResponse res) throws IOException{
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String message = "{\n" +
                "  \"message\": \"InvalidParameters\"\n" +
                "}";
        res.getWriter().write(message);
    }

    private void setNotFoundResponse(HttpServletResponse res) throws IOException{
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        String message = "{\n" +
                "  \"message\": \"Data not found\"\n" +
                "}";
        res.getWriter().write(message);
    }


    private void setNotFoundResponse(HttpServletResponse res, String error) throws IOException{
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        String message = "{\n" +
                "  \"message\": \""+ error +"\"\n" +
                "}";
        res.getWriter().write(message);
    }

    private boolean isUrlValid(String[] url) {
        if(url==null || (url.length!=3 && url.length!=8) ) return false;
        return true;
    }

    private static void calculateGetStat(long startTime) {
        long latency = System.currentTimeMillis() - startTime;
        getMax = Math.max(getMax, latency);
        double total = getMean * getCount;
        getMean = (total+latency)/ (getCount+1);
        getCount++;

        if(getCount>=StatUpdateThreshold){
            statManager.addStat("skiers", "get", getCount, getMean, getMax);
            getCount=0;
            getMean=0.0;
            getMax=0;
        }
    }

    private static void calculatePostStat(long startTime) {
        long latency = System.currentTimeMillis() - startTime;

        postMax = Math.max(postMax, latency);
        double total = postMean * postCount;
        postMean = (total+latency)/ (postCount+1);
        postCount++;

        if(postCount>=StatUpdateThreshold){
            statManager.addStat("skiers", "post", postCount, postMean, postMax);
            postCount=0;
            postMean=0.0;
            postMax=0;
        }
    }


    public static long getGetCount() {
        return getCount;
    }

    public static double getGetMean() {
        return getMean;
    }

    public static long getGetMax() {
        return getMax;
    }

    public static long getPostCount() {
        return postCount;
    }

    public static double getPostMean() {
        return postMean;
    }

    public static long getPostMax() {
        return postMax;
    }
}
