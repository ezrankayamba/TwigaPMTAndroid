package tz.co.nezatech.apps.twigapmt;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import tz.co.nezatech.apps.twigapmt.model.IdName;
import tz.co.nezatech.apps.twigapmt.util.Constants;

public class ProjectDetailsActivity extends AppCompatActivity {
    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details2);

        String requestId = getIntent().getStringExtra(Constants.EXTRAS_PROJECT_ID);
        if (requestId != null) {
            String[] tokens = requestId.split(Constants.PROJECT_ID_NAME_SEP);
            projectId = Integer.parseInt(tokens[0].trim());
            getSupportActionBar().setTitle(tokens[1].trim());
        }
        getSupportActionBar().setSubtitle("Project Details");

        WebView myWebView = findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadUrl(Constants.BASE_URL + "projects/alert/" + projectId);
    }
}
