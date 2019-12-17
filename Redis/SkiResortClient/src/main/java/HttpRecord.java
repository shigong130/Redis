public class HttpRecord {
    private long startTime;
    private long latency;
    private HttpRequestType type;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public HttpRequestType getType() {
        return type;
    }

    public void setType(HttpRequestType type) {
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    private int code;

    public HttpRecord(long startTime, long latency, HttpRequestType type, int code) {
        this.startTime = startTime;
        this.latency = latency;
        this.type = type;
        this.code = code;
    }
}
