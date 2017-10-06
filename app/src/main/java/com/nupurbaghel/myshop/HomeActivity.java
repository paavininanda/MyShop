package com.nupurbaghel.myshop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    int count = 0;
    private Firebase mRef;
    GridLayout gridLayout;
    static Map<String, Map<String, Map<String, String>>> map;
    static Map<String,String> mycart= new HashMap();
    private ProgressDialog progressDialog;
    TextView tv4;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.home_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.logout:
                LogoutAlert alertt=new LogoutAlert();
                alertt.lA(this);
                return true;
            case R.id.mycart:
                Log.i("Clicked","View Cart");
                startActivity(new Intent(HomeActivity.this,ViewCartActivity.class));
                return true;
            case R.id.checkOut:
                Log.i("Clicked","Check Out");
                startActivity(new Intent(HomeActivity.this,CheckOutActivity.class));
                return true;
            case R.id.allOrders:
                Log.i("Clicked","All orders");
                startActivity(new Intent(HomeActivity.this,AllOrdersActivity.class));
                return true;
            default:return false;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //initialise cart
        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        ManageCart manageCart= new ManageCart();
        mycart= manageCart.LoadCart(this);

        gridLayout=(GridLayout) findViewById(R.id.gridLayout);
        gridLayout.removeAllViews();
        mRef=new Firebase(getString(R.string.firebaseUrl));

        final ValueEventListener valueEventListener = mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("onDataChange","1");
                findCount(dataSnapshot);
                displayInGridView();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.i("onCancelled","2");
            }
        });

        setupToolbar();
    }

    public void callScrollActivity(View view){
        startActivity(new Intent(this,ScrollActivity.class));
    }

    public void findCount(DataSnapshot dataSnapshot){
        count=0;
        gridLayout.removeAllViews();
        map = dataSnapshot.getValue(Map.class);
        Iterator<Map.Entry<String, Map<String, Map<String, String>>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Log.i("Level 0",Integer.toString(count));
            Map.Entry<String, Map<String, Map<String, String>>> pair = iterator.next();
            Log.i("Printing key",pair.getKey());
            if (pair.getKey() == "category") {

                Iterator<Map.Entry<String, Map<String, String>>> it2 = pair.getValue().entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry<String, Map<String, String>> pair2 = it2.next();
                    if (!pair2.getKey().contains("-")) {
                        count++;
                    }
                }
            }
        }
        Log.i("Printing Count",Integer.toString(count));
    }

    public void displayInGridView(){
        FirebaseStorage storage= FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef;
        gridLayout.setColumnCount(2);

        float paading = getResources().getDimension(R.dimen.padding);
        if(count%2==0) {
            gridLayout.setRowCount(count / 2);
        }
        else{
            gridLayout.setRowCount((count / 2) + 1) ;
        }

        for(int i=1;i<=count;i++) {
            LinearLayout parent = new LinearLayout(getApplicationContext());
            TextView tv1 = new TextView(getApplicationContext());
            tv1.setGravity(Gravity.CENTER);

            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            parent.setOrientation(LinearLayout.VERTICAL);
            Log.i("image url",map.get("category").get(Integer.toString(i)).toString());
            String imageURL=map.get("category").get(Integer.toString(i)).get("img");

            imagesRef = storageRef.child(imageURL);
            Log.i("Printing image url",imageURL);
            tv1.setText(map.get("category").get(Integer.toString(i)).get("title"));
            ImageView iv = new ImageView(getApplicationContext());

            float width = getResources().getDimension(R.dimen.chart_width);
            iv.setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) width));
            iv.setPadding((int)paading,(int)paading,(int)paading,(int)paading);

            Glide.with(getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(imagesRef)
                    .into(iv);

            Log.i("trying to access", "yay");

            tv1.setTextColor(Color.BLACK);

            parent.addView(iv);
            parent.addView(tv1);


            gridLayout.setPadding((int)paading,(int)paading,(int)paading,(int)paading);
            gridLayout.addView(parent);

            final int finalI = i;
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("AppDebug","button clicked");
                    Intent x= new Intent(HomeActivity.this,ScrollActivity.class);
                    Log.i("ButtonHome", String.valueOf(finalI));
                    x.putExtra("categoryNo",finalI);
                    startActivity(x);

                }
            });
        }
        progressDialog.cancel();
    }

    void setupToolbar(){
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
