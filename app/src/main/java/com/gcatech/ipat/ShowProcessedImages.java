package com.gcatech.ipat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import domain.ImageServiceResult;
import domain.ProcessedImage;
import services.IProcessedImagesService;
import utils.RestHandler;

public class ShowProcessedImages extends AppCompatActivity {

    ShowProcessedImages.ImageFragmentPagerAdapter imageFragmentPagerAdapter;
    ViewPager viewPager;
    public static List<ProcessedImage> images;
    private FrameLayout tabContainer;
    private IProcessedImagesService processedImagesService;
    private RestHandler restHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_show_processed_images);
            tabContainer = (FrameLayout) findViewById(R.id.tab_container);
            restHandler = new RestHandler();
            processedImagesService = restHandler.getInstance(getString(R.string.apiBaseUrl), IProcessedImagesService.class);
            String token = FirebaseInstanceId.getInstance().getToken();
            Call<List<ProcessedImage>> imagesCall = processedImagesService.getImages(token);
            imagesCall.enqueue(new Callback<List<ProcessedImage>>() {
                @Override
                public void onResponse(Call<List<ProcessedImage>> call, Response<List<ProcessedImage>> response) {
                    if (response != null && response.isSuccessful()) {
                        List<ProcessedImage> body = response.body();
                        images = body;
                        showImages();
                    }
                }

                @Override
                public void onFailure(Call<List<ProcessedImage>> call, Throwable throwable) {
                    Toast.makeText(ShowProcessedImages.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Throwable t) {
            int i = 0;
        }
    }

    private void showImages() {
        if (images != null) {
            imageFragmentPagerAdapter = new ShowProcessedImages.ImageFragmentPagerAdapter(getSupportFragmentManager());
            viewPager = (ViewPager) findViewById(R.id.pager);
            viewPager.setAdapter(imageFragmentPagerAdapter);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (position == ShowProcessedImages.images.size() - 1) {
                        tabContainer.setVisibility(View.GONE);
                    } else if (tabContainer.getVisibility() == View.GONE) {
                        tabContainer.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            tabLayout.setupWithViewPager(viewPager, true);
        }


        TextView nextButton = (TextView) findViewById(R.id.next_button);
        nextButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int currentItem = viewPager.getCurrentItem();
                        viewPager.setCurrentItem(currentItem + 1);
                    }
                }
        );
    }

    public static class ImageFragmentPagerAdapter extends FragmentPagerAdapter {

        public ImageFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Fragment getItem(int position) {
            boolean latest = position == images.size() - 1;
            return ShowProcessedImages.SwipeFragment.newInstance(position, latest);
        }
    }

    public static class SwipeFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View swipeView = inflater.inflate(R.layout.fragment_swipe, container, false);
            Bundle bundle = getArguments();
            int position = bundle.getInt("position");
            boolean latest = bundle.getBoolean("latest");
            ImageButton closeButton = (ImageButton) swipeView.findViewById(R.id.close_button);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().finish();
                }
            });
            if (latest)
                closeButton.setVisibility(View.VISIBLE);
            else
                closeButton.setVisibility(View.INVISIBLE);

            String imageUrl = images.get(position).getUrl();
            String apiUrl = getString(R.string.apiBaseUrl);
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
            imageUrl = String.format("%s%s", apiUrl, imageUrl);
            String title = images.get(position).getTitle();
            setTitle(swipeView, title);
            setImage(swipeView, imageUrl);
            return swipeView;
        }

        private void setTitle(View view, String title) {
            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setPadding(0, 20, 0, 0);
            textView.setText(title);
        }

        public static ShowProcessedImages.SwipeFragment newInstance(int position, boolean latest) {
            ShowProcessedImages.SwipeFragment swipeFragment = new ShowProcessedImages.SwipeFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            bundle.putBoolean("latest", latest);

            swipeFragment.setArguments(bundle);
            return swipeFragment;
        }

        private void setImage(View view, String imageUrl) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(false)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
            final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

            imageLoader.displayImage(imageUrl, imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {

                }
            });
        }
    }
}


