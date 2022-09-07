package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlay_MainActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        updateSeek.interrupt();
    }

    TextView songNameDisplay, currentTime, totalTime;
    ImageView previousSongPlayBtn, playPauseBtn, nextSongPlayBtn;
    ArrayList<File> songs;
    SeekBar seekBar;
    MediaPlayer mediaPlayer;
    String songTextContent;
    int position;
    Thread updateSeek;
    Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play_main);

        songNameDisplay = (TextView) findViewById(R.id.songNameDisplay);
        previousSongPlayBtn = (ImageView) findViewById(R.id.previousSongPlayBtn);
        playPauseBtn = (ImageView) findViewById(R.id.playPauseBtn);
        nextSongPlayBtn = (ImageView) findViewById(R.id.nextSongPlayBtn);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        currentTime = (TextView) findViewById(R.id.currentTime);
        totalTime = (TextView) findViewById(R.id.totalTime);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs = (ArrayList) bundle.getParcelableArrayList("songList");
        songTextContent = intent.getStringExtra("currentSong");
        songNameDisplay.setText(songTextContent);
        songNameDisplay.setSelected(true);
        position = intent.getIntExtra("position", 0);

        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String totalLabel = createTimeTable(mediaPlayer.getDuration());
                totalTime.setText(totalLabel);



            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        if (mediaPlayer != null) {
            mediaPlayer.start();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                currentTime.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        currentTime.setText(createTimeTable(mediaPlayer.getCurrentPosition()));
                                    }
                                });
                            } else {
                                timer.cancel();
                                timer.purge();
                            }
                        }
                    });
                }
            }, 0, 1000);
        }

        updateSeek = new Thread(){
            @Override
            public void run() {
                int currentPosition = 0;
                try {
                    while(currentPosition<mediaPlayer.getDuration()){
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        sleep((800));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    playPauseBtn.setImageResource(R.drawable.play_icon);
                    mediaPlayer.pause();
                }
                else{
                    playPauseBtn.setImageResource(R.drawable.pause_icon);
                    mediaPlayer.start();
                }
            }
        });

        previousSongPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position != 0){
                    position = position - 1;
                }else {
                    position = songs.size() - 1;
                }
                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
                songTextContent = songs.get(position).getName().toString();
                songNameDisplay.setText(songTextContent);
                playPauseBtn.setImageResource(R.drawable.pause_icon);
            }
        });

        nextSongPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position != songs.size() - 1){
                    position = position + 1;
                }else {
                    position = 0;
                }

                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
                songTextContent = songs.get(position).getName().toString();
                songNameDisplay.setText(songTextContent);
                playPauseBtn.setImageResource(R.drawable.pause_icon);
            }
        });
    }

    public String createTimeTable(int duration){
        String timeLabel = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        timeLabel += min + ":";

        if(sec < 10) timeLabel += "0";
        timeLabel += sec;
        return timeLabel;
    }
}