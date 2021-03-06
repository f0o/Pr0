package com.pr0gramm.app.ui.views.viewer;

import android.annotation.SuppressLint;
import android.net.Uri;

import com.google.common.base.Optional;
import com.pr0gramm.app.ActivityComponent;
import com.pr0gramm.app.services.gif.GifToVideoService;
import com.pr0gramm.app.services.proxy.ProxyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import rx.Observable;

import static com.pr0gramm.app.util.AndroidUtility.checkMainThread;

/**
 */
@SuppressLint("ViewConstructor")
public class Gif2VideoMediaView extends ProxyMediaView {
    private static final Logger logger = LoggerFactory.getLogger("Gif2VideoMediaView");

    @Inject
    GifToVideoService gifToVideoService;

    @Inject
    ProxyService proxyService;

    Gif2VideoMediaView(Config config) {
        super(config);
        startWebmConversion();
    }

    private void startWebmConversion() {
        logger.info("Start converting gif to webm");

        // normalize to http://
        String gifUrl = getMediaUri().toString().replace("https://", "http://");

        // and start conversion!
        gifToVideoService.toVideo(gifUrl)
                .compose(backgroundBindView())
                .onErrorResumeNext(Observable.just(new GifToVideoService.Result(gifUrl)))
                .limit(1)
                .doAfterTerminate(this::hideBusyIndicator)
                .subscribe(this::handleConversionResult);
    }

    private void handleConversionResult(GifToVideoService.Result result) {
        checkMainThread();

        // create the correct child-viewer
        MediaView mediaView;
        Optional<String> oVideoUrl = result.getVideoUrl();
        if (oVideoUrl.isPresent()) {
            logger.info("Converted successfully, replace with video player");
            Uri videoUri = Uri.parse(oVideoUrl.get());
            MediaUri webm = getMediaUri().withUri(videoUri, MediaUri.MediaType.VIDEO);
            mediaView = MediaViews.newInstance(ImmutableConfig.copyOf(config).withMediaUri(webm));

        } else {
            logger.info("Conversion did not work, showing gif");
            mediaView = new GifMediaView(config);
        }

        mediaView.removePreviewImage();
        setChild(mediaView);
    }

    @Override
    protected void injectComponent(ActivityComponent component) {
        component.inject(this);
    }
}
