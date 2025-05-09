package com.example.newsapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NewsAdapter newsAdapter;
    private final List<NewsArticle> newsList = new ArrayList<>();
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewNews);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Add item decoration for spacing between items
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        recyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));

        // Setup adapter
        newsAdapter = new NewsAdapter(newsList);
        recyclerView.setAdapter(newsAdapter);

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeColors(android.graphics.Color.parseColor("#2196F3"));
        swipeRefreshLayout.setOnRefreshListener(this::refreshNews);

        // Load initial data
        loadNews();
    }

    private void loadNews() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateTextView.setVisibility(View.GONE);

        String url = "https://gnews.io/api/v4/top-headlines?country=us&token=d722574e8e75944372536ab665e1d936";


        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<NewsArticle> apiNews = new ArrayList<>();
                    try {
                        JSONArray articles = response.getJSONArray("articles");
                        for (int i = 0; i < articles.length(); i++) {
                            JSONObject article = articles.getJSONObject(i);

                            String title = article.getString("title");
                            String description = article.getString("description");
                            String time = article.getString("publishedAt");
                            String urlToNews = article.getString("url");

                            apiNews.add(new NewsArticle(title, description, time, urlToNews));
                        }
                        updateNewsDisplay(apiNews);
                    } catch (JSONException e) {
                        showError("Parsing error");
                    }
                },
                error -> showError("Failed to fetch news"));

        queue.add(jsonObjectRequest);
    }


    private void refreshNews() {
        String url = "https://gnews.io/api/v4/top-headlines?country=us&token=d722574e8e75944372536ab665e1d936";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<NewsArticle> refreshedNews = new ArrayList<>();
                    try {
                        JSONArray articles = response.getJSONArray("articles");
                        for (int i = 0; i < articles.length(); i++) {
                            JSONObject article = articles.getJSONObject(i);

                            String title = article.getString("title");
                            String description = article.getString("description");
                            String time = article.getString("publishedAt");
                            String urlToNews = article.getString("url");

                            refreshedNews.add(new NewsArticle(title, description, time, urlToNews));
                        }

                        // Inject a few new mock articles at top
                        List<NewsArticle> newMockArticles = getRandomMockArticles(2); // Add 2 mock "new" articles
                        refreshedNews.addAll(0, newMockArticles);



                        updateNewsDisplay(refreshedNews);
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "News updated!", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        swipeRefreshLayout.setRefreshing(false);
                        showError("Parsing error");
                    }
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showError("Failed to refresh news");
                });

        queue.add(jsonObjectRequest);
    }





    private void updateNewsDisplay(List<NewsArticle> fetchedNews) {
        progressBar.setVisibility(View.GONE);

        if (fetchedNews.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            newsList.clear();
            newsList.addAll(fetchedNews);
            newsAdapter.notifyDataSetChanged();
        }
    }

    private void showError(final String message) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);

            if (newsList.isEmpty()) {
                emptyStateTextView.setText(R.string.error_loading_news);
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private List<NewsArticle> createMockNewsData() {
        List<NewsArticle> mockNews = new ArrayList<>();
        mockNews.add(new NewsArticle(
                "Global Tech Conference Announces Breakthrough in AI",
                "Scientists reveal new advancements in machine learning that could revolutionize healthcare diagnostics, enabling earlier detection of diseases and more personalized treatment plans.",
                "2 hours ago",
                "https://example.com/tech-ai-breakthrough"));

        mockNews.add(new NewsArticle(
                "New Climate Agreement Reached at International Summit",
                "World leaders agree on ambitious targets to reduce carbon emissions by 2030, with developed nations pledging significant financial support for sustainable energy initiatives in developing countries.",
                "5 hours ago",
                "https://example.com/climate-agreement"));

        mockNews.add(new NewsArticle(
                "SpaceX Successfully Launches Next-Generation Satellite",
                "The mission marks a significant milestone in expanding global internet coverage, potentially bringing high-speed connectivity to remote regions previously underserved by traditional infrastructure.",
                "Yesterday",
                "https://example.com/spacex-launch"));

        mockNews.add(new NewsArticle(
                "Major Breakthrough in Renewable Energy Storage",
                "Researchers develop new battery technology that could make renewable energy more viable for widespread adoption, solving the intermittency challenge that has limited solar and wind power integration.",
                "2 days ago",
                "https://example.com/energy-storage"));

        mockNews.add(new NewsArticle(
                "Global Economy Shows Signs of Recovery",
                "Economic indicators suggest a rebound following last year's downturn, with job growth accelerating and consumer confidence reaching pre-pandemic levels in several major markets.",
                "3 days ago",
                "https://example.com/economy-recovery"));

        mockNews.add(new NewsArticle(
                "New Archaeological Discovery Redefines Ancient History",
                "Excavation in the Mediterranean reveals artifacts that may require historians to reconsider the timeline of early human civilization and technological development.",
                "4 days ago",
                "https://example.com/archaeology-discovery"));

        return mockNews;
    }

    private List<NewsArticle> getRandomMockArticles(int count) {
        List<NewsArticle> allMock = createMockNewsData();
        List<NewsArticle> randomPicks = new ArrayList<>();
        java.util.Collections.shuffle(allMock);
        for (int i = 0; i < Math.min(count, allMock.size()); i++) {
            NewsArticle article = allMock.get(i);
            // Add "Just now" tag to simulate freshness
            randomPicks.add(new NewsArticle(
                    article.getTitle(),
                    article.getDescription(),
                    "Just now",
                    article.getUrl()
            ));
        }
        return randomPicks;
    }


    public static class NewsArticle {
        private final String title;
        private final String description;
        private final String timestamp;
        private final String url;

        public NewsArticle(String title, String description, String timestamp, String url) {
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
            this.url = url;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getTimestamp() { return timestamp; }
        public String getUrl() { return url; }
    }

    public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

        private final List<NewsArticle> articles;

        public NewsAdapter(List<NewsArticle> articles) {
            this.articles = articles;
        }

        @Override
        public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.news_item, parent, false);
            return new NewsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NewsViewHolder holder, int position) {
            NewsArticle article = articles.get(position);
            holder.titleTextView.setText(article.getTitle());
            holder.descriptionTextView.setText(article.getDescription());
            holder.timestampTextView.setText(article.getTimestamp());

            // Add a special indicator for very recent news
            if (article.getTimestamp().equals("Just now")) {
                holder.newBadge.setVisibility(View.VISIBLE);
            } else {
                holder.newBadge.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(article.getUrl()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                            "Cannot open article: " + article.getTitle(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return articles.size();
        }

        class NewsViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, descriptionTextView, timestampTextView, newBadge;

            public NewsViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.textViewTitle);
                descriptionTextView = itemView.findViewById(R.id.textViewDescription);
                timestampTextView = itemView.findViewById(R.id.textViewTimestamp);
                newBadge = itemView.findViewById(R.id.textViewNewBadge);
            }
        }
    }

    // Custom item decoration for spacing between items
    public class ItemSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public ItemSpacingDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(android.graphics.Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = spacing;

            // Add top spacing for the first item
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing;
            }
        }
    }
}