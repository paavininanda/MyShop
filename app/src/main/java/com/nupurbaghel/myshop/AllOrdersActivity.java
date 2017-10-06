package com.nupurbaghel.myshop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static com.nupurbaghel.myshop.HomeActivity.map;


public class AllOrdersActivity extends AppCompatActivity {
    LinearLayout linearLayout;
    private Firebase mref;
    FirebaseUser currentFirebaseUser;
    String userId;
    int totalOrders;
    static Map< String ,Map<String,String>> ordersDB;
    String[] prodIds,freqs;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.all_orders,menu);

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
                startActivity(new Intent(AllOrdersActivity.this,ViewCartActivity.class));
                return true;
            case R.id.checkOut:
                Log.i("Clicked","Check Out");
                startActivity(new Intent(AllOrdersActivity.this,CheckOutActivity.class));
                return true;
            case R.id.home:
                startActivity(new Intent(this,HomeActivity.class));
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            default:return false;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_orders);

        linearLayout=(LinearLayout) findViewById(R.id.linearLayoutOrders);
        linearLayout.removeAllViews();
        mref= new Firebase(getString(R.string.firebaseUrl)+"/orders");
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        userId=currentFirebaseUser.getUid();
        ordersDB=new HashMap<>();
        fetchOrders();
        displayOrders();
        setupToolbar();
    }

    public void fetchOrders(){
        totalOrders=0;
        String orderId;
        Map<String, Map<String, String>> mapChild=map.get("orders");
        Iterator<Map.Entry<String, Map<String, String>>> iterator = mapChild.entrySet().iterator();
        while (iterator.hasNext()) {
            orderId=iterator.next().getKey();
            Log.i("OrderId",orderId);
            String userIdInDB=mapChild.get(orderId).get("userId");
            if(userIdInDB.equals(userId)){
                Map<String,String> temp=new HashMap<>();
                Log.i("dateTime",mapChild.get(orderId).get("dateTime"));
                temp.put("dateTime",mapChild.get(orderId).get("dateTime"));
                temp.put("netPrice",mapChild.get(orderId).get("netPrice"));
                temp.put("prodFreq",mapChild.get(orderId).get("prodFreq"));
                temp.put("prodNos",mapChild.get(orderId).get("prodNos"));
                temp.put("status",mapChild.get(orderId).get("status"));
                temp.put("email",mapChild.get(orderId).get("email"));
                temp.put("name",mapChild.get(orderId).get("name"));
                temp.put("address",mapChild.get(orderId).get("address"));
                ordersDB.put(orderId,temp);
                totalOrders++;
            }
        }
    }


    public void displayOrders(){
        if(totalOrders==0){
            TextView tv1=new TextView(getApplicationContext());
            tv1.setText("No orders yet!");
            tv1.setTextColor(Color.BLACK);
            tv1.setTextSize(30);
            linearLayout.addView(tv1);
        }
        else{
            for(final Map.Entry orders: ordersDB.entrySet()) {
                LinearLayout layout2 = new LinearLayout(getApplicationContext());
                layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                layout2.setOrientation(LinearLayout.VERTICAL);


                TextView tv1 = new TextView(getApplicationContext());
                TextView tv2 = new TextView(getApplicationContext());
                TextView tv3 = new TextView(getApplicationContext());
                TextView tv6 = new TextView(getApplicationContext());
                final String status=ordersDB.get(orders.getKey()).get("status");

                tv1.setText("Order Id : " + orders.getKey());
                tv1.setTextSize(20);
                tv1.setTextColor(Color.BLACK);
                tv2.setTextColor(Color.BLACK);
                tv3.setTextColor(Color.BLACK);
                tv6.setTextColor(Color.BLACK);


                tv2.setText("Date and Time of order : " + ordersDB.get(orders.getKey()).get("dateTime"));
                tv3.setText("Net total : Rs." + ordersDB.get(orders.getKey()).get("netPrice"));
                tv6.setText("Status : "+ status);
                //Log.i("Status",status);
                layout2.addView(tv1);

                prodIds=ordersDB.get(orders.getKey()).get("prodNos").split(",");
                freqs=ordersDB.get(orders.getKey()).get("prodFreq").split(",");

                Button cancelOrder=new Button(this);
                if(status.equals("cancelled")){
                    RelativeLayout.LayoutParams rel_bottone = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cancelOrder.setLayoutParams(rel_bottone);
                    cancelOrder.setText("Delete this order from list");
                }
                else{
                    RelativeLayout.LayoutParams rel_bottone = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cancelOrder.setLayoutParams(rel_bottone);
                    cancelOrder.setText("Cancel this order");
                }

                cancelOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(AllOrdersActivity.this);
                        if (status.equals("cancelled")) {
                            builder1.setMessage("This will permanently delete your order! Are you sure?");
                        }
                        else{
                            builder1.setMessage("This will cancel your order! Are you sure?");
                        }
                        builder1.setCancelable(true);
                        builder1.setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (status.equals("cancelled")) {
                                            mref.child(orders.getKey().toString()).removeValue();
                                        }
                                        else{
                                            String email = ordersDB.get(orders.getKey().toString()).get("email");
                                            Log.i("Email",email);
                                            GenerateMail generateMail= new GenerateMail();
                                            new GMailSender().execute(email,"Order Cancelled",generateMail.generateOrderCancel(AllOrdersActivity.this,currentFirebaseUser.getUid(),orders.getKey().toString()),getString(R.string.OwnerEmailId),getString(R.string.OwnerPass));

                                            mref.child(orders.getKey().toString()).child("status").setValue("cancelled");
                                        }
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                    }
                                });
                        builder1.setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                });


                String w,q;
                Log.i("ProductsNo", Integer.toString(prodIds.length));
                for(int i = 0; i < prodIds.length; i++){
                    TextView tv4 = new TextView(getApplicationContext());
                    TextView tv5 = new TextView(getApplicationContext());
                    LinearLayout layoutx = new LinearLayout(getApplicationContext());
                    layoutx.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    layoutx.setOrientation(LinearLayout.HORIZONTAL);

                    tv4.setText("Product : " + map.get("product").get(prodIds[i]).get("name"));
                    Log.i("ProductsNo", map.get("product").get(prodIds[i]).get("name"));
                    tv5.setText("Qty : " + freqs[i]);
                    tv5.setPadding(20,0,0,0);

                    layoutx.addView(tv4);
                    layoutx.addView(tv5);
                    layout2.addView(layoutx);
                    tv4.setTextColor(Color.BLACK);
                    tv5.setTextColor(Color.BLACK);
                }
                Log.i("NoOfChildren",Integer.toString(layout2.getChildCount()));
                //layout2.addView(ll);
                layout2.addView(tv2);
                layout2.addView(tv3);
                layout2.addView(tv6);
                layout2.setPadding(0,40,0,40);
                layout2.addView(cancelOrder);
                linearLayout.addView(layout2);
            }
        }
    }

    void setupToolbar(){
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
