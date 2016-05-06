package com.example.nurerkizilkaya.newsrss;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.loopj.android.http.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;

import cz.msebera.android.httpclient.Header;
public class MainActivity extends Activity {
    ListView listView1;
    MyCustomAdapter adapter;

    ArrayList<String> xmlList=new ArrayList<String>();
    ArrayList<String> xmlLink=new ArrayList<String>();
    ArrayList<String> xmlDate=new ArrayList<String>();
    ArrayList<String> xmlPng=new ArrayList<String>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView1 = (ListView)findViewById(R.id.newslistView);
        new arkaPlanIsleri().execute();

        listView1 = (ListView)findViewById(R.id.newslistView);
        listView1.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Uri link = Uri.parse(xmlLink.get(position));
                final Intent openBrowser = new Intent(Intent.ACTION_VIEW, link);
                startActivity(openBrowser);
            }
        });

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://www.milliyet.com.tr/rss/rssNew/magazinRss.xml", new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });

    }
    public class arkaPlanIsleri extends  AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            adapter= new MyCustomAdapter(MainActivity.this, R.layout.list, xmlList);
            listView1.setAdapter(adapter);
            dialog.dismiss();
        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            dialog.setMessage("Yükleniyor...");
            dialog.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            xmlList=getListFromXml("http://www.milliyet.com.tr/rss/rssNew/magazinRss.xml");
            xmlLink=getLinkFromXml("http://www.milliyet.com.tr/rss/rssNew/magazinRss.xml");
            xmlDate=getDateFromXml("http://www.milliyet.com.tr/rss/rssNew/magazinRss.xml");
            xmlPng=getPngFromXml("http://www.milliyet.com.tr/rss/rssNew/magazinRss.xml");
            return null;
        }

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView image;

        public DownloadImageTask(ImageView image) {
            this.image = image;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
        }
    }

    public class MyCustomAdapter extends ArrayAdapter<String> {

        public MyCustomAdapter(Context context, int textViewResourceId,ArrayList<String> xmlList) {
            super(context, textViewResourceId, xmlList);
            // TODO Auto-generated constructor stub
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            //return super.getView(position, convertView, parent);
            View row = convertView;
            if(row==null){
                LayoutInflater inflater=getLayoutInflater();
                row=inflater.inflate(R.layout.list, parent, false);
            }

            TextView label=(TextView)row.findViewById(R.id.text1);
            label.setText(xmlList.get(position));

            TextView dateText=(TextView)row.findViewById(R.id.dateId);
            dateText.setText(xmlDate.get(position));


            int baslangic_numarasi,gidecegi_karakter_sayisi;
            String Main_String =xmlPng.get(position);
            String word = "src=";
            String karakter="/>";
            baslangic_numarasi = Main_String.indexOf(word);
            gidecegi_karakter_sayisi= Main_String.indexOf(karakter, baslangic_numarasi + 1);
            String SoftPng=Main_String.substring(baslangic_numarasi + 5, gidecegi_karakter_sayisi -2);

            String urldisplay = SoftPng;
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            ImageView image =(ImageView)row.findViewById(R.id.list);

            image.setImageBitmap(mIcon11);


           // new DownloadImageTask((ImageView) findViewById(R.id.list)).execute(SoftPng);

            return row;
        }
    }
    // TODO Auto-generated method stub
    //super.onListItemClick(l, v, position, id);
    public ArrayList<String> getPngFromXml(String strng) {

        ArrayList<String> list=new ArrayList<String>();

        try {

            URL url=new URL(strng);
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder=dFactory.newDocumentBuilder();

            Document document=dBuilder.parse(new InputSource(url.openStream()));
            document.getDocumentElement().normalize();

            NodeList nodeListCountry=document.getElementsByTagName("item");
            for (int i = 0; i < nodeListCountry.getLength(); i++) {
                Node node=nodeListCountry.item(i);
                Element elementMain=(Element) node;
                NodeList nodeListText=elementMain.getElementsByTagName("description");
                Element elementText=(Element) nodeListText.item(0);
                list.add(elementText.getChildNodes().item(0).getNodeValue());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
    public ArrayList<String> getListFromXml(String strng)  {

        ArrayList<String> list=new ArrayList<String>();

        try {

            URL url=new URL(strng);
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder=dFactory.newDocumentBuilder();

            Document document=dBuilder.parse(new InputSource(url.openStream()));
            document.getDocumentElement().normalize();

            NodeList nodeListCountry=document.getElementsByTagName("item");
            for (int i = 0; i < nodeListCountry.getLength(); i++) {
                Node node=nodeListCountry.item(i);
                Element elementMain=(Element) node;

                NodeList nodeListText=elementMain.getElementsByTagName("title");
                Element elementText=(Element) nodeListText.item(0);

                list.add(elementText.getChildNodes().item(0).getNodeValue());


            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return list;
    }
    public ArrayList<String> getLinkFromXml(String strng)  {

        ArrayList<String> list=new ArrayList<String>();

        try {

            URL url=new URL(strng);
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder=dFactory.newDocumentBuilder();

            Document document=dBuilder.parse(new InputSource(url.openStream()));
            document.getDocumentElement().normalize();

            NodeList nodeListCountry=document.getElementsByTagName("item");
            for (int i = 0; i < nodeListCountry.getLength(); i++) {
                Node node=nodeListCountry.item(i);
                Element elementMain=(Element) node;
                NodeList nodeListText=elementMain.getElementsByTagName("link");
                Element elementText=(Element) nodeListText.item(0);
                list.add(elementText.getChildNodes().item(0).getNodeValue());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    public ArrayList<String> getDateFromXml(String strng) {

        ArrayList<String> list=new ArrayList<String>();

        try {

            URL url=new URL(strng);
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder=dFactory.newDocumentBuilder();

            Document document=dBuilder.parse(new InputSource(url.openStream()));
            document.getDocumentElement().normalize();

            NodeList nodeListCountry=document.getElementsByTagName("item");
            for (int i = 0; i < nodeListCountry.getLength(); i++) {
                Node node=nodeListCountry.item(i);
                Element elementMain=(Element) node;
                NodeList nodeListText=elementMain.getElementsByTagName("pubDate");
                Element elementText=(Element) nodeListText.item(0);
                list.add(elementText.getChildNodes().item(0).getNodeValue());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}


            /*int baslangic_numarasi,gidecegi_karakter_sayisi;


            String Main_String =xmlPng.get(position);

            String word = "src=";
            String karakter="/>";

            baslangic_numarasi = Main_String.indexOf(word);
            gidecegi_karakter_sayisi= Main_String.indexOf(karakter, baslangic_numarasi + 1);
           String SoftPng=Main_String.substring(baslangic_numarasi + 5, gidecegi_karakter_sayisi -2);
            try{
                URL url = new URL(SoftPng);
                URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),url.getQuery(), url.getRef());
                url = uri.toURL();

                Bitmap bitmap=null;
                URL imageUrl = new URL(url);
                URI uri = new URI(imageUrl.getProtocol(), imageUrl.getUserInfo(), imageUrl.getHost(), imageUrl.getPort(), imageUrl.getPath(), imageUrl.getQuery(), imageUrl.getRef());
                imageUrl = uri.toURL();
            }
            catch(IOException e){
                e.printStackTrace();
            }*/