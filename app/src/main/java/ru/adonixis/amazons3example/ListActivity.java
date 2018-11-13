package ru.adonixis.amazons3example;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    protected static final String COGNITO_POOL_ID = "us-east-1:b49d58b8-65ae-400b-9ab9-5c0c5685f709";//"us-east-1:8609474f-b806-4464-95a1-ed5f6bb0fe38"; //"us-east-1_G5nTErGjf";
    public static final String BUCKET_NAME = "test12321a"; //"omnapibucket";
    TransferUtility transferUtility;
    private AmazonS3Client s3Client;
    ListView listView;
    ArrayAdapter<String> adapter;
    private ArrayList<String> listItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initUi();
        createTransferUtility();
    }

    private void initUi() {
        listItem = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listItem);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // TODO Auto-generated method stub
                String value = adapter.getItem(position);
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),ObjectListActivity.class)
                .putExtra("BUCKET_NAME",value));

            }
        });
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

    private ArrayList<String> getBucketList(AmazonS3Client s3Client) {
        listItem = new ArrayList<>();
        System.out.println("Your Amazon S3 buckets are:");
        List<Bucket> buckets = s3Client.listBuckets();
        for (Bucket b : buckets) {
            System.out.println("* " + b.getName());
            listItem.add(b.getName());
        }
        return listItem;
    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            listItem = getBucketList(s3Client);
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


}
