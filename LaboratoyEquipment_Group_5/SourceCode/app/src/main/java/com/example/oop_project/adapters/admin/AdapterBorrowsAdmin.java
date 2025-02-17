package com.example.oop_project.adapters.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.oop_project.MyApplication;
import com.example.oop_project.activities.admin.BorrowsAdminActivity;
import com.example.oop_project.activities.common.EquipmentDetailActivity;
import com.example.oop_project.databinding.RowBorrowsAdminBinding;
import com.example.oop_project.filters.admin.FilterBorrowsAdmin;
import com.example.oop_project.models.ModelEquipment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterBorrowsAdmin extends RecyclerView.Adapter<AdapterBorrowsAdmin.HolderBorrowsAdmin> implements Filterable {
    public ArrayList<ModelEquipment> equipmentArrayList, filterList;
    private final Context context;
    private FilterBorrowsAdmin filter;
    private RowBorrowsAdminBinding binding;
    private ArrayList<Boolean> isCallbackHandled;


    public AdapterBorrowsAdmin(Context context, ArrayList<ModelEquipment> equipmentArrayList) {
        this.context = context;
        this.equipmentArrayList = equipmentArrayList;
        filterList = equipmentArrayList;
        isCallbackHandled = new ArrayList<>();
        for(int i = 0; i <= 1000; i++){
            isCallbackHandled.add(false);
        }

    }

    @NonNull
    @Override
    public HolderBorrowsAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowBorrowsAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderBorrowsAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBorrowsAdmin holder, @SuppressLint("RecyclerView") int position) {
        ModelEquipment model = equipmentArrayList.get(position);
        String equipmentId = model.getId();
        String key = model.getKey();
        binding.textDate.setText("Thời gian mượn: ");
        holder.categoryTv.setVisibility(View.GONE);
        String uid = model.getUid();
        String preStatus = model.getPreStatus();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("EquipmentsBorrowed");
        ref.child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String date = "";
                        if(timestamp.equals("null") || timestamp.equals("")){

                        }else{
                            date = MyApplication.formatTimestampToDetailTime(Long.parseLong(timestamp));
                        }
                        holder.dateTv.setText(date);
                        String fullName = "" + snapshot.child("fullName").getValue();
                        holder.descriptionTv.setText("Người mượn: " + fullName);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .child("Borrowed").child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String equipmentId = "" + snapshot.child("equipmentId").getValue();
                        String quantityBorrowed = "" + snapshot.child("quantityBorrowed").getValue();
                        holder.quantityTv.setText(quantityBorrowed);
                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Equipments");
                        ref1.child(equipmentId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshots) {
                                        String title = "" + snapshots.child("title").getValue();
                                        String description = "" + snapshots.child("description").getValue();
                                        holder.titleTv.setText(title);
//                                                        holder.descriptionTv.setText(description);
                                        String equipmentId = "" + snapshots.child("id").getValue();
                                        model.setId(equipmentId);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EquipmentDetailActivity.class);
                intent.putExtra("equipmentId", model.getId());
                intent.putExtra("status", model.getStatus());
                intent.putExtra("key", model.getKey());
                intent.putExtra("role", "admin");
                intent.putExtra("personI4", "admin");
                intent.putExtra("preStatus", preStatus);
                context.startActivity(intent);
            }
        });
        DatabaseReference refImage = FirebaseDatabase.getInstance().getReference("Equipments");
        refImage.child(equipmentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object lock = new Object();
                synchronized (lock){
                    final int finalPosition = position; // Gán giá trị của "position" vào biến tạm thời
                    String title = "" + snapshot.child("title").getValue();
                    String equipmentImage = "" + snapshot.child("equipmentImage").getValue();
                    Log.d("Position", ""+finalPosition);
                    Log.d("Image", equipmentImage);
                    if(context instanceof BorrowsAdminActivity){
                        BorrowsAdminActivity activity = (BorrowsAdminActivity) context;
                        if(!activity.isDestroyed() && !(equipmentImage.equals("null") || equipmentImage.equals(""))){
                            Glide.with(context)
                                    .load(equipmentImage)
                                    .centerCrop()
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            if (!isCallbackHandled.get(finalPosition)) { // Kiểm tra nếu chưa xử lý callback
                                                holder.progressBar.setVisibility(View.VISIBLE);
                                                holder.imageView.setVisibility(View.GONE);



                                                // Đánh dấu là đã xử lý callback
                                                isCallbackHandled.set(finalPosition, true);
                                            }

                                            // Trả về false để cho phép Glide xử lý callback tiếp
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                            if (!isCallbackHandled.get(finalPosition)) { // Kiểm tra nếu chưa xử lý callback
                                                Log.d("Check position", "" + finalPosition);
                                                Log.d("Handle", "" + isCallbackHandled.get(finalPosition));
                                                holder.imageView.setVisibility(View.VISIBLE);
                                                holder.progressBar.setVisibility(View.GONE);


                                                // Đánh dấu là đã xử lý callback
                                                isCallbackHandled.set(finalPosition, true);

                                                Log.d("Hanlde old", ""+isCallbackHandled.get(finalPosition));
                                            }

                                            // Trả về false để cho phép Glide xử lý callback tiếp
                                            return false;
                                        }
                                    })
                                    .into(holder.imageView);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    @Override
    public int getItemCount() {
        return equipmentArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterBorrowsAdmin(filterList, this);
        }
        return filter;
    }

    class HolderBorrowsAdmin extends RecyclerView.ViewHolder {
        TextView titleTv, categoryTv, quantityTv, descriptionTv, dateTv;
        ProgressBar progressBar;
        ImageView imageView;

        public HolderBorrowsAdmin(@NonNull View itemView) {
            super(itemView);
            titleTv = binding.titleTv;
            categoryTv = binding.categoryTv;
            quantityTv = binding.quantityTv;
            descriptionTv = binding.descriptionTv;
            dateTv = binding.dateTv;
            imageView = binding.imageView;
            progressBar = binding.progressBar;
        }
    }
}
