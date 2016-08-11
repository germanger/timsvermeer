package cl.manger.timsvermeer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class CameraActivity extends Activity {

    private Bitmap originalPhotoBitmap = null;

    private Camera camera = null;
    private MySurfaceView mySurfaceView = null;
    private ImageView imageView = null;

    private int rotation = 0;

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
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {

                TextView currentToolTextView = (TextView) findViewById(R.id.textView_currentTool);

                if (currentToolTextView.getText().toString().equals(getString(R.string.vermeer))) {
                    float tuning = (float) progressValue / seekBar.getMax();

                    if (tuning == 0) {
                        return;
                    }

                    int startX = 0;
                    int startY = 0;
                    int width = (int) (tuning * originalPhotoBitmap.getWidth());
                    int height = originalPhotoBitmap.getHeight();

                    Bitmap modifiedPhotoBitmap = Bitmap.createBitmap(originalPhotoBitmap, startX, startY, width, height);

                    imageView.setImageBitmap(modifiedPhotoBitmap);
                } else if (currentToolTextView.getText().toString().equals(getString(R.string.scale_photo))) {
                    scaleImage(progressValue - 50);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void scaleImage(int scale) {
        float newWidth = originalPhotoBitmap.getWidth() + 1 * scale;
        float newHeight = newWidth/originalPhotoBitmap.getWidth() * originalPhotoBitmap.getHeight();
        originalPhotoBitmap = Bitmap.createScaledBitmap(originalPhotoBitmap, (int) newWidth, (int) newHeight, true);
        imageView.setImageBitmap(originalPhotoBitmap);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(CameraActivity.this, "Back button is disabled", Toast.LENGTH_SHORT).show();
    }

    /*@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            Toast.makeText(CameraActivity.this, "Menu button is disabled", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        enableCamera();
    }
    @Override
    protected void onPause() {
        camera.stopPreview();
        camera.release();

        super.onPause();
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
            autoFocus();
        } else if (itemId == R.id.action_rotatePhoto) {
            rotatePhoto();
        } else if (itemId == R.id.action_vermeer) {
            useVermeerTool();
        } else if (itemId == R.id.action_scalePhoto) {
            useScaleTool();
        }

        return super.onOptionsItemSelected(item);
    }

    private void useVermeerTool() {
        TextView currentToolTextView = (TextView) findViewById(R.id.textView_currentTool);
        currentToolTextView.setText(R.string.vermeer);

        Toast.makeText(CameraActivity.this, "Vermeer tool activated", Toast.LENGTH_SHORT).show();
    }

    private void useScaleTool() {
        TextView currentToolTextView = (TextView) findViewById(R.id.textView_currentTool);
        currentToolTextView.setText(R.string.scale_photo);

        Toast.makeText(CameraActivity.this, "Scale tool activated", Toast.LENGTH_SHORT).show();
    }

    private void autoFocus() {
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
            }
        });
    }

    private void rotatePhoto() {
        Matrix matrix = new Matrix();

        rotation = rotation + 90;
        matrix.postRotate(90);

        originalPhotoBitmap = Bitmap.createBitmap(originalPhotoBitmap , 0, 0, originalPhotoBitmap.getWidth(), originalPhotoBitmap.getHeight(), matrix, true);

        //imageView.setImageBitmap(rotatedBitmap);
    }

    private void enableCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            Log.d("ERROR", "Failed to open camera: " + e.getMessage());
        }

        if (camera != null) {
            mySurfaceView = new MySurfaceView(this, camera);

            FrameLayout cameraFrameLayout = (FrameLayout) findViewById(R.id.frameLayout_camera);

            cameraFrameLayout.removeAllViews();
            cameraFrameLayout.addView(mySurfaceView);
        }
    }
}
