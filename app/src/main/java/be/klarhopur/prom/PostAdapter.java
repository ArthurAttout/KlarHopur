package be.klarhopur.prom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;
import java.util.List;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView nom,adresse;

        public ViewHolder(View itemView){
            super(itemView);

            nom=(TextView)itemView.findViewById(R.id.mainTextView);
            adresse=(TextView)itemView.findViewById(R.id.addressTextView);
        }
    }
    private Context context;
    private List<Post> posts;
    public PostAdapter(Context c, List<Post> postList){
        this.context=c;
        posts=postList;
    }
    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(context).inflate(R.layout.poi_list_elements, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PostAdapter.ViewHolder holder, int position) {
        Post p = posts.get(position);
        holder.nom.setText(p.getNom());
        holder.adresse.setText(p.getAdresse());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
