package ru.adonixis.amazons3example;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ObjectListActivity extends AppCompatActivity {

    protected static final String COGNITO_POOL_ID ="YOUR ID";
    public static final String BUCKET_NAME = "Bucket Name"; //"omnapibucket";
    TransferUtility transferUtility;
    private AmazonS3Client s3Client;
    ListView listView;
    ArrayAdapter<String> adapter;
    String bucketName = "";
    private ArrayList<String> listItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_list);
        initUi();
    }

    private void initUi() {
        listItem = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listItem);
        listView.setAdapter(adapter);
        bucketName = getIntent().getStringExtra("BUCKET_NAME");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                String value = adapter.getItem(position);
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();


               new GetVideoUrl().execute(value);
            }
        });

        createTransferUtility();
    }

    private void createTransferUtility() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                COGNITO_POOL_ID,
                Regions.US_EAST_1
        );
        s3Client = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3Client, getApplicationContext());
        new AsyncTaskRunner().execute();
    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            listItem = getObjectList(s3Client, bucketName);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            adapter = new ArrayAdapter<String>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, listItem);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    private ArrayList<String> getObjectList(AmazonS3Client s3Client, String bucket_name) {
        ListObjectsV2Result result = s3Client.listObjectsV2(bucket_name);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        for (S3ObjectSummary os : objects) {
            System.out.println("* " + os.getKey());
            listItem.add(os.getKey());
        }
        return listItem;
    }


    private class GetVideoUrl extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            String key= params[0];
            return getVideoUrl(key);
        }

        @Override
        protected void onPostExecute(String result) {
            startActivity(new Intent(getApplicationContext(), VideoViewActivity.class)
                    .putExtra("URL", result)
                    .putExtra("BUCKET_NAME",bucketName));


        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    private String getVideoUrl(String objectName) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
        URL objectURL = s3Client.generatePresignedUrl(request);
        return  objectURL.toString();
    }


}
