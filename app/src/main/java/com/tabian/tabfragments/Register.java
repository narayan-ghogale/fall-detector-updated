package com.tabian.tabfragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity
{
    // UI references.
    private AutoCompleteTextView usernameView,fnameTextView,lnameTextView,phoneTextView,mPasswordView,mconfirmPassView;
    private AutoCompleteTextView licenceView, addressView;
    private View mProgressView;
    private View mRegisterFormView;
    private String username,password,fname,lname,phone,licence,address,conf_password;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){
            //
        }
        setContentView(R.layout.register);

        initializeViews();

        alertDialog = new AlertDialog.Builder(
                Register.this).create();
    }

    private void initializeViews()
    {
        usernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mPasswordView = (AutoCompleteTextView)findViewById(R.id.password);
        mconfirmPassView = (AutoCompleteTextView)findViewById(R.id.confirm_password);
        fnameTextView = (AutoCompleteTextView)findViewById(R.id.first_name);
        lnameTextView = (AutoCompleteTextView)findViewById(R.id.last_name);
        mProgressView = findViewById(R.id.register_progress);
        alertDialog = new AlertDialog.Builder(
                Register.this).create();
    }

    public void createAccount(View view)
    {
        username = usernameView.getText().toString();
        fname = fnameTextView.getText().toString();
        lname = lnameTextView.getText().toString();
        password = mPasswordView.getText().toString();
        conf_password = mconfirmPassView.getText().toString();
        if(validate())
        {
            final ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage("Loading...");
            pDialog.show();


            //register logic
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            Map<String, String> params = new HashMap<>();
            //Adding parameters to request
            params.put(ConfigConstants.KEY_EMAIL, username);
            params.put(ConfigConstants.KEY_PASSWORD, password);
            params.put("operation", "signin");
            params.put("first_name", fname);
            params.put("last_name", lname);


            //Creating a json request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, ConfigConstants.LOGIN_REGISTER_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                //Toast.makeText(Register.this, response.toString(), Toast.LENGTH_SHORT).show();
                                if ((response.getString("message")).equals("success"))
                                {
                                    onSuccessfulSignin(response.getString("email"));
                                    pDialog.hide();
                                }
                                else
                                {
                                    alertDialog.setTitle("Error");
                                    alertDialog.setMessage("There's a problem creating your account. Please try again with correct information.");
                                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //
                                        }
                                    });
                                    alertDialog.show();
                                    pDialog.hide();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //You can handle error here if you want
                                }
                            });

            //Adding the string request to the queue
            requestQueue.add(jsonObjectRequest);
        }
    }

    private boolean validate()
    {
        boolean cancel=false;
        View focusView = null;
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(username);

        if(fname.equals("")){
            fnameTextView.setError(getString(R.string.error_field_required));
            focusView = fnameTextView;
            cancel = true;
        }
        else if(lname.equals("")){
            lnameTextView.setError(getString(R.string.error_field_required));
            focusView = lnameTextView;
            cancel = true;
        }
        else if(username.equals("")){
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        }
        else if(m.find()){
            usernameView.setError(getString(R.string.error_invalid_username));
            focusView = usernameView;
            cancel = true;
        }
        else if(password.equals("")){
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else if(password.length()<6){
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        else if(!conf_password.equals(password)){
            mconfirmPassView.setError(getString(R.string.error_conf_password));
            focusView = mconfirmPassView;
        }


        if (cancel)
            focusView.requestFocus();
        return cancel;
    }


    private void onSuccessfulSignin(String email) {
        SessionManager s = new SessionManager(getApplicationContext());
        s.createLoginSession(email);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
