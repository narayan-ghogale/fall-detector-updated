package com.tabian.tabfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Tab3Fragment extends Fragment {
    private static final String TAG = "Tab3Fragment";

    private Button btnTEST;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab3_fragment,container,false);

        String statement;
        statement = "Falls of the elderly always lead to serious health issues as the decline of their physical fitness. At most fall situations, the fall process is the main source of injury because of the high impact. But sometimes the late medical salvage may worsen the situation. That means the faster the salvage comes, the less risk the elderly will face.\n" +
                "\n" +
                "Progress of technology brings more possibilities to help us protect the elderly."+"Motion sensor-based method is also commonly used. Accelerometer and gyroscope could provide linear and angular motion information directly. Sensor measurements or their proper fusion could be used to distinguish a real fall.";
        statement = statement + "The app can detect the elderly’s falling by acceleration analysis. Then it will get the elderly’s geographic position and send fall alarm short message to caregivers. So the elderly who has fallen can get timely help to minimize the negative influence.";
        TextView statement_txt = (TextView) view.findViewById(R.id.textView3);
        statement_txt.setText(statement);
        return view;
    }
}
