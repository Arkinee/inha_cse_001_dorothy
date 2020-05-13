package com.inha.dorothy.src.mydraw;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.inha.dorothy.BaseActivity;
import com.inha.dorothy.ItemDecoration;
import com.inha.dorothy.R;
import com.inha.dorothy.src.entrance.Room;
import com.inha.dorothy.src.entrance.RoomInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

public class MyDrawActivity extends BaseActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = mAuth.getCurrentUser();
    private FirebaseDatabase mFirebase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = mFirebase.getReference();
    private DatabaseReference mRoomTitle = mFirebase.getReference("room").child("room_id");

    private ArrayList<MyDraw> mDrawArrayList;
    private ArrayList<DrawPerRoom> mRoomList;
    private DrawAdapter mAdapter;

    private TextView mTvNumOfDraw;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_draw);

        RecyclerView rvMyDraw = findViewById(R.id.rv_my_draw);
        rvMyDraw.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        rvMyDraw.addItemDecoration(new ItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing),
                getResources().getInteger(R.integer.draw_list_columns)));

        mTvNumOfDraw = findViewById(R.id.tv_my_draw_number);
        mDrawArrayList = new ArrayList<>();
        mRoomList = new ArrayList<>();
        mAdapter = new DrawAdapter(this, mDrawArrayList);
        rvMyDraw.setAdapter(mAdapter);

        accessDatabase();

        //그림 클릭 이벤트
        mAdapter.setOnItemClickListener(new DrawAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                showCustomMessage(mDrawArrayList.get(pos).room);
            }
        });

    }

    void accessDatabase() {
        mRoomTitle.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRoomList.clear();
                if (dataSnapshot.hasChildren()) {
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()) {
                        DataSnapshot snap = iter.next();
                        RoomInfo info = snap.child("RoomInfo").getValue(RoomInfo.class);
                        ArrayList<String> ids = new ArrayList<>();

                        if(snap.child("RoomInfo").child("downloadURL").hasChildren()){
                            Iterator<DataSnapshot> iter2 = snap.child("RoomInfo").child("downloadURL").getChildren().iterator();
                            while (iter2.hasNext()){
                                DataSnapshot snap2 = iter2.next();
                                ids.add(snap2.getKey());
                                Log.d("로그", "추가: " + snap2.getKey());
                            }
                        }

                        mRoomList.add(new DrawPerRoom(snap.getKey(), info.title, ids));
                        Log.d("로그", "리스트에 추가: " + snap.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRef.child("user").child(mUser.getUid()).child("downloadURL").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDrawArrayList.clear();

                if (dataSnapshot.hasChildren()) {
                    Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                    while (iter.hasNext()) {
                        DataSnapshot snap = iter.next();
                        DrawInfo info = snap.getValue(DrawInfo.class);
                        Log.d("로그", "key: " + snap.getKey());
                        mTitle = compare(snap.getKey());
                        Log.d("로그", "title: " + mTitle);
                        mDrawArrayList.add(new MyDraw(snap.getKey(), mTitle, info));
                    }
                }
                mAdapter.notifyDataSetChanged();
                mTvNumOfDraw.setText(String.valueOf(mDrawArrayList.size()).concat(" photos"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String compare(String id){
        String result = "";
        for(int i=0; i < mRoomList.size(); i++){
            for(int j=0; j<mRoomList.get(i).ids.size(); j++){
                if(id.equals(mRoomList.get(i).ids.get(j))){
                    result = mRoomList.get(i).title;
                    return result;
                }
            }
        }

        return result;
    }

    public void OnClick(View view) {
        switch (view.getId()){
            case R.id.iv_my_draw_back_arrow:
                finish();
                break;
            case R.id.iv_my_draw_menu:
                break;
        }

    }

}
