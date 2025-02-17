package com.example.oop_project;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.oop_project.adapters.admin.AdapterBorrowsAdmin;
import com.example.oop_project.databinding.FragmentBorrowsAdminBinding;
import com.example.oop_project.models.ModelEquipment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class BorrowsAdminFragment extends Fragment {


    private String categoryId;
    private String title;
    private String uid;
    private Context context;
    private FragmentBorrowsAdminBinding binding;
    private ArrayList<ModelEquipment> equipmentArrayListBorrowing;
    private ArrayList<ModelEquipment> equipmentArrayListBorrowed;
    private AdapterBorrowsAdmin adapterBorrowsAdmin;
    private ArrayList<ModelEquipment> getEquipmentArrayListRefuse;
    private ArrayList<ModelEquipment> equipmentArrayListAccept;
    private String equipmentId;

    public BorrowsAdminFragment() {
        // Required empty public constructor
    }

    public static BorrowsAdminFragment newInstance(String categoryId, String title, String uid) {
        BorrowsAdminFragment fragment = new BorrowsAdminFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("categoryTitle", title);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    void fetchDataFromFirebase(String equipmentId, MyCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Equipments");
        ref.child(equipmentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String title = "" + snapshot.child("title").getValue();
                callback.onSuccess(title);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            title = getArguments().getString("categoryTitle");
            uid = getArguments().getString("uid");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBorrowsAdminBinding.inflate(LayoutInflater.from(getContext()), container, false);
        if (categoryId.equals("01")) {
            // load report layout + button
            loadBorrowingEquipments();

        } else if (categoryId.equals("02")) {
            // hidden report layout + button
            loadBorrowedEquipments();

        } else if (categoryId.equals("03")) {
            loadRefuseEquipments();
        }
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterBorrowsAdmin.getFilter().filter(s);
                } catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }

    private void loadBorrowingEquipments() {
        equipmentArrayListBorrowing = new ArrayList<>();
        if (equipmentArrayListBorrowing.size() != 0) {
            equipmentArrayListBorrowing.clear();
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("EquipmentsBorrowed");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                equipmentArrayListBorrowing.clear();
                Object lock = new Object();
                synchronized (lock) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (("" + ds.child("status").getValue()).equals("Borrowed")) {
                            String key = ds.getKey();
                            String uid = "" + ds.child("uid").getValue();
                            ModelEquipment model = snapshot.getValue(ModelEquipment.class);
                            String equipmentId = "" + ds.child("equipmentId").getValue();
                            String title = "" + ds.child("title").getValue();
                            String preStatus = "";
                            if (ds.hasChild("preStatus")) {
                                preStatus = "" + ds.child("preStatus").getValue();
                            }
                            model.setTitle(title);
                            model.setPreStatus(preStatus);
                            model.setId(equipmentId);
                            model.setKey(key);
                            model.setUid(uid);
                            model.setStatus("Borrowed");
                            equipmentArrayListBorrowing.add(model);


                        }
                    }
                }

                adapterBorrowsAdmin = new AdapterBorrowsAdmin(getContext(), equipmentArrayListBorrowing);
                adapterBorrowsAdmin.notifyDataSetChanged();
                binding.equipmentRv.setAdapter(adapterBorrowsAdmin);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void loadBorrowedEquipments() {
        equipmentArrayListBorrowed = new ArrayList<>();
        if (equipmentArrayListBorrowed.size() != 0) {
            equipmentArrayListBorrowed.clear();
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("EquipmentsBorrowed");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                equipmentArrayListBorrowed.clear();
                Object lock = new Object();
                synchronized (lock) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (("" + ds.child("status").getValue()).equals("History")) {
                            String key = ds.getKey();
                            String uid = "" + ds.child("uid").getValue();
                            ModelEquipment model = snapshot.getValue(ModelEquipment.class);
                            String equipmentId = "" + ds.child("equipmentId").getValue();
                            String title = "" + ds.child("title").getValue();
                            String preStatus = "";
                            if (ds.hasChild("preStatus")) {
                                preStatus = "" + ds.child("preStatus").getValue();
                            }
                            model.setTitle(title);
                            model.setId(equipmentId);
                            model.setKey(key);
                            model.setPreStatus(preStatus);
                            model.setUid(uid);
                            model.setStatus("History");

                            equipmentArrayListBorrowed.add(model);
                        }
                    }
                }
                adapterBorrowsAdmin = new AdapterBorrowsAdmin(getContext(), equipmentArrayListBorrowed);
                adapterBorrowsAdmin.notifyDataSetChanged();
                binding.equipmentRv.setAdapter(adapterBorrowsAdmin);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadRefuseEquipments() {
        getEquipmentArrayListRefuse = new ArrayList<>();
        if (getEquipmentArrayListRefuse.size() != 0) {
            getEquipmentArrayListRefuse.clear();
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("EquipmentsBorrowed");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getEquipmentArrayListRefuse.clear();
                Object lock = new Object();
                synchronized (lock) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (("" + ds.child("status").getValue()).equals("Refuse")) {
                            String key = ds.getKey();
                            String uid = "" + ds.child("uid").getValue();
                            ModelEquipment model = snapshot.getValue(ModelEquipment.class);
                            String equipmentId = "" + ds.child("equipmentId").getValue();
                            String title = "" + ds.child("title").getValue();
                            model.setTitle(title);

                            model.setId(equipmentId);
                            model.setKey(key);
                            model.setUid(uid);
                            model.setStatus("Refuse");
                            getEquipmentArrayListRefuse.add(model);

                        }
                    }
                }

                adapterBorrowsAdmin = new AdapterBorrowsAdmin(getContext(), getEquipmentArrayListRefuse);
                adapterBorrowsAdmin.notifyDataSetChanged();
                binding.equipmentRv.setAdapter(adapterBorrowsAdmin);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    interface MyCallback {
        void onSuccess(String title);

        void onError(DatabaseError error);
    }

}
