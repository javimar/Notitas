package eu.javimar.notitas;

import static eu.javimar.notitas.util.HelperUtils.isContentUriPointingToValidResource;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.databinding.AudioNotaBinding;

@SuppressLint("DefaultLocale")
public class AudioActivity extends AppCompatActivity {

    private AudioNotaBinding binding;

    private MediaPlayer mediaPlayer;

    private double startTime = 0;
    private double finalTime = 0;

    private final Handler myHandler = new Handler();
    private final int forwardTime = 5000;
    private final int backwardTime = 5000;
    private static int oneTimeOnly = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AudioNotaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.seekBar.setClickable(false);
        binding.buttonPause.setEnabled(false);

        if (!isContentUriPointingToValidResource(getIntent().getData(), this)) {
            Toasty.error(this, R.string.err_audio_resource_not_valid,
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        // Assume that this activity always receives an Intent
        mediaPlayer = MediaPlayer.create(this, getIntent().getData());
        
        binding.buttonPlay.setOnClickListener(v -> {
            Toasty.info(AudioActivity.this, R.string.playing, Toast.LENGTH_SHORT).show();
            mediaPlayer.start();
            finalTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();

            if (oneTimeOnly == 0) {
                binding.seekBar.setMax((int) finalTime);
                oneTimeOnly = 1;
            }

            binding.total.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    finalTime)))
            );
            
            binding.remaining.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    startTime)))
            );

            binding.seekBar.setProgress((int) startTime);
            myHandler.postDelayed(UpdateSongTime, 100);
            binding.buttonPause.setEnabled(true);
            binding.buttonPlay.setEnabled(false);
        });

        binding.buttonPause.setOnClickListener(v ->
        {
            Toasty.info(AudioActivity.this, R.string.paused, Toast.LENGTH_SHORT).show();
            mediaPlayer.pause();
            binding.buttonPause.setEnabled(false);
            binding.buttonPlay.setEnabled(true);
        });
        
        binding.buttonFf.setOnClickListener(v -> {
            int temp = (int) startTime;
            if ((temp + forwardTime) <= finalTime) {
                startTime = startTime + forwardTime;
                mediaPlayer.seekTo((int) startTime);
            } else {
                Toasty.warning(getApplicationContext(), "Cannot jump forward 5" +
                        " seconds", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonRew.setOnClickListener(v -> {
            int temp = (int) startTime;
            if ((temp - backwardTime) > 0) {
                startTime = startTime - backwardTime;
                mediaPlayer.seekTo((int) startTime);
            } else {
                Toasty.warning(getApplicationContext(), "Cannot jump backward " +
                        "5 seconds", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            binding.remaining.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            binding.seekBar.setProgress((int) startTime);
            myHandler.postDelayed(this, 100);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Make the “up” button behave like the “back” button to avoid problems
        // if not, detail activity is empty and with old toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return (super.onOptionsItemSelected(item));
    }
}