package io.codetail.circualrevealsample;

import android.animation.Animator;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.VideoView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import io.codetail.animation.SpringViewAnimatorManager;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.ViewRevealManager;
import io.codetail.widget.RevealFrameLayout;

/**
 * https://www.google.com/design/spec/motion/choreography.html#choreography-radial-reaction
 */
@SuppressWarnings("ConstantConditions") public class RadialTransformationActivity
    extends AppCompatActivity {

  private final static String VIDEO_URL =
      "https://material-design.storage.googleapis.com/publish/material_v_8/material_ext_publish/0B14F_FSUCc01WUt2SFZkbGVuNVk/RR_Point_of_Contact_001.mp4";

  @BindView(R.id.view_stack) RevealFrameLayout stack;
  @BindView(R.id.san_francisco) ImageView sanFranciscoView;
  @BindView(R.id.video) VideoView videoView;
  @BindView(R.id.springSettings) SpringSettingsBottomDialog settingsView;

  private int currentViewIndex = 0;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sample_2);
    ButterKnife.bind(this);

    Picasso.with(this)
        .load("http://camp-campbell.com/wp-content/uploads/2014/09/847187872-san-francisco.jpg")
        .resizeDimen(R.dimen.radial_card_width, R.dimen.radial_card_height)
        .centerCrop()
        .into(sanFranciscoView);

    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override public void onPrepared(MediaPlayer mp) {
        mp.setLooping(true);
      }
    });
    videoView.setVideoURI(Uri.parse(VIDEO_URL));
    videoView.start();

    final GestureDetector detector = new GestureDetector(this, tapDetector);

    for (int i = 0; i < stack.getChildCount(); i++) {
      View view = stack.getChildAt(i);
      view.setOnTouchListener(new View.OnTouchListener() {
        @Override public boolean onTouch(View v, MotionEvent event) {
          return detector.onTouchEvent(event);
        }
      });
    }

    final ViewRevealManager revealManager = new ViewRevealManager();
    final SpringViewAnimatorManager springManager = new SpringViewAnimatorManager();
    springManager.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
    springManager.setStiffness(SpringForce.STIFFNESS_LOW);

    stack.setViewRevealManager(revealManager);

    settingsView.addSwitch("Enable Spring", false, new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        stack.setViewRevealManager(isChecked ? springManager : revealManager);
      }
    });
    settingsView.setAnimatorManager(springManager);

    final BottomSheetBehavior behavior = BottomSheetBehavior.from(settingsView);
    behavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_peek_height));
    behavior.setSkipCollapsed(false);
    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
  }

  private GestureDetector.OnGestureListener tapDetector =
      new GestureDetector.SimpleOnGestureListener() {
        @Override public boolean onDown(MotionEvent e) {
          return true;
        }

        @Override public boolean onSingleTapUp(MotionEvent e) {
          View nextView = getNext();
          nextView.bringToFront();
          nextView.setVisibility(View.VISIBLE);

          final float finalRadius =
              (float) Math.hypot(nextView.getWidth() / 2f, nextView.getHeight() / 2f) + hypo(
                  nextView, e);

          Animator revealAnimator =
              ViewAnimationUtils.createCircularReveal(nextView, (int) e.getX(), (int) e.getY(), 0,
                  finalRadius, View.LAYER_TYPE_HARDWARE);

          revealAnimator.setDuration(MainActivity.SLOW_DURATION);
          revealAnimator.setInterpolator(new FastOutLinearInInterpolator());
          revealAnimator.start();

          return true;
        }
      };

  private float hypo(View view, MotionEvent event) {
    Point p1 = new Point((int) event.getX(), (int) event.getY());
    Point p2 = new Point(view.getWidth() / 2, view.getHeight() / 2);

    return (float) Math.sqrt(Math.pow(p1.y - p2.y, 2) + Math.pow(p1.x - p2.x, 2));
  }

  private View getCurrentView() {
    return stack.getChildAt(currentViewIndex);
  }

  private View getNext() {
    return getViewAt(++currentViewIndex);
  }

  private View getViewAt(int index) {
    if (index >= stack.getChildCount()) {
      index = 0;
    } else if (index < 0) {
      index = stack.getChildCount() - 1;
    }
    return stack.getChildAt(index);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    videoView.suspend();
  }
}
