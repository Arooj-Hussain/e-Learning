package android.software.com.e_learning;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class FirstFragment extends Fragment {


    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        Toast.makeText(getActivity(), "on first fragment", Toast.LENGTH_SHORT).show();
//        TextView textView = (TextView) view.findViewById(R.id.tvLabel);
//        textView.setText("Fragment first");
        return view;
    }
}