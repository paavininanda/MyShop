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
import android.widget.Button;
import android.widget.EditText;


import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.nupurbaghel.myshop.HomeActivity.map;
import static com.nupurbaghel.myshop.HomeActivity.mycart;

public class CheckOutActivity extends AppCompatActivity {
    static HashMap<String,String> details=new HashMap<>();
    EditText name,address,phone,email;
    String name_,address_,phone_,email_;
    Button checko;
    float TotalCost;
    FirebaseUser currentFirebaseUser;
    private Firebase mref2;
    ManageCart manageCart;
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
                startActivity(new Intent(CheckOutActivity.this,ViewCartActivity.class));
                return true;
            case R.id.checkOut:
                Log.i("Clicked","Check Out");
                startActivity(new Intent(CheckOutActivity.this,CheckOutActivity.class));
                return true;
            case R.id.allOrders:
                Log.i("Clicked","All orders");
                startActivity(new Intent(CheckOutActivity.this,AllOrdersActivity.class));
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
        setContentView(R.layout.activity_check_out);

        if(mycart.isEmpty()==true){
            AlertDialog.Builder builder1 = new AlertDialog.Builder(CheckOutActivity.this);
            builder1.setMessage("Please select some items first!");
            builder1.setCancelable(true);

            builder1.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CheckOutActivity.this.finish();
                            //startActivity(new Intent(CheckOutActivity.this,HomeActivity.class));
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
        name=(EditText) findViewById(R.id.name);
        address=(EditText) findViewById(R.id.address);
        email=(EditText) findViewById(R.id.email);
        phone=(EditText) findViewById(R.id.phone);
        checko=(Button) findViewById(R.id.checko);

        if(!getDefault(name,address,email,phone))
            email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

        checko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CheckOutActivity.this);
                save_details(name,address,email,phone);
                builder1.setMessage("Do you really want to place the order?");
                builder1.setCancelable(true);

                builder1.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                GenerateMail generateMail= new GenerateMail();
                                String orderId = addToFirebase();
                                Log.i("Trying to generate mail","Now in Checkout");

                                new GMailSender().execute(details.get("email"),"New Order Placed",generateMail.generateOrder(CheckOutActivity.this,currentFirebaseUser.getUid(),orderId),getString(R.string.OwnerEmailId),getString(R.string.OwnerPass));
                                clearEverything();
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

        setupToolbar();
        manageCart = new ManageCart();
    }

    public void save_details(EditText name,EditText address,EditText email,EditText phone){

        String fname = "def_details.txt";
        File file = new File(getDir("data", MODE_PRIVATE), fname);

        details.put("name",name.getText().toString());
        details.put("address",address.getText().toString());
        details.put("email",email.getText().toString());
        details.put("phone",phone.getText().toString());

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

    }

    public boolean getDefault(EditText name,EditText address,EditText email,EditText phone){
        String fname = "def_details.txt";
        File file = new File(getDir("data", MODE_PRIVATE), fname);

        try {
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                details = (HashMap<String, String>) ois.readObject();
                if(details.isEmpty()) {
                    Log.i("Default details stored","empty");
                    return false;
                }
                else{
                    name.setText(details.get("name"));
                    address.setText(details.get("address"));
                    email.setText(details.get("email"));
                    phone.setText(details.get("phone"));


                    name.setTextColor(Color.BLACK);
                    address.setTextColor(Color.BLACK);
                    email.setTextColor(Color.BLACK);
                    phone.setTextColor(Color.BLACK);


                    Log.i("Default loaded", details.toString());
                    return true;
                }
            }
            else{
                ObjectOutputStream outputStream = null;
                outputStream = new ObjectOutputStream(new FileOutputStream(file));
                outputStream.writeObject(details);
                outputStream.flush();
                outputStream.close();

                return false;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }



    public String addToFirebase(){
        Log.i("Addtofire","called");
        mref2 = new Firebase(getString(R.string.firebaseUrl)+"/orders");
        name_=name.getText().toString();
        address_=address.getText().toString();
        email_=email.getText().toString();
        phone_=phone.getText().toString();

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        String prodNos="";
        String prodFreqs="";
        Map<String,String> mymap=new HashMap<>();
        for(Map.Entry cartItem: mycart.entrySet()) {
            Log.i("Keys Found",cartItem.getKey().toString());
            prodNos=prodNos + cartItem.getKey().toString() + "," ;
            prodFreqs=prodFreqs + cartItem.getValue().toString() + ",";

            String quantity = (String) cartItem.getValue();
            String prodId= (String) cartItem.getKey();
            String price= map.get("product").get(prodId).get("price");
            String discount= map.get("product").get(prodId).get("discount");
            String total = findCost(price,discount,quantity);
            String cost = "Per item = Rs "+ price +" -" +discount+"%  = Rs"+ total;

        }




        mymap.put("name",name_);
        mymap.put("address",address_);
        mymap.put("email",email_);
        mymap.put("phone",phone_);
        mymap.put("prodNos",prodNos);
        mymap.put("prodFreq",prodFreqs);
        mymap.put("userId",currentFirebaseUser.getUid());
        mymap.put("netPrice",Float.toString(TotalCost));
        mymap.put("dateTime",currentDateTimeString);
        mymap.put("status","Processing");


        Firebase orderRef = mref2.push();
        orderRef.setValue(mymap);
        String orderId = orderRef.getKey();

        Log.i("Addtofire","OrderId :"+orderId);
        return orderId;

    }

    void clearEverything(){
        mycart.clear();
        AlertDialog.Builder builder1 = new AlertDialog.Builder(CheckOutActivity.this);
        builder1.setMessage("Your order has been successfully placed");
        builder1.setCancelable(true);

        manageCart.updateCart(CheckOutActivity.this);
        builder1.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(CheckOutActivity.this,HomeActivity.class));
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }


    void setupToolbar(){
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public String findCost(String price,String discount, String quantity){
        float Price = Float.parseFloat(price);
        float Discount = Float.parseFloat(discount);

        float cost = (float) (Price - Discount * 0.01 * Price);
        TotalCost = TotalCost + cost * (float)Integer.parseInt(quantity);
        return Float.toString(cost);
    }

}
