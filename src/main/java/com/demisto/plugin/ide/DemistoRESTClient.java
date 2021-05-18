package com.demisto.plugin.ide;

import com.demisto.plugin.ide.plugin_settings_setup.DemistoSetupConfig;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.IdeHttpClientHelpers;
import com.intellij.util.net.ssl.CertificateManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static com.demisto.plugin.ide.DemistoUtils.stringIsNotEmptyOrNull;

public class DemistoRESTClient {
    public static final Logger LOG = Logger.getInstance(DemistoRESTClient.class);
    private String serverUrl;
    private String apiKey;
    private Project project;
    private String playgroundID;
    private Boolean isMasterMT;
    private int[] playgroundType = new int[]{9};
    private Boolean trustAnyCertificate;
    private Boolean trackLocalChanges;

    public DemistoRESTClient(Project project) {
        this.project = project;
        // Check if the env is on MT and on Master, if does, show error.
        this.isMasterMT = this.isMTAndOnMaster();
        if (this.isMasterMT){
            this.playgroundID = "MTOnMaster";
        }
        else{
            this.playgroundID = getPlaygroundInvestigationID();

        }
    }

    private void getDemistoCreds() {
        DemistoSetupConfig config = DemistoSetupConfig.getInstance(this.project);
        assert config != null;
        LOG.info("Retrieving Demisto creds");
        this.serverUrl = config.getServerURL();
        this.apiKey = config.getApiKey();
        this.trustAnyCertificate = config.getTrustAnyCertificate();
        this.trackLocalChanges = config.getTrackLocalChanges();
        if (!DemistoUtils.stringIsNotEmptyOrNull(this.serverUrl) || !DemistoUtils.stringIsNotEmptyOrNull(this.apiKey)) {
            JOptionPane.showMessageDialog(new JFrame(), "Your Demisto credentials are missing. Please configure them in Preferences -> Tools -> Demisto Plugin Setup", "Demisto", JOptionPane.ERROR_MESSAGE);
            LOG.error("Your Demisto credentials are missing. Please configure them in Preferences -> Tools -> Demisto Plugin Setup");
        }
    }

    private String getPlaygroundInvestigationID() {
        String INVESTIGATION_SEARCH_PATH = "/investigations/search";
        String playgroundID;
        String jsonString = new JSONObject()
                .put("filter", new JSONObject()
                        .put("type", playgroundType)).toString();

        String res = sendPostRequest(jsonString, INVESTIGATION_SEARCH_PATH, null);
        assert stringIsNotEmptyOrNull(res);
        JSONObject resultObject = new JSONObject(res);
        // Take the first result
        JSONArray resultData = resultObject.getJSONArray("data");
        assert resultData != null;
        JSONObject resultMap = resultData.getJSONObject(0);
        playgroundID = resultMap.get("id").toString();

        return playgroundID;
    }

    public String sendQueryToDemisto(String query) {
        String QUERY_RUN_PATH = "/entry/execute/sync";
        LOG.info("Running query: " + query + "  in Demisto");
        assert stringIsNotEmptyOrNull(playgroundID);
        JSONObject jsonObject = new JSONObject()
                .put("data", query)
                .put("investigationId", playgroundID)
                .put("version", 0)
                .put("id", "")
                .put("args", JSONObject.NULL);
        String res = sendPostRequest(jsonObject.toString(), QUERY_RUN_PATH, null);
        assert stringIsNotEmptyOrNull(res);
        LOG.info("Response from Demisto is: " + res);
        return res;
    }

    private String executeHttpRequest(CloseableHttpClient httpClient, HttpUriRequest request) {
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            try {
                LOG.info("Response code was: " + response.getStatusLine().toString());
                HttpEntity resEntity = response.getEntity();
                Integer statusCode = response.getStatusLine().getStatusCode();
                if (statusCode.equals(200)) {
                    return EntityUtils.toString(resEntity, StandardCharsets.UTF_8);
                } else if (statusCode.equals(403)) {
                    JOptionPane.showMessageDialog(new JFrame(), "Encountered error while sending a request to Demisto,\nplease check your API key. Status code 403 - Forbidden",
                            "Demisto", JOptionPane.ERROR_MESSAGE);
                } else if (statusCode.equals(400)) {
                    JOptionPane.showMessageDialog(new JFrame(), "Encountered error while sending a request to Demisto.]n Status code 400 - Bad request. Response was: \n" +
                            EntityUtils.toString(resEntity), "Demisto", JOptionPane.ERROR_MESSAGE);
                } else if (resEntity != null) {
                    JOptionPane.showMessageDialog(new JFrame(), "Encountered error while sending a request to Demisto. Status code " + statusCode.toString() +
                            " Response message was: \n" + EntityUtils.toString(resEntity), "Demisto", JOptionPane.ERROR_MESSAGE);
                    LOG.error("Encountered error while sending a request to Demisto. Status code " + statusCode.toString() + "  Response message was: " + EntityUtils.toString(resEntity));
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }

        } catch (HttpHostConnectException e) {
            JOptionPane.showMessageDialog(new JFrame(), "Encountered error while sending a request to Demisto, check your plugin settings (server and API key). Error was: \n" + e.getMessage(),
                    "Demisto", JOptionPane.ERROR_MESSAGE);
            LOG.info("Encountered error while sending a request to Demisto, error message was: " + e.getMessage());
        } catch (ClientProtocolException e) {
            JOptionPane.showMessageDialog(new JFrame(), "Encountered error while exporting to Demisto, check your plugin settings (server and API key). Error was: \n" + e.getMessage(),
                    "Demisto", JOptionPane.ERROR_MESSAGE);
            LOG.info("Encountered error while sending a request to Demisto, error message was: " + e.getMessage());
        } catch (IOException e2) {
            LOG.info("Encountered error while sending a request to Demisto, error message was: " + e2.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Encountered error while sending a request to Demisto. \n" +
                    "It's possible you need to check the 'Trust any certificate' box. Error was: \n" + e2.getMessage(), "Demisto", JOptionPane.ERROR_MESSAGE);
        }
        return "";
    }

    public String sendPostRequest(String params, String endpointPath, String filePath) {
        this.getDemistoCreds();
        try {
            String dest = serverUrl + endpointPath;
            CloseableHttpClient httpClient = getHttpClient(dest);

            HttpPost httppost = new HttpPost(dest);
            httppost.setHeader("Authorization", apiKey);
            httppost.addHeader("Accept", "application/json");

            if (DemistoUtils.stringIsNotEmptyOrNull(filePath)) {
                LOG.info("Sending POST request. File path is: " + params);
                FileBody bin = new FileBody(new File(filePath));
                if (trackLocalChanges) {
                    httppost.setEntity(MultipartEntityBuilder.create()
                            .addPart("file", bin)
                            .addTextBody("shouldPublish", "true")
                            .build());
                } else {
                    httppost.setEntity(MultipartEntityBuilder.create()
                            .addPart("file", bin)
                            .build());
                }
            } else {
                LOG.info("Sending POST request. Params are: " + params);
                httppost.addHeader("content-type", "application/json");
                httppost.setEntity(new StringEntity(params));
            }

            return executeHttpRequest(httpClient, httppost);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LOG.error("Encountered UnsupportedEncodingException while making request to Demisto, error message was: " + e.getMessage());
        }
        return "";
    }

    public String sendGetRequest(String params, String endpointPath) {
        this.getDemistoCreds();
        String dest = serverUrl + endpointPath;
        CloseableHttpClient httpClient = getHttpClient(dest);
        HttpGet httpGet = new HttpGet(dest);
        httpGet.setHeader("Authorization", apiKey);
        httpGet.addHeader("Accept", "application/json");
        return executeHttpRequest(httpClient, httpGet);
    }

    private CloseableHttpClient getHttpClient(String url) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        IdeHttpClientHelpers.ApacheHttpClient4.setProxyCredentialsForUrlIfEnabled(provider, url);

        RequestConfig.Builder config = RequestConfig.custom();
        IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(config, url);
        RequestConfig requestConfig = config.build();
        if (trustAnyCertificate && serverUrl.contains("https://")) {
            return HttpClients.custom()
                    // Other initialization
                    .setDefaultRequestConfig(requestConfig)
                    .setSSLContext(CertificateManager.getInstance().getSslContext())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultCredentialsProvider(provider)
                    .build();
        }
        return HttpClients.custom()
                // Other initialization
                .setDefaultRequestConfig(requestConfig)
                .setSSLContext(CertificateManager.getInstance().getSslContext())
                .setDefaultCredentialsProvider(provider)
                .build();
    }

    private Boolean isMTAndOnMaster(){
        // On Master MT environments the return will be 'true'
        String response = this.sendGetRequest("", "/proxyMode");
        return response.contains("true");
    }

    public Boolean getIsMasterMT(){
        return this.isMasterMT;
    }
}