import com.neu.dao.RedisRedissonUtil;
import com.neu.manager.StatManager;
import com.neu.pojo.Stat;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.neu.dao.RedisRedissonUtil.getRedisClusterClient;

@WebServlet(name = "ResortServlet")
public class StatServlet extends HttpServlet {
    final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(StatServlet.class);

    StatManager statManager = new StatManager();

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        setNotFoundResponse(res);
    }



    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String urlPath = req.getPathInfo();

        logger.info("Get Url: " + urlPath);

        // check we have a URL!
        if (urlPath == null || urlPath.length() == 0) {
            setNotFoundResponse(res);
            return;
        }


        String[] urlParts = urlPath.split("/");


        logger.info(urlParts[0]+" "+urlParts[1]);



        String key =  urlParts[1];
        String mess = "";

        try{
            //String result = this.jedis(query);


            /*------------*/
            RedissonClient client = getRedisClusterClient();
            RBucket<Integer> bucket = client.getBucket(key);
            Object obj = bucket.get();
            mess+=String.valueOf(obj);
            /*------------*/


        } catch (Exception e) {

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);


            mess+=sw.toString();
        }


        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(mess);

    }


    String url = "redis://skiresortcachecluster.gytc6f.clustercfg.use1.cache.amazonaws.com:6379";
    //String url = "redis://localhost:6379";;


    String host = "skiresortcachecluster.gytc6f.clustercfg.use1.cache.amazonaws.com";



    public String jedis(String key){

        List<String> list = new ArrayList<String>();
        list.add("skiresortcachecluster-0001-001.gytc6f.0001.use1.cache.amazonaws.com");
        list.add("skiresortcachecluster-0002-001.gytc6f.0001.use1.cache.amazonaws.com");
        list.add("skiresortcachecluster-0003-001.gytc6f.0001.use1.cache.amazonaws.com");
        list.add("skiresortcachecluster-0004-001.gytc6f.0001.use1.cache.amazonaws.com");
        list.add("skiresortcachecluster-0005-001.gytc6f.0001.use1.cache.amazonaws.com:");
        list.add("skiresortcachecluster-0006-001.gytc6f.0001.use1.cache.amazonaws.com:");
        list.add("skiresortcachecluster-0007-001.gytc6f.0001.use1.cache.amazonaws.com:");
        list.add("skiresortcachecluster-0008-001.gytc6f.0001.use1.cache.amazonaws.com:");


        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        //Jedis Cluster will attempt to discover cluster nodes automatically
        jedisClusterNodes.add(new HostAndPort(host, 7379));
        for(String str  : list){
            jedisClusterNodes.add(new HostAndPort(str, 7379));
        }

        JedisCluster jc = new JedisCluster(jedisClusterNodes);
        String value = jc.get(key);

        return value;
    }




//    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//        res.setContentType("application/json");
//        res.setCharacterEncoding("UTF-8");
//
//        String urlPath = req.getPathInfo();
//
//        logger.info("Get Url: " + urlPath);
//
//        // check we have a URL!
//        if (urlPath == null || urlPath.length() == 0) {
//            setNotFoundResponse(res);
//            return;
//        }
//
//
//        String[] urlParts = urlPath.split("/");
//
//
//        logger.info(urlParts[0]+" "+urlParts[1]);
//
//        String getStr = req.getParameter("get");
//        String postStr = req.getParameter("post");
//
//
//        if ("skiers".equals(urlParts[1])) {
//            if("true".equals(getStr)) {
//                Stat stat = statManager.queryStat("skiers", "get",
//                        (long) SkierServlet.getGetCount(),
//                        (double) SkierServlet.getGetMean(),
//                        (long) SkierServlet.getGetMax());
//                handleGetStat(res, "skiers", "GET", stat);
//            }else if ("true".equals(postStr)){
//                Stat stat = statManager.queryStat("skiers", "post",
//                        (long) SkierServlet.getPostCount(),
//                        (double) SkierServlet.getPostMean(),
//                        (long) SkierServlet.getPostMax());
//                handleGetStat(res, "skiers", "POST", stat);
//            }
//        } else if ("resorts".equals(urlParts[1])) {
//            if("true".equals(getStr)) {
//                Stat stat = statManager.queryStat("resorts", "get",
//                        (long) ResortServlet.getGetCount(),
//                        (double) ResortServlet.getGetMean(),
//                        (long) ResortServlet.getGetMax());
//                handleGetStat(res, "resorts", "GET", stat);
//            }else if ("true".equals(postStr)){
//                Stat stat = statManager.queryStat("resorts", "post",
//                        (long) ResortServlet.getPostCount(),
//                        (double) ResortServlet.getPostMean(),
//                        (long) ResortServlet.getPostMax());
//                handleGetStat(res, "resorts", "POST", stat);
//            }
//        }else {
//            setInvalidParaResponse(res);
//        }
//    }

    private void handleGetStat(HttpServletResponse res, String endpoint, String operation, Stat stat) throws IOException{
        res.setStatus(HttpServletResponse.SC_OK);

        String message = null;

         message = "{\n" +
                 "  \"endpointStats\": [\n" +
                 "    {\n" +
                 "      \"URL\": \"/%s\",\n" +
                 "      \"operation\": \"%s\",\n" +
                 "      \"mean\": %d,\n" +
                 "      \"max\": %d\n" +
                 "    }\n" +
                 "  ]\n" +
                 "}";

        res.getWriter().write(String.format(message, endpoint, operation, (int)stat.getMean(), (int)stat.getMax()));
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
}
