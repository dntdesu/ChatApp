package com.example.dnt.chatapp;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private View requestView;
    private RecyclerView request_view;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestView = inflater.inflate(R.layout.fragment_request, container, false);
        request_view = requestView.findViewById(R.id.request_view);
        request_view.setLayoutManager(new LinearLayoutManager(getContext()));


        return requestView;
    }

}
