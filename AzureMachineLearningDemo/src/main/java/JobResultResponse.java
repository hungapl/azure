/**
 * Stores Job results from JSON response
 *
 * @author bonapetite
 *         Created: 04/03/17
 */
public class JobResultResponse {
    public Results Results;
    public String StatusCode;

    static class Results {
        public Output output1;
    }

    static class Output {
        public String RelativeLocation;
        public String BaseLocation;
        public String SasBlobToken;
    }
}
