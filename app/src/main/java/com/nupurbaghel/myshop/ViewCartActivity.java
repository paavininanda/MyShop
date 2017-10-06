package com.nupurbaghel.myshop;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import static android.R.attr.layout_centerVertical;
import static com.nupurbaghel.myshop.CheckOutActivity.details;
import static com.nupurbaghel.myshop.HomeActivity.map;
import static com.nupurbaghel.myshop.HomeActivity.mycart;

public class ViewCartActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    static ManageCart manageCart;
    String netprice;
    float TotalCost;
    Button fab;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.cart_menu,menu);

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
            case  R.id.checkOut:
                startActivity(new Intent(this,CheckOutActivity.class));
                return true;
            case  R.id.home:
                startActivity(new Intent(this,HomeActivity.class));
                return true;
            case R.id.allOrders:
                Log.i("Clicked","All orders");
                startActivity(new Intent(ViewCartActivity.this,AllOrdersActivity.class));
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:return false;
        }

    }

    public void Logout(){
        FirebaseAuth.getInstance().signOut();
        manageCart.clearCart(ViewCartActivity.this);
        details.clear();
        String fname = "def_details.txt";
        File file = new File(getDir("data", MODE_PRIVATE), fname);
        try {
            ObjectOutputStream outputStream = null;
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(details);
            outputStream.flush();
            outputStream.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cart);
        linearLayout= (LinearLayout)findViewById(R.id.linearLayout);
        TotalCost=0;

        fab=  (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Click action
                Intent intent = new Intent(ViewCartActivity.this, CheckOutActivity.class);
                startActivity(intent);
            }
        });

        if(getCart()){
            displayInLayout();
            displayTotal();
        }
        else{
            displayTotal();
        }

        setupToolbar();
        manageCart = new ManageCart();
    }
    public boolean getCart(){

        if (mycart.isEmpty()) {
            Log.i("Cart", "empty");
            return false;
        } else {
            Log.i("Loaded cart ", mycart.toString());
            return true;
        }
    }

    public void displayInLayout(){
        FirebaseStorage storage= FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef;

        for(Map.Entry cartItem: mycart.entrySet()) {

            LinearLayout parent = new LinearLayout(getApplicationContext());

            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            parent.setOrientation(LinearLayout.HORIZONTAL);
            String prodId= (String) cartItem.getKey();
            String imageURL= map.get("product").get(prodId).get("img");

            imagesRef = storageRef.child(imageURL);
            Log.i("Printing image url",imageURL);
            //tv1.setText(map.get("category").get(Integer.toString(i)).get("title"));

            ImageView iv = new ImageView(getApplicationContext());
            LinearLayout layout2 = new LinearLayout(getApplicationContext());
            LinearLayout layout3 = new LinearLayout(getApplicationContext());

            layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout2.setOrientation(LinearLayout.VERTICAL);

            layout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout3.setOrientation(LinearLayout.HORIZONTAL);

            float width = getResources().getDimension(R.dimen.chart_width);
            iv.setLayoutParams(new ViewGroup.LayoutParams((int) width, (int) width));

            Glide.with(this /* context */)
                    .using(new FirebaseImageLoader())
                    .load(imagesRef)
                    .into(iv);
//children of layout2 LinearLayout
            Log.i("trying to access", "yay");
            TextView tv1 = new TextView(getApplicationContext());
            TextView tv2 = new TextView(getApplicationContext());
            TextView tv3 = new TextView(getApplicationContext());
            TextView tv4 = new TextView(getApplicationContext());
            TextView tv5 = new TextView(getApplicationContext());

            String title= map.get("product").get(prodId).get("name");
            tv1.setText(title);
            String company = "By: "+map.get("product").get(prodId).get("company");
            tv2.setText(company);
            String quantity = (String) cartItem.getValue();
            tv3.setText("Quantity: "+quantity);

            String price= map.get("product").get(prodId).get("price");
            String discount= map.get("product").get(prodId).get("discount");
            String total = findCost(price,discount,quantity);
            String cost = "Per item = Rs "+ price +" -" +discount+"%  = Rs"+ total;
            tv4.setText(cost);
            netprice = "Net Price: Rs " + Float.toString(Float.parseFloat(total) * Float.parseFloat(quantity));
            tv5.setText(netprice);

            tv1.setTextColor(Color.BLACK);
            tv1.setTextSize(25);
            tv2.setTextColor(Color.BLACK);
            tv3.setTextColor(Color.BLACK);
            tv4.setTextColor(Color.BLACK);
            tv5.setTextColor(Color.BLACK);

            Button btn1 = new Button(getApplicationContext());
            Button btn2 = new Button(getApplicationContext());
            LinearLayout.LayoutParams layoutParams =new LinearLayout.LayoutParams((int) width,(int) width/3);
            layoutParams.gravity= Gravity.CENTER;
            btn1.setLayoutParams(layoutParams);
            btn2.setLayoutParams(layoutParams);

            btn1.setText("Add Qty (+1)");
            btn2.setText("Remove Qty (-1)");
            btn1.setOnClickListener(AddToCart(btn1, (String)cartItem.getKey(),quantity));
            btn2.setOnClickListener(RemoveFromCart(btn2,(String)cartItem.getKey(),quantity));
            layout2.addView(tv1);
            layout2.addView(tv2);
            layout2.addView(tv3);
            layout2.addView(tv4);
            layout2.addView(tv5);
            layout2.setGravity(layout_centerVertical);
            layout2.setPadding(50,50,50,50);
            layout3.addView(btn1);
            layout3.addView(btn2);
            parent.addView(iv);
            parent.addView(layout2);

            linearLayout.addView(parent);
            linearLayout.addView(layout3);
        }
    }

    public void displayTotal(){
        TextView tv1= new TextView(getApplicationContext());
        if(TotalCost!=0) {
            tv1.setText("  Total Price: Rs " + Float.toString(TotalCost)+ "/-");
            fab.setVisibility(View.VISIBLE);
        }
        else{
            tv1.setText("Cart is empty!!");
            fab.setVisibility(View.INVISIBLE);
        }
        LinearLayout.LayoutParams layoutParams =new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0,50,0,0);
        tv1.setLayoutParams(layoutParams);
        tv1.setTextSize(30);
        tv1.setTextColor(Color.BLACK);
        linearLayout.addView(tv1);
    }


    public String findCost(String price,String discount, String quantity){
        float Price = Float.parseFloat(price);
        float Discount = Float.parseFloat(discount);

        float cost = (float) (Price - Discount * 0.01 * Price);
        TotalCost = TotalCost + cost * (float)Integer.parseInt(quantity);
        return Float.toString(cost);
    }


    View.OnClickListener AddToCart(final Button btn, final String prodId,final String quantity)  {
        return new View.OnClickListener() {
            public void onClick(View v) {

                manageCart.AddToCart(ViewCartActivity.this,prodId,quantity);
                linearLayout.removeAllViews();
                TotalCost=0;
                displayInLayout();
                displayTotal();
                Toast.makeText(ViewCartActivity.this, "Increased Qty in Cart", Toast.LENGTH_SHORT).show();
            }
        };
    }
    View.OnClickListener RemoveFromCart(final Button btn, final String prodId,final String quantity)  {
        return new View.OnClickListener() {
            public void onClick(View v) {

                manageCart.ReduceQty(ViewCartActivity.this,prodId,quantity);
                linearLayout.removeAllViews();
                TotalCost=0;
                displayInLayout();
                displayTotal();
                Toast.makeText(ViewCartActivity.this, "Reduced Qty in Cart", Toast.LENGTH_SHORT).show();
            }
        };
    }

    void setupToolbar(){
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}