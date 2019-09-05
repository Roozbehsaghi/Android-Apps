package com.example.cab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginRegisterActivity extends AppCompatActivity
{
    private Button CustomerLoginButton;
    private Button CustomerRegisterButton;
    private TextView CustomerRegisterlink;
    private TextView CustomerStatus;
    private EditText EmailCustomer;
    private EditText PasswordCustomer;
    private ProgressDialog LoadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference CustomerDatabaseRef;
    private String onlineCustomerID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        mAuth = FirebaseAuth.getInstance();

        CustomerLoginButton = (Button) findViewById(R.id.customer_login_btn);
        CustomerRegisterButton = (Button) findViewById(R.id.customer_register_btn);
        CustomerRegisterlink = (TextView) findViewById(R.id.register_customer_link);
        CustomerStatus = (TextView) findViewById(R.id.customer_status);
        EmailCustomer = (EditText) findViewById(R.id.email_customer);
        PasswordCustomer = (EditText) findViewById(R.id.password_customer);
        LoadingBar = new ProgressDialog(this);


        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);

        CustomerRegisterlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CustomerLoginButton.setVisibility(View.INVISIBLE);
                CustomerRegisterlink.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Register Customer");

                CustomerRegisterButton.setVisibility(View.VISIBLE);
                CustomerRegisterButton.setEnabled(true);
            }
        });

        CustomerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();

                RegisterCustomer(email, password);
            }
        });


        CustomerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String email = EmailCustomer.getText().toString();
                String password = PasswordCustomer.getText().toString();
                
                SignInCustomer(email, password);
            }
        });
    }



    private void SignInCustomer(String email, String password)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write Email...", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write Password...", Toast.LENGTH_SHORT).show();
        }

        else
        {
            LoadingBar.setTitle("Customer Login");
            LoadingBar.setMessage("Please wait, while we are checking your credentials...");
            LoadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                Intent customerIntent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapActivity.class);
                                startActivity(customerIntent);

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Login Successfully...", Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }

                            else
                            {
                                Toast.makeText(CustomerLoginRegisterActivity  .this, "Login Unsuccesful, Please try Again...   ", Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }
                        }
                    });
        }
    }




    private void RegisterCustomer(String email, String password)
    {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write Email...", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this, "Please write Password...", Toast.LENGTH_SHORT).show();
        }

        else
        {
            LoadingBar.setTitle("Customer Registration");
            LoadingBar.setMessage("Please wait, while we are registering your data...");
            LoadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                onlineCustomerID = mAuth.getCurrentUser().getUid();
                                CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(onlineCustomerID);

                                CustomerDatabaseRef.setValue(true);

                                Intent driverIntent = new Intent(CustomerLoginRegisterActivity.this, CustomersMapActivity.class);
                                startActivity(driverIntent);

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Register Successfully...", Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }

                            else
                            {
                                Toast.makeText(CustomerLoginRegisterActivity  .this, "Registeration Unsuccesful, Please try Again...   ", Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }
                        }
                    });
        }
    }
}
