package be.klarhopur.prom;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class POIAdapter extends RecyclerView.Adapter<POIAdapter.POIViewHolder> {


    private final ArrayList<PointOfInterest> pointOfInterest;

    private POIViewHolder viewHolder;

    public POIAdapter(ArrayList<PointOfInterest> pointsOfInterest) {
        this.pointOfInterest = pointsOfInterest;
    }

    @NonNull
    @Override
    public POIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poi_list_elements, parent, false);
        viewHolder = new POIAdapter.POIViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull POIViewHolder holder, int position) {
        final PointOfInterest poi = pointOfInterest.get(position);

        holder.mainTextView.setText(poi.getName());
        holder.textViewAddress.setText(poi.getAddress());
    }

    @Override
    public int getItemCount() {
        return pointOfInterest.size();
    }

    public class POIViewHolder extends RecyclerView.ViewHolder {

        public TextView mainTextView;
        public TextView textViewAddress;
        public ImageView imageView;

        public POIViewHolder(View itemView) {
            super(itemView);

            mainTextView = itemView.findViewById(R.id.mainTextView);
            textViewAddress = itemView.findViewById(R.id.addressTextView);
            imageView = itemView.findViewById(R.id.imageViewMap);
        }
    }
}

