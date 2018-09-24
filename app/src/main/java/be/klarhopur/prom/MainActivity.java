package be.klarhopur.prom;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });
        recyclerView = findViewById(R.id.recyclerViewHistory);
        initialSetup();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);
    }


    private void openDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_choose_path, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        view.findViewById(R.id.imageButtonAtoB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), AtoBPathActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.imageButtonDistance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), DistancePathActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.imageButtonRandom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                openDialogRandomPath();
            }
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void openDialogRandomPath() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_random_path, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        view.findViewById(R.id.buttonShortPath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), RandomPathActivity.class);
                intent.putExtra("type",EnumTypeRandomPath.SHORT);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.buttonMediumPath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), RandomPathActivity.class);
                intent.putExtra("type",EnumTypeRandomPath.MEDIUM);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.buttonLongPath).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), RandomPathActivity.class);
                intent.putExtra("type",EnumTypeRandomPath.LONG);
                startActivity(intent);
            }
        });
        dialog.setContentView(view);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(getBaseContext(), AuthActivity.class);
                            startActivity(intent);
                        }
                    });

                default:
                    return super.onOptionsItemSelected(item);

        }
    }


    private void initialSetup() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Subscribe the user if doesn't exist
                Log.i("firebase","in");
                if(!dataSnapshot.hasChild(user.getUid())){
                    firebaseDatabase.child("users")
                        .child(user.getUid())
                        .setValue(new UserFirebase(user.getEmail(),user.getDisplayName(),4, new HashMap<String, Path>()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("firebase",databaseError.getDetails());
            }
        });

        firebaseDatabase
            .child("users")
            .child(user.getUid())
            .child("path_history")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final HashMap<String,Map<String,Object>> data = (HashMap<String, Map<String,Object>>) dataSnapshot.getValue();
                    if(data == null)return;

                    final ArrayList<PathRecord> records = new ArrayList<>();
                    for (Map.Entry<String, Map<String,Object>> stringObjectEntry : data.entrySet()) {
                        final PathRecord record = PathRecord.fromDataSnapshot(stringObjectEntry.getValue());
                        Map<String,Boolean> poiMap = (Map<String, Boolean>) stringObjectEntry.getValue().get("poi");

                        for (Map.Entry<String, Boolean> stringBooleanEntry : poiMap.entrySet()) {
                            firebaseDatabase
                                .child("poi")
                                .child(stringBooleanEntry.getKey())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        PointOfInterest pointOfInterest = PointOfInterest.fromDataSnapshot((Map<String, Object>) dataSnapshot.getValue());
                                        record.addPOI(pointOfInterest);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                                });
                        }
                        records.add(record);
                    }
                    adapter.setHistory(records);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
    }

    public class HistoryAdapter extends RecyclerView.Adapter<ParentViewHolder> {

        private static final int VIEW_TYPE_NO_ITEMS = 0x98a;
        public ArrayList<PathRecord> history = new ArrayList<>();
        private ParentViewHolder viewHolder;
        private static final int VIEW_TYPE_ITEM = 0x987d;

        public HistoryAdapter(ArrayList<PathRecord> arrayList) {
            history = arrayList;
        }

        public HistoryAdapter() {

        }

        @Override
        public ParentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == VIEW_TYPE_NO_ITEMS){ //Placeholder
                ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.placeholder_history_path, parent, false);
                viewHolder = new PlaceHolderViewHolder(v);
                return viewHolder;
            }
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history_record_layout, parent, false);
            viewHolder = new PathRecordViewHolder(v);
            return viewHolder;
        }

        public void setHistory(ArrayList<PathRecord> history) {
            this.history = history;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(final ParentViewHolder holder, final int position) {
            if(viewHolder instanceof PathRecordViewHolder){
                PathRecordViewHolder viewHolderTemp = (PathRecordViewHolder) viewHolder;
                final PathRecord record = history.get(position);

                viewHolderTemp.mainTextView.setText(record.getTitle());
                viewHolderTemp.secondTextView.setText(record.getDistanceKilometer() + " km  -  " + record.getPoints() + " pts");
                Picasso.get().load(record.getImageURL()).into(viewHolderTemp.imageMap);
                viewHolderTemp.recyclerViewPOI.setVisibility(record.isExpanded() ? View.VISIBLE : View.GONE);
                viewHolderTemp.recyclerViewPOI.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                viewHolderTemp.recyclerViewPOI.setAdapter(new POIAdapter(record.getPointsOfInterest()));

                Picasso.get()
                        .load(record.isExpanded() ? R.drawable.baseline_expand_less_black_24 : R.drawable.baseline_expand_more_black_24)
                        .into(viewHolderTemp.expand);

                viewHolderTemp.expand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        record.setExpanded(!record.isExpanded());
                        notifyItemChanged(position);
                        Log.i("recyclerview",record.getTitle() + " expanded : " + record.isExpanded());
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(history.size() == 0) return VIEW_TYPE_NO_ITEMS;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return history.size() == 0 ? 1 : history.size();
        }
    }

    private class PathRecordViewHolder extends ParentViewHolder{

        public ImageView imageMap;
        public TextView mainTextView;
        public TextView secondTextView;
        public RecyclerView recyclerViewPOI;
        public ImageButton expand;

        public PathRecordViewHolder(View itemView) {

            super(itemView);
            expand = itemView.findViewById(R.id.imageButtonExpand);
            imageMap = itemView.findViewById(R.id.imageViewMap);
            mainTextView = itemView.findViewById(R.id.mainTextView);
            secondTextView = itemView.findViewById(R.id.secondTextView);
            recyclerViewPOI = itemView.findViewById(R.id.recyclerViewPOI);
        }
    }

    private class PlaceHolderViewHolder extends ParentViewHolder{

        public PlaceHolderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class ParentViewHolder extends RecyclerView.ViewHolder{
        public ParentViewHolder(View itemView) {
            super(itemView);
        }
    }


    //endregion
}
