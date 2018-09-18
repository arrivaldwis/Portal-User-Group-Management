package id.portaluserfacebook.news.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.portaluserfacebook.R;
import id.portaluserfacebook.news.adapter.ArticleAdapter;
import id.portaluserfacebook.news.api.APIService;
import id.portaluserfacebook.news.config.Constant;
import id.portaluserfacebook.news.model.Article;
import id.portaluserfacebook.news.model.NewsHeadline;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {

    private static final String TAG = "NewsFragment";
    @BindView(R.id.rv_data)
    RecyclerView rv_data;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipe_refresh;

    private ArrayList<Article> newsList;
    private ArticleAdapter mAdapter;
    private String menu;

    public NewsFragment() {
        // Required empty public constructor
    }

    public static NewsFragment newInstance(String menu) {
        NewsFragment myFragment = new NewsFragment();

        Bundle args = new Bundle();
        args.putString("menu", menu);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        setUI(view);
        loadNews();
        return view;
    }

    private void setUI(View v) {
        ButterKnife.bind(this, v);
        newsList = new ArrayList<>();
        LinearLayoutManager llManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_data.setLayoutManager(llManager);
        mAdapter = new ArticleAdapter(newsList, getActivity());
        rv_data.setAdapter(mAdapter);
        menu = getArguments().getString("menu");

        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNews();
            }
        });
    }

    private void loadNews() {
        newsList.clear();
        swipe_refresh.setRefreshing(true);
        APIService service = APIService.retrofit.create(APIService.class);
        Call<NewsHeadline> call = null;
        call = service.loadHeadlines("id", menu, Constant.API_KEY);
        call.enqueue(new Callback<NewsHeadline>() {
            @Override
            public void onResponse(Call<NewsHeadline> call, Response<NewsHeadline> response) {
                if (response.isSuccessful()) {
                    NewsHeadline article = response.body();
                    for (int i = 0; i < article.getArticles().size(); i++) {
                        Article articleData = article.getArticles().get(i);
                        newsList.add(articleData);
                    }
                    mAdapter.notifyDataSetChanged();
                    swipe_refresh.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<NewsHeadline> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });
    }

}
