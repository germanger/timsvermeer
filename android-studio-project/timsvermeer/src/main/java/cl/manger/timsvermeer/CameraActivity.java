package cl.manger.timsvermeer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

public class CameraActivity extends Activity {

    private static final int SELECT_PICTURE = 1;
    private Bitmap originalPhotoBitmap = null;

    private Camera camera = null;
    private MySurfaceView mySurfaceView = null;
    private ImageView imageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Prepare photo that was chosen in MainActivity
        Intent intent = getIntent();
        this.originalPhotoBitmap = BitmapFactory.decodeFile(intent.getStringExtra("photoPath"));

        // Get screen width
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int photoWidth = size.x;
        int photoHeight = (int) (photoWidth / (float) originalPhotoBitmap.getWidth() * originalPhotoBitmap.getHeight());

        this.originalPhotoBitmap = Bitmap.createScaledBitmap(originalPhotoBitmap, photoWidth, photoHeight, true);

        // Set image
        imageView = (ImageView) findViewById(R.id.imageView_photo);
        imageView.setImageBitmap(this.originalPhotoBitmap);

        // Enable camera
        enableCamera();

        SeekBar opacitySeekBar = (SeekBar) findViewById(R.id.seekBar_opacity);
        opacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                imageView.setAlpha((float) progress / seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar cropSeekBar = (SeekBar) findViewById(R.id.seekBar_crop);
        cropSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;

                float tuning = (float) progress / seekBar.getMax();

                if (tuning == 0) {
                    return;
                }

                int startX = 0;
                int startY = 0;
                int width = (int) (tuning * originalPhotoBitmap.getWidth());
                int height = originalPhotoBitmap.getHeight();

                Bitmap modifiedPhotoBitmap = Bitmap.createBitmap(originalPhotoBitmap, startX, startY, width, height);

                imageView.setImageBitmap(modifiedPhotoBitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();

        if (itemId == R.id.action_focusCamera) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                }
            });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            Log.d("ERROR", "Failed to open camera: " + e.getMessage());
        }

        if (camera != null) {
            FrameLayout cameraFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_camera);

            mySurfaceView = new MySurfaceView(this, camera);
            cameraFrameLayout.addView(mySurfaceView);
        }
    }
}
