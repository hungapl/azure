public class HttpRequestException extends Exception {
    private String url;
    private int statusCode;
    private String reason;

    public HttpRequestException(String url, int statusCode, String reason) {
        this.url = url;
        this.statusCode = statusCode;
        this.reason = reason;
    }

    public String toString() {
        return "ERROR: " + this.url + " RESPONSE " + this.statusCode + " - " + this.reason;
    }
}
