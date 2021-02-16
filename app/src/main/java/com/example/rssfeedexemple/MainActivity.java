package com.example.rssfeedexemple;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


///user permission to internet in manifest file

public class MainActivity extends AppCompatActivity {
    ListView lvRss;
    ArrayList<String> titles;
    ArrayList<String> links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvRss=(ListView) findViewById(R.id.lvRss);
        titles=new ArrayList<String>();
        links=new ArrayList<String>();


        lvRss.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Uri uri= Uri.parse(links.get(position));///set link fist connection

                Intent intent=new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        new ProcessInBackground().execute();


    }
    public InputStream getInputStream(URL url)
    {
        try
        {
            return url.openConnection().getInputStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }
    public class  ProcessInBackground extends AsyncTask<Integer, Void, Exception>
    {
        ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
        Exception exception=null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Busy loading rss feed..please wait...");
            progressDialog.show();

        }

        @Override
        protected Exception doInBackground(Integer... params) {

            try {
                URL url=new URL("https://www.digisport.ro/rss");
                XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp=factory.newPullParser();///new instance of xmlpullparser , help us to extract data

                xpp.setInput(getInputStream(url),"UTF_8");//encoding of the document, at the top

                boolean insideItem=false;//if data is inside item tag

                int eventType=xpp.getEventType();//return tipe of start event  tag, tell us where we are

                while (eventType!=XmlPullParser.END_DOCUMENT)///when we reach end this stop
                {
                    if(eventType==XmlPullParser.START_TAG)
                    {
                        if(xpp.getName().equalsIgnoreCase("item"))//searh for what tag ypu want
                        {
                            insideItem=true;
                        }
                        else if(xpp.getName().equalsIgnoreCase("title"))
                        {
                            if(insideItem)
                            {
                                titles.add(xpp.nextText());
                            }
                        }
                        else if(xpp.getName().equalsIgnoreCase("link"))
                        {
                            if(insideItem)
                            {
                                links.add(xpp.nextText());
                            }
                        }
                    }
                    else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item"))
                    {
                        insideItem=false;
                    }
                    eventType=xpp.next();///increment and go to the next tag
                }
            }
            catch (MalformedURLException e)//sth wrong with url
            {
                exception=e;
            }
            catch (XmlPullParserException e)//when we try to extract data
            {
                exception=e;
            }
            catch (IOException e)//prob with reading
            {
                exception=e;
            }


            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);

            ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, titles);
            lvRss.setAdapter(adapter);
            progressDialog.dismiss();
        }
    }
}