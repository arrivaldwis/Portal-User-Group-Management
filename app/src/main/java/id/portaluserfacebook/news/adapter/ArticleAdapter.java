package id.portaluserfacebook.news.adapter;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import id.portaluserfacebook.R;
import id.portaluserfacebook.news.activity.DetailNewsActivity;
import id.portaluserfacebook.news.activity.ListNewsActivity;
import id.portaluserfacebook.news.model.Article;

/**
 * Created by arrival on 5/14/18.
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private ArrayList<Article> moviesList;
    private Context context;

    public ArticleAdapter(ArrayList<Article> moviesList, Context context) {
        this.moviesList = moviesList;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView img_bg;
        private TextView tv_source;
        private TextView tv_date;
        private TextView tv_judul;
        private RelativeLayout rl_row;

        public ViewHolder(View itemView) {
            super(itemView);
            img_bg = (ImageView) itemView.findViewById(R.id.img_bg);
            tv_source = (TextView) itemView.findViewById(R.id.tv_source);
            tv_date = (TextView) itemView.findViewById(R.id.tv_date);
            tv_judul = (TextView) itemView.findViewById(R.id.tv_judul);
            rl_row = (RelativeLayout) itemView.findViewById(R.id.rl_row);
        }

        @Override
        public void onClick(View view) {

        }
    }

    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_news, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(final ArticleAdapter.ViewHolder holder, final int position) {
        final Article article = moviesList.get(position);
        holder.tv_judul.setText(article.getTitle());
        holder.tv_source.setText(article.getSource().getName());
        holder.tv_date.setText(article.getPublishedAt());
        Picasso.get()
                .load(article.getUrlToImage())
                .into(holder.img_bg);

        holder.rl_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityOptions options = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptions.makeSceneTransitionAnimation((ListNewsActivity) context, view, "s");
                    Intent i = new Intent(context, DetailNewsActivity.class);
                    i.putExtra("article", article);
                    context.startActivity(i, options.toBundle());
                }
            }
        });

        animate(holder);
    }

    public  void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAlpha = AnimationUtils.loadAnimation(context, R.anim.activity_open_translate_from_bottom);
        viewHolder.itemView.setAnimation(animAlpha);
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
