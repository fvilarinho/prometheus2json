package br.app.vila.prometheus2json.core;

import br.app.vila.prometheus2json.constants.Constants;
import br.app.vila.prometheus2json.helpers.Header;
import br.app.vila.prometheus2json.helpers.Job;
import br.app.vila.prometheus2json.helpers.Metric;
import br.app.vila.prometheus2json.helpers.Url;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines the job task.
 *
 * @author fvilarin
 */
public class JobTask extends TimerTask {
    private static final Logger logger = LogManager.getLogger(JobTask.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    // Accept self-signed TLS certificates in the handshake (source or destination).
    static {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;

            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private final Job job;

    private HttpURLConnection sourceConnection;
    private HttpURLConnection destinationConnection;

    /**
     * Default constructor.
     *
     * @param job Instance that contains the job definition.
     */
    public JobTask(Job job) {
        super();

        this.job = job;
    }

    /**
     * Executes the job.
     *
     * @throws IOException Failed to execute the job.
     */
    public void execute() throws IOException {
        List<Metric> metrics = parseMetrics();

        if (metrics != null && !metrics.isEmpty())
            convertToJson(metrics);
    }

    /**
     * Parse the prometheus metrics.
     *
     * @return List containing all parsed prometheus metrics.
     * @throws IOException Failed to parse prometheus metrics.
     */
    public List<Metric> parseMetrics() throws IOException {
        Url source = this.job.getSource();
        long timestamp = new Date().getTime();
        List<Metric> metrics = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(Objects.requireNonNull(getSourceStream(source)))) {
            if (this.sourceConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("Failed to fetch data for the job {}! Please check your settings!", job.getId());
                logger.debug(this.sourceConnection.getResponseMessage());
            } else {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#") || line.trim().isEmpty())
                        continue;

                    String regex = "([a-zA-Z_:][a-zA-Z0-9_:]*)(\\{([^{}]*)\\})?\\s+([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        String name = matcher.group(1);
                        String labels = matcher.group(3);

                        if (labels != null && !labels.isEmpty())
                            labels = labels.replaceAll("\"", "");

                        double value = 0d;

                        try {
                            value = Double.parseDouble(matcher.group(4));

                            if (Double.isNaN(value))
                                continue;
                        } catch (Throwable ignore) {
                        }

                        metrics.add(new Metric(timestamp, name, labels, value));
                    }
                }

                logger.info("{} metrics fetched for the job {}!", metrics.size(), job.getId());
            }
        }

        return metrics;
    }

    /**
     * Convert metrics to JSON format.
     *
     * @param metrics List containing all parsed metrics.
     * @throws IOException Failed to convert the metrics.
     */
    private void convertToJson(List<Metric> metrics) throws IOException {
        String value = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metrics);

        logger.debug(value);

        Url destination = job.getDestination();

        try (OutputStream out = Objects.requireNonNull(getDestinationStream(destination))) {
            out.write(value.getBytes());
            out.flush();

            if (this.destinationConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("Failed to save data for the job {}! Please check your settings!", job.getId());
                logger.debug(this.destinationConnection.getResponseMessage());
            } else
                logger.info("{} metrics saved for the job {}!", metrics.size(), job.getId());
        } finally {
            close();
        }
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Returns the stream of the source.
     *
     * @param source Instance that contains the source URL.
     * @return Instance that contains the stream.
     * @throws IOException Failed to fetch the source.
     */
    private InputStreamReader getSourceStream(Url source) throws IOException {
        String url = source.getUrl().toLowerCase();

        this.sourceConnection = null;

        if (url.startsWith("file://")) {
            url = url.replace("file://", "");

            return new InputStreamReader(new FileInputStream(url));
        } else if (url.startsWith("http://") || url.startsWith("https://")) {
            this.sourceConnection = (HttpURLConnection) new URL(url).openConnection();

            String method = source.getMethod();

            if(method != null && !method.isEmpty())
                this.sourceConnection.setRequestMethod(method);
            else
                this.sourceConnection.setRequestMethod(Constants.DEFAULT_METHOD);

            String userAgent = source.getUserAgent();

            if (userAgent != null && !userAgent.isEmpty())
                this.sourceConnection.setRequestProperty("User-Agent", userAgent);
            else
                this.sourceConnection.setRequestProperty("User-Agent", Constants.DEFAULT_USER_AGENT);

            List<Header> headers = source.getHeaders();

            if(headers != null && !headers.isEmpty()) {
                for (Header header : headers)
                    if(header.getName() != null && !header.getName().isEmpty() && header.getValue() != null && !header.getValue().isEmpty())
                        this.sourceConnection.setRequestProperty(header.getName(), header.getValue());
            }

            boolean basicAuthentication = source.isBasicAuthentication();
            String userName = source.getUserName();
            String password = source.getPassword();

            if (basicAuthentication && userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
                String authorization = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes(StandardCharsets.UTF_8));

                this.sourceConnection.setRequestProperty("Authorization", "Basic " + authorization);
            }

            return new InputStreamReader(this.sourceConnection.getInputStream());
        }

        return null;
    }

    /**
     * Returns the stream of the destination.
     *
     * @param destination Instance that contains the destination URL.
     * @return Instance that contains the stream.
     * @throws IOException Failed to push to the destination.
     */
    private OutputStream getDestinationStream(Url destination) throws IOException {
        String url = destination.getUrl().toLowerCase();

        this.destinationConnection = null;

        if (url.startsWith("file://")) {
            url = url.replace("file://", "");

            return new FileOutputStream(url);
        } else if (url.startsWith("http://") || url.startsWith("https://")) {
            this.destinationConnection = (HttpURLConnection) new URL(url).openConnection();

            String method = destination.getMethod();

            if(method != null && !method.isEmpty())
                this.destinationConnection.setRequestMethod(method);
            else
                this.destinationConnection.setRequestMethod(Constants.DEFAULT_METHOD);

            String userAgent = destination.getUserAgent();

            if (userAgent != null && !userAgent.isEmpty())
                this.destinationConnection.setRequestProperty("User-Agent", userAgent);
            else
                this.destinationConnection.setRequestProperty("User-Agent", Constants.DEFAULT_USER_AGENT);

            this.destinationConnection.setRequestProperty("Content-Type", Constants.DEFAULT_CONTENT_TYPE);
            this.destinationConnection.setRequestProperty("Accept", Constants.DEFAULT_CONTENT_TYPE);

            List<Header> headers = destination.getHeaders();

            if(headers != null && !headers.isEmpty())
                for (Header header : headers)
                    if(header.getName() != null && !header.getName().isEmpty() && header.getValue() != null && !header.getValue().isEmpty())
                        this.destinationConnection.setRequestProperty(header.getName(), header.getValue());

            boolean basicAuthentication = destination.isBasicAuthentication();
            String userName = destination.getUserName();
            String password = destination.getPassword();

            if (basicAuthentication && userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
                String authorization = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes(StandardCharsets.UTF_8));

                this.destinationConnection.setRequestProperty("Authorization", "Basic " + authorization);
            }

            this.destinationConnection.setDoOutput(true);

            return new DataOutputStream(this.destinationConnection.getOutputStream());
        }

        return null;
    }

    /**
     * Close all streams (source and destination) connections.
     */
    private void close() {
        if(this.sourceConnection != null)
            this.sourceConnection.disconnect();

        if(this.destinationConnection != null)
            this.destinationConnection.disconnect();

        this.sourceConnection = null;
        this.destinationConnection = null;
    }
}