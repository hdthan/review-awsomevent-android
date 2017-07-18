package vn.axonactive.aevent.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import vn.axonactive.aevent.BuildConfig;
import vn.axonactive.aevent.R;
import vn.axonactive.aevent.model.Category;
import vn.axonactive.aevent.model.Event;
import vn.axonactive.aevent.util.DataStorage;

public class DescriptionFragment extends Fragment {

    private ProgressBar mProgressBar;

    private FrameLayout mWebContainer;
    private WebView mWebView;

    ImageView mImage;

    public DescriptionFragment() {

    }

    public static DescriptionFragment newInstance() {
        DescriptionFragment fragment = new DescriptionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_description, container, false);

        Event event = DataStorage.currentEvent;

        TextView mStartTime = (TextView) view.findViewById(R.id.start_time);
        TextView mEndTime = (TextView) view.findViewById(R.id.end_time);
        TextView mName = (TextView) view.findViewById(R.id.name);
        TextView mLocation = (TextView) view.findViewById(R.id.location);

        mWebContainer = (FrameLayout) view.findViewById(R.id.web_container);
        mWebView = new WebView(getContext().getApplicationContext());
        mWebContainer.addView(mWebView);

        // improve performance webview
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());

        mImage = (ImageView) view.findViewById(R.id.image);
        TextView mCategory = (TextView) view.findViewById(R.id.category);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.date_time_format));

        mName.setText(event.getTitle());
        mStartTime.setText("From: " + format.format(event.getStartDate()));
        mEndTime.setText("To:" + format.format(event.getEndDate()));
        mLocation.setText("Location: " + event.getLocation());

        List<Category> categories = event.getCategories();

        StringBuffer sb = new StringBuffer("Category: ");
        sb.append(categories.get(0).getTypeName());

        for (int i = 1; i < categories.size(); i++) {
            sb.append(", ").append(categories.get(i).getTypeName());
        }

        mCategory.setText(sb.toString());

        String description = event.getDescription();

        if (description == null || "".equals(description)) {
            description = "<body>\n" +
                    "\n" +
                    "<p style=\"text-align:center; padding-top: 24px; padding-bottom: 24px; color: #9E9E9E; font-size: 13px\">No description.</p>\n" +
                    "\n" +
                    "</body>";
        } else {
            description = description.replaceAll("src=\"//", "src=\"http://");
        }

        int start = description.indexOf("<body>") + "<body>".length();
        int end = description.lastIndexOf("</body>");

        String body = description;

        if (start != -1 && end != -1 && start < end) {
            body = description.substring(start, end);
        }

        String css = ".embed-responsive-16by9 {\n" +
                "    padding-bottom: 56.25%;\n" +
                "}\n" +
                ".embed-responsive {\n" +
                "    position: relative;\n" +
                "    display: block;\n" +
                "    height: 0;\n" +
                "    overflow: hidden;\n" +
                "}\n" +
                "\n" +
                ".embed-responsive .embed-responsive-item, .embed-responsive iframe, .embed-responsive embed, .embed-responsive object, .embed-responsive video {\n" +
                "    position: absolute;\n" +
                "    top: 0;\n" +
                "    left: 0;\n" +
                "    bottom: 0;\n" +
                "    height: 100%;\n" +
                "    width: 100%;\n" +
                "    border: 0;\n" +
                "}\n" +
                "\n" +
                ".img-responsive, .thumbnail>img, .thumbnail a>img, .carousel-inner>.item>img, .carousel-inner>.item>a>img {\n" +
                "    display: block;\n" +
                "    max-width: 100%;\n" +
                "    height: auto;\n" +
                "}\n" +
                "img {\n" +
                "    vertical-align: middle;\n" +
                "}\n" +
                "img {\n" +
                "    border: 0;\n" +
                "}";

        String html = String.format("<html><head><style>%s</style></head><body>%s</body></html>",
                css,
                body);

        mWebView.loadData(html, "text/html; charset=utf-8", "UTF-8");

        String urlImage = BuildConfig.API_URL + event.getImageCover() + "/cover";

        Picasso.with(getContext())
                .load(urlImage)
                .placeholder(R.drawable.image_cover_default)
                .into(mImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mWebContainer.removeAllViews();
        mWebView.destroy();
    }
}