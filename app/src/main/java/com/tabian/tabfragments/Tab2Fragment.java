package com.tabian.tabfragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Tab2Fragment extends Fragment
{
    private static final String TAG = "Tab2Fragment";
    private static Button btn;
    private static EditText contactTxt;
    private static boolean added=false;
    static ArrayList<String> x;
    SessionManager s;
    static String email;
    static Activity activity;
    static Context context;
    static View view;
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.tab2_fragment, container, false);

        btn = (Button) view.findViewById(R.id.addContacts);
        contactTxt = (EditText)view.findViewById(R.id.editText);

        x=new ArrayList<>();
        s = new SessionManager(getContext());
        SharedPreferences sp = s.pref;
        email = sp.getString(ConfigConstants.KEY_EMAIL, "");

        activity = getActivity();
        context = getContext();

        getContacts();

        //ListView listView = (ListView) view.findViewById(R.id.list);
        //listView.setAdapter(new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_1 ,x));
        return view;
    }

    static void getContacts()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        Map<String,String> params = new HashMap<>();
        //Adding parameters to request
        params.put(ConfigConstants.KEY_EMAIL, email);
        //Toast.makeText(activity, email, Toast.LENGTH_LONG).show();
        params.put("operation", "get_contacts");

        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.LOGIN_REGISTER_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(activity, response.toString(), Toast.LENGTH_LONG).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                JSONArray contacts = response.getJSONArray("contacts");
                                x.clear();
                                for(int i=0;i<contacts.length();i++){
                                    x.add((String)contacts.get(i));
                                }
                                ListView listView = (ListView) view.findViewById(R.id.list);
                                listView.setAdapter(new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_list_item_1 ,x));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,
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

    static void addContact()
    {
        if(added){
            contactTxt.setVisibility(View.GONE);
            btn.setText("ADD CONTACT");
            //if(!x.contains(contactTxt.getText().toString()))
                //x.add(contactTxt.getText().toString());

            added = false;
            RequestQueue requestQueue = Volley.newRequestQueue(activity);
            Map<String,String> params = new HashMap<>();
            //Adding parameters to request
            params.put(ConfigConstants.KEY_EMAIL, email);
            params.put("contact",contactTxt.getText().toString());
            params.put("operation", "add_contacts");

            //Toast.makeText(activity, email+" "+contactTxt.getText().toString(), Toast.LENGTH_LONG).show();

            //Creating a json request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, ConfigConstants.LOGIN_REGISTER_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                //Toast.makeText(activity, response.toString(), Toast.LENGTH_LONG).show();
                                if ((response.getString("message")).equals("success"))
                                {
                                    getContacts();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context,
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

            contactTxt.setText("");
        }
        else{
            contactTxt.setVisibility(View.VISIBLE);
            btn.setText("ADD");
            added = true;
        }

    }

    void refresh()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(Tab2Fragment.this).attach(Tab2Fragment.this).commit();
    }
}