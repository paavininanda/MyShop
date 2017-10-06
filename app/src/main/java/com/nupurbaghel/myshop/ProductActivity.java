package com.nupurbaghel.myshop;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import static android.R.attr.layout_centerVertical;
import static com.nupurbaghel.myshop.CheckOutActivity.details;
import static com.nupurbaghel.myshop.HomeActivity.map;
import static com.nupurbaghel.myshop.ViewCartActivity.manageCart;

public class ProductActivity extends AppCompatActivity {

    LinearLayout linearLayout2;
    int categoryNo,subCategoryNo;
    String imageURL;
    private Firebase mRef;
    static String[] prodIds;
    int count;
    Spinner criteria,filter;
    private DrawerLayout mDrawerLayout;
    android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    Toolbar toolbar;
    ManageCart managecart;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

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
                startActivity(new Intent(ProductActivity.this,ViewCartActivity.class));
                return true;
            case R.id.checkOut:
                Log.i("Clicked","Check Out");
                startActivity(new Intent(ProductActivity.this,CheckOutActivity.class));
                return true;
            case R.id.allOrders:
                Log.i("Clicked","All orders");
                startActivity(new Intent(ProductActivity.this,AllOrdersActivity.class));
                return true;
            case R.id.home:
                startActivity(new Intent(this,HomeActivity.class));
                return true;
            default:return false;
        }

    }
    public void Logout(){
        FirebaseAuth.getInstance().signOut();
        manageCart.clearCart(ProductActivity.this);
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
        setContentView(R.layout.activity_product);

        linearLayout2= (LinearLayout) findViewById(R.id.linearLayoutProduct);
        if(linearLayout2==null){
            Log.i("Linear layout","Not found");
        }

        subCategoryNo = getIntent().getIntExtra("subCategoryNo",1);

        categoryNo = getIntent().getIntExtra("categoryNo",1);

        mRef=new Firebase(getString(R.string.firebaseUrl));
        final ValueEventListener valueEventListener2 = mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                linearLayout2.removeAllViews();
                findCount();
                displayInLayout(prodIds);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        FirebaseStorage storage= FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("categories/electric2.jpg");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        setupToolbar();
        setupDrawerToggle();
        init_navdrawer();
        managecart = new ManageCart();
    }

    void setupToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void setupDrawerToggle(){
        mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.app_name, R.string.app_name);
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        mDrawerToggle.syncState();
    }

    public void init_navdrawer(){

        criteria= (Spinner)findViewById(R.id.criteria);
        filter= (Spinner)findViewById(R.id.filter);
        findCount();

        final Button subcriteria =(Button)findViewById(R.id.subcriteria);
        subcriteria.setVisibility(View.INVISIBLE);
        filter.setVisibility(View.INVISIBLE);

        //set adapters for all spinners
        final FilterClass filterClass= new FilterClass();
        criteria.setAdapter(filterClass.CreateAdapter(this,"criteria"));

        criteria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected= (String) criteria.getItemAtPosition(position);
                if(selected.equals("Chose Color")||selected.equals("Chose Make")||selected.equals("Chose Material")){

                    subcriteria.setVisibility(View.VISIBLE);
                    filter.setVisibility(View.VISIBLE);
                    create_filter(selected);
                    filter.performClick();
                }
                else{
                    if(selected.equals("Highest Discount First")){
                        displayInLayout(filterClass.findProdId("discount",selected));
                    }
                    else if(selected.equals("Highest Price First")){
                        displayInLayout(filterClass.findProdId("price",selected));
                    }
                    else if(selected.equals("Lowest Discount First")){
                        displayInLayout(filterClass.findProdId("discount",selected));
                    }
                    else{
                        displayInLayout(filterClass.findProdId("price",selected));
                    }
                    mDrawerLayout.closeDrawer(Gravity.LEFT,true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void create_filter(final String selected){
        final FilterClass filterClass= new FilterClass();
        filter.setAdapter(filterClass.CreateAdapter(this,selected));
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(selected.equals("Chose Color")){
                    displayInLayout(filterClass.findProdId("color", (String) filter.getItemAtPosition(position)));
                }
                else if(selected.equals("Chose Material")){
                    displayInLayout(filterClass.findProdId("quality", (String) filter.getItemAtPosition(position)));
                }
                else{
                    displayInLayout(filterClass.findProdId("company", (String) filter.getItemAtPosition(position)));
                }
                if(!filter.getItemAtPosition(position).toString().equals("None"))
                mDrawerLayout.closeDrawer(Gravity.LEFT,true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int getIndex(Spinner spinner, String myString){
        int index = 0;
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

    public void findCount(){
        count=Integer.parseInt( map.get("category").get(categoryNo+"-"+subCategoryNo).get("total"));
        Log.i("TotalCountoOfProducts",Integer.toString(count));
        prodIds=map.get("category").get(categoryNo+"-"+subCategoryNo).get("products").split(",");
        for(String w:prodIds){
            Log.i("ProductIds",w);
        }
    }

    public void displayInLayout(String[] prodIds){
        FirebaseStorage storage= FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef;
        linearLayout2.removeAllViews();
        for(String w:prodIds) {


            LinearLayout parent = new LinearLayout(getApplicationContext());
            parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            parent.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout layout2 = new LinearLayout(getApplicationContext());
            layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout2.setOrientation(LinearLayout.VERTICAL);

            imageURL= map.get("product").get(w).get("img");
            imagesRef = storageRef.child(imageURL);
            Log.i("Printing image url",imageURL);
            ImageView iv = new ImageView(getApplicationContext());
            float width = getResources().getDimension(R.dimen.chart_width);
            LinearLayout.LayoutParams layoutParams= new LinearLayout.LayoutParams((int) width, (int) width);
            layoutParams.gravity= Gravity.CENTER;
            iv.setLayoutParams(layoutParams);
            Glide.with(getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(imagesRef)
                    .into(iv);

            TextView name = new TextView(getApplicationContext());
            TextView company = new TextView(getApplicationContext());
            TextView price = new TextView(getApplicationContext());
            TextView discount = new TextView(getApplicationContext());
            TextView quantity = new TextView(getApplicationContext());
            quantity.setText("Quantity : ");
            Button btn = new Button(getApplicationContext());

            LinearLayout layout3 = new LinearLayout(getApplicationContext());
            layout3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout3.setOrientation(LinearLayout.HORIZONTAL);
            final TextView qty= new TextView(getApplicationContext());
            qty.setText("1");
            qty.setTextSize(20);
            Button add = new Button(getApplicationContext());
            add.setText("+");
            Button minus =new Button(getApplicationContext());
            minus.setText("-");
            add.setLayoutParams(new ViewGroup.LayoutParams((int) width/3,(int) width/3));
            minus.setLayoutParams(new ViewGroup.LayoutParams((int) width/3,(int) width/3));
            layout3.addView(quantity);
            layout3.addView(qty);
            layout3.addView(add);
            layout3.addView(minus);

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantity =1 + Integer.parseInt(qty.getText().toString());
                    qty.setText(Integer.toString(quantity));
                }
            });

            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantity = Integer.parseInt(qty.getText().toString()) - 1;
                    if(quantity == 0)
                        qty.setText("1");
                    else
                        qty.setText(Integer.toString(quantity));
                }
            });

            String name_= map.get("product").get(w).get("name");
            String company_= map.get("product").get(w).get("company");
            String price_= map.get("product").get(w).get("price");
            String discount_= map.get("product").get(w).get("discount");

            name.setText("Product : " +name_);
            company.setText("Company : " +company_);
            price.setText("Price (Rs.) : "+price_);
            discount.setText("Discount : "+discount_+"%");
            name.setTextColor(Color.BLACK);
            company.setTextColor(Color.BLACK);
            price.setTextColor(Color.BLACK);
            discount.setTextColor(Color.BLACK);
            quantity.setTextColor(Color.BLACK);
            qty.setTextColor(Color.BLACK);
            btn.setText("Add to cart");

            layout2.addView(name);
            layout2.addView(company);
            layout2.addView(price);
            layout2.addView(discount);
            layout2.addView(layout3);
            layout2.addView(btn);
            layout2.setGravity(layout_centerVertical);
            layout2.setPadding(50,50,50,50);
            parent.addView(iv);
            parent.addView(layout2);
            Log.i("Adding layout",w);

            linearLayout2.addView(parent);
            btn.setOnClickListener(AddToCart(btn, w, name_, qty));
        }
    }

    View.OnClickListener AddToCart(final Button btn, final String prodId,final String prodName,final TextView quantity)  {
        return new View.OnClickListener() {
            public void onClick(View v) {

                managecart.AddToCart(ProductActivity.this,prodId,quantity.getText().toString());
                Toast.makeText(ProductActivity.this, "Added "+quantity.getText().toString() +" "+prodName+" to Cart ", Toast.LENGTH_SHORT).show();
                Log.i("Added to cart" , prodName);
            }
        };
    }
}