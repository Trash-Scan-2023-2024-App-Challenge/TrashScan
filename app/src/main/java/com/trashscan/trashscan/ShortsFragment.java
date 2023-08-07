package com.trashscan.trashscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class ShortsFragment extends Fragment {

    ArrayList<Post> posts = new ArrayList<>();

    RecyclerView rvItems;
    // Add other views to display city, zip code, state, country, address here

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shorts, container, false);
        rvItems = rootView.findViewById(R.id.itemsRV);
        FirebaseFirestore db =FirebaseFirestore.getInstance();

        db.collection("posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot d : task.getResult()) {
                        Post p = new Post(
                                (String)d.getData().get("name"),
                                (String)d.getData().get("desc"),
                                (String)d.getData().get("imageUrl")
                        ); // NOTE: These are the field names for a post document
                        posts.add(p);
                    }

                }
            }
        });


        return rootView;
    }
}