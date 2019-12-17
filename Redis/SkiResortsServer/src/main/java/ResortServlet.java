import com.neu.manager.ResortManager;
import com.neu.manager.StatManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "ResortServlet")
public class ResortServlet extends HttpServlet {
    private final static int StatUpdateThreshold = 1000;

    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ResortServlet.class);

    private ResortManager resortManager = new ResortManager();
    private static StatManager statManager = new StatManager();

    private static long getCount = 0;
    private static double getMean = 0;
    private static long getMax = 0;
    private static long postCount = 0;
    private static double postMean = 0;
    private static long postMax = 0;

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        //res.setContentType("text/plain");
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.length() == 0) {
            setNotFoundResponse(res);
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            setNotFoundResponse(res);
        } else if (urlParts[2].equals("seasons")) {
            handlePostResorts(req, res, urlParts);
            calculatePostStat(startTime);
        } else{
            setNotFoundResponse(res);
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String urlPath = req.getPathInfo();

        logger.info("Get Url: " + urlPath);

        // check we have a URL!
        if (urlPath == null || urlPath.length() == 0) {
            handleGetResorts(res);
            calculateGetStat(startTime);
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            setNotFoundResponse(res);
        } else if (urlParts[2].equals("seasons")) {
            handleGetResortsWithId(res, urlParts);
            calculateGetStat(startTime);
        } else{
            setNotFoundResponse(res);
        }
    }

    private void handleGetResorts(HttpServletResponse res) throws IOException{
        res.setStatus(HttpServletResponse.SC_OK);

        String message = null;

        try {
            message = resortManager.getAllResort();
        } catch (Exception e) {
            setNotFoundResponse(res);
            return;
        }
        res.getWriter().write(message);
    }

    private void handleGetResortsWithId(HttpServletResponse res, String[] url) throws IOException{
        int resortId;
        String message = null;
        try{
            resortId = Integer.valueOf(url[1]);
        }catch(Exception e){
            setInvalidParaResponse(res);
            return;
        }

        try {
            message = resortManager.getSeasonsByResortId(resortId);
        } catch(Exception e){
            setNotFoundResponse(res);
            return;
        }

        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(message);
    }

    private void handlePostResorts(HttpServletRequest req, HttpServletResponse res, String[] url) throws IOException{
        long resortId, year;
        String str, wholeStr = "";
        try {
            resortId = Long.valueOf(url[1]);

            BufferedReader br = req.getReader();
            while ((str = br.readLine()) != null) {
                wholeStr += str;
            }

            Object obj = new JSONParser().parse(wholeStr);
            JSONObject jo = (JSONObject) obj;

            Object yearObj = jo.get("year");
            year = (Long) yearObj;

            if(resortId<0 || year<0) throw new Exception("Negative input");

            res.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            logger.error(e.getMessage());
            setInvalidParaResponse(res);
            return;
        }


       try{
           resortManager.addResortSeason((int)resortId, (int)year);
       }catch (Exception e) {
           setNotFoundResponse(res);
       }
    }

    private void setInvalidParaResponse(HttpServletResponse res) throws IOException{
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String dummyMessage = "{\n" +
                "  \"message\": \"400 bad request: invalid parameters\"\n" +
                "}";
        res.getWriter().write(dummyMessage);
    }

    private void setNotFoundResponse(HttpServletResponse res) throws IOException{
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        String dummyMessage = "{\n" +
                "  \"message\": \"404 not found : incorrect URL\"\n" +
                "}";
        res.getWriter().write(dummyMessage);
    }

    private boolean isUrlValid(String[] url) {
        if(url==null || url.length!=3 ) return false;
        return true;
    }



    private static void calculateGetStat(long startTime) {
        long latency = System.currentTimeMillis() - startTime;
        getMax = Math.max(getMax, latency);
        double total = getMean * getCount;
        getMean = (total+latency)/ (getCount+1);
        getCount++;

        if(getCount>=StatUpdateThreshold){
            statManager.addStat("resorts", "get", getCount, getMean, getMax);
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
            statManager.addStat("resorts", "post", postCount, postMean, postMax);
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
