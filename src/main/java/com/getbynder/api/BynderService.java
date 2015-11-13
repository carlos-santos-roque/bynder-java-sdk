package com.getbynder.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.getbynder.api.domain.Category;
import com.getbynder.api.domain.ImageAsset;
import com.getbynder.api.domain.UserAccessData;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

/**
 *
 * @author daniel.sequeira
 */
public class BynderService {

    private static final String LOGIN_PATH = BynderProperties.getInstance().getProperty("LOGIN_PATH");
    private static final String CATEGORIES_PATH = BynderProperties.getInstance().getProperty("CATEGORIES_PATH");
    private static final String IMAGE_ASSETS_PATH = BynderProperties.getInstance().getProperty("IMAGE_ASSETS_PATH");

    private static final String CONSUMER_KEY = BynderProperties.getInstance().getProperty("CONSUMER_KEY");
    private static final String CONSUMER_SECRET = BynderProperties.getInstance().getProperty("CONSUMER_SECRET");
    private static final String ACCESS_TOKEN = BynderProperties.getInstance().getProperty("ACCESS_TOKEN");
    private static final String ACCESS_TOKEN_SECRET = BynderProperties.getInstance().getProperty("ACCESS_TOKEN_SECRET");

    private final String baseUrl;
    private final UserAccessData userAccessData;

    public BynderService(final String baseUrl, final String username, final String password) throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, ClientProtocolException, IOException, URISyntaxException {
        this.baseUrl = baseUrl;
        this.userAccessData = getUserAccessData(username, password);
    }

    public UserAccessData getUserAccessData(final String username, final String password) throws OAuthMessageSignerException, OAuthExpectationFailedException,
    OAuthCommunicationException, ClientProtocolException, IOException, URISyntaxException {

        // create a consumer object and configure it with the access token and token secret obtained from the service provider
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        consumer.setTokenWithSecret(ACCESS_TOKEN, ACCESS_TOKEN_SECRET);

        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(CONSUMER_KEY, CONSUMER_SECRET));
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));

        // create an HTTP request to a protected resource
        URI loginUri = Utils.createLoginURI(new URL(baseUrl), LOGIN_PATH, params);

        HttpPost request = new HttpPost(loginUri);

        // sign the request
        consumer.sign(request);

        //consumerId shall not be used in the request
        params.remove(0);

        // set the parameters into the request
        request.setEntity(new UrlEncodedFormEntity(params));

        // send the request
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(request);

        // if request was unsuccessful
        if(response.getStatusLine().getStatusCode() != 200){
            throw new HttpResponseException(response.getStatusLine().getStatusCode(), "The request was unsuccessful");
        }

        // if successful, return the response body
        HttpEntity resEntity = response.getEntity();
        String responseBody = "";

        if (resEntity != null) {
            responseBody = EntityUtils.toString(resEntity);
        }

        //close this stream
        httpClient.close();

        // parse the response string into a JSON object
        JSONObject responseObj = new JSONObject(responseBody);

        return new UserAccessData(responseObj);
    }

    public List<Category> getCategories() throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, UnsupportedEncodingException, MalformedURLException {

        String apiGetCategoriesUrl = baseUrl+CATEGORIES_PATH;

        String oauthHeader = Utils.createOAuthHeader(CONSUMER_KEY, CONSUMER_SECRET, userAccessData, apiGetCategoriesUrl);

        Client client = ClientBuilder.newClient();

        Response response = client.target(apiGetCategoriesUrl)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", oauthHeader)
                .get();

        JSONArray responseJsonArray = new JSONArray(response.readEntity(String.class));

        List<Category> categories = new ArrayList<>();

        for(int i=0; i<responseJsonArray.length(); i++) {
            categories.add(new Category((JSONObject) responseJsonArray.get(i)));
        }

        return categories;
    }

    public List<ImageAsset> getImageAssets() throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, MalformedURLException {

        String apiGetImageAssetsUrl = baseUrl+IMAGE_ASSETS_PATH;

        String oauthHeader = Utils.createOAuthHeader(CONSUMER_KEY, CONSUMER_SECRET, userAccessData, apiGetImageAssetsUrl);

        Client client = ClientBuilder.newClient();

        Response response = client.target(apiGetImageAssetsUrl)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", oauthHeader)
                .get();

        JSONArray responseJsonArray = new JSONArray(response.readEntity(String.class));

        return Utils.createImageAssetListFromJSONArray(responseJsonArray);
    }

    public List<ImageAsset> getImageAssets(final int limit, final int offset) throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, MalformedURLException {

        StringBuilder stringBuilder = new StringBuilder(baseUrl+IMAGE_ASSETS_PATH);
        stringBuilder.append("&limit=");
        stringBuilder.append(limit);
        stringBuilder.append("&page=");
        stringBuilder.append(offset);

        String apiGetImageAssetsUrl = stringBuilder.toString();

        String oauthHeader = Utils.createOAuthHeader(CONSUMER_KEY, CONSUMER_SECRET, userAccessData, apiGetImageAssetsUrl);

        Client client = ClientBuilder.newClient();

        Response response = client.target(apiGetImageAssetsUrl)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", oauthHeader)
                .get();

        JSONArray responseJsonArray = new JSONArray(response.readEntity(String.class));

        return Utils.createImageAssetListFromJSONArray(responseJsonArray);
    }

    public int getImageAssetCount() throws OAuthMessageSignerException, OAuthExpectationFailedException, OAuthCommunicationException, MalformedURLException {

        String apiGetImageAssetsUrl = baseUrl+IMAGE_ASSETS_PATH;

        String oauthHeader = Utils.createOAuthHeader(CONSUMER_KEY, CONSUMER_SECRET, userAccessData, apiGetImageAssetsUrl);

        Client client = ClientBuilder.newClient();

        Response response = client.target(apiGetImageAssetsUrl)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", oauthHeader)
                .get();

        JSONArray responseJsonArray = new JSONArray(response.readEntity(String.class));

        return responseJsonArray.length();
    }

}
