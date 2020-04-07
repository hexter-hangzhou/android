package com.hexter.checkshakeapp;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static Visualizer visualizer;
    Boolean isRight = false, isStart = false, analyzeMode = true, isRunning = false;
    Button start;
    RecordAudio recordTask = null;
    TextView showinfo;
    GraphView graph;
    int frequency = 44100, delay = 3;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;//CHANNEL_IN_STEREO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int blockSize = 64 * 2048;
    int wb=10000;
    int isfre=1;
    int dotpers=300;
    DataPoint[] mDataPoint = new DataPoint[2 * wb];
    double[] toTransform = new double[blockSize];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            List<String> mPermissionList = new ArrayList<>();

            //检测是否有写的权限
            int permission = this.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                mPermissionList.add("android.permission.WRITE_EXTERNAL_STORAGE");
            }
            permission = this.checkSelfPermission("android.permission.WAKE_LOCK");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add("android.permission.WAKE_LOCK");
            }
            permission = this.checkSelfPermission("android.permission.RECORD_AUDIO");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add("android.permission.RECORD_AUDIO");
            }
            permission = this.checkSelfPermission("android.permission.MODIFY_AUDIO_SETTINGS");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add("android.permission.MODIFY_AUDIO_SETTINGS");
            }
            permission = this.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add("android.permission.READ_EXTERNAL_STORAGE");
            }
            if (mPermissionList.size() > 0) {
                this.requestPermissions(mPermissionList.toArray(new String[mPermissionList.size()]), 1001);
            } else {
                //  visualizer = new Visualizer(0);

                isRight = true;
            }
            ;

        } catch (Exception e) {
            e.printStackTrace();
        }
        start = (Button) this.findViewById(R.id.start);
        showinfo = (TextView) this.findViewById(R.id.showinfo);
        showinfo.setBackgroundColor(Color.GREEN);
        graph = (GraphView) findViewById(R.id.graph);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRight) {
                    if (isRunning) {

                        isRunning = false;
                        start.setText("Start");
                    } else {
                        isRunning = true;
                        start.setText("Stop");
                        if (recordTask == null) {
                            isStart = true;
                            recordTask = new RecordAudio();

                            recordTask.execute();
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == 1001) {
            try {
                List<String> mPermissionList = new ArrayList<>();

                //检测是否有写的权限
                int permission = this.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // 没有写的权限，去申请写的权限，会弹出对话框
                    mPermissionList.add("android.permission.WRITE_EXTERNAL_STORAGE");
                }
                permission = this.checkSelfPermission("android.permission.WAKE_LOCK");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add("android.permission.WAKE_LOCK");
                }
                permission = this.checkSelfPermission("android.permission.RECORD_AUDIO");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add("android.permission.RECORD_AUDIO");
                }
                permission = this.checkSelfPermission("android.permission.MODIFY_AUDIO_SETTINGS");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add("android.permission.MODIFY_AUDIO_SETTINGS");
                }
                permission = this.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE");
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add("android.permission.READ_EXTERNAL_STORAGE");
                }
                if (mPermissionList.size() > 0) {
                    showToast("还有部分权限未授予");
                    this.requestPermissions(
                            mPermissionList.toArray(new String[mPermissionList.size()]), 1001);
                } else {
                    showToast("谢谢授权");
                    //visualizer = new Visualizer(0);
                    isRight = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showToast(String string) {
        Toast toast = Toast.makeText(MainActivity.this, string, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onDestroy() {
        isStart = false;
        super.onDestroy();

    }

    @Override
    public void onPause() {

        // isStart = false;
        //   start.setText("Start");
        if (recordTask != null) {

            //   recordTask.cancel(true);
            //    recordTask=null;
        }
        if (visualizer != null) {
            visualizer.release();
            visualizer = null;
        }
        super.onPause();
    }

    public class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            if (analyzeMode) {

                // RECORD FROM MICROPHONE
                try {

                    //SET UP AUDIORECORDER
                    int bufferSize = AudioRecord.getMinBufferSize(frequency,
                            channelConfiguration, audioEncoding);
                    AudioRecord audioRecord = new AudioRecord(
                            MediaRecorder.AudioSource.MIC, frequency,
                            channelConfiguration, audioEncoding, bufferSize);
                    NoiseSuppressor suppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
                    if (suppressor.isAvailable()) {
                        suppressor.setEnabled(true);
                    } else {
                        showToast( "您的手机不支持噪音消除");
                    }
                    short[] buffer = new short[blockSize];

                    double[] progress = new double[8];
                    audioRecord.startRecording();
                    for(int i=0;i <2*wb;i++)mDataPoint[i] =new DataPoint(i,0);

                    // RECORDS AUDIO & PERFORMS FFT
                    int bufferReadResult;
                    FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
                    Complex[] result;
                    double maxp;
                    int maxid;
                    Comparator cmp = new MyComparator();
                    Comparator cmp2 = new MyComparator2();
                    while (isStart) {
                        bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                        for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                            toTransform[i] = (double) buffer[i] / 32768.0; // / 32768.0
                        }
                        for (int i = bufferReadResult; i < blockSize; i++) {
                            toTransform[i] = 0.0; // / 32768.0
                        }

                        //创建傅里叶方法实例
                        result = fft.transform(toTransform, TransformType.FORWARD);
                        maxp = result[1].abs();
                        maxid = 1;
                        for (int i = 2; i < 9000; i++) { //blockSize / 2
                            if (result[i].abs() > maxp) {

                                maxp = result[i].abs();
                                maxid = i;
                            }
                        }
                        if (isfre==-1)
                        {   int step=frequency/dotpers;
                            double buf=0;
                            for (int i = 0; i < (blockSize)/step; i++) {
                                buf=0;
                                for(int p=0;p<step;p++)buf+=Math.abs(toTransform[i*step+p]);
                                mDataPoint[i] = new DataPoint(i,buf);// result[i].abs());
                            }
                        } else {
                            for (int i = 0; i < 2 * wb; i++) {
                                mDataPoint[i] = new DataPoint(i, result[ i].abs());
                            }
                        }


                        progress[0] = maxp;
                        progress[1] = 1.0 * maxid;
                        progress[2] = mDataPoint[0].getY();
                        progress[3] = mDataPoint[0].getX();


                        publishProgress(progress);

                    }
                    audioRecord.stop();

                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else { // RECORD FROM SOUND MIX



                // SETS UP VISUALIZER
                visualizer = new Visualizer(0);
                visualizer.setEnabled(false);
                int capRate = Visualizer.getMaxCaptureRate();
                visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

                // USES VISUALIZER TO RETURN AUDIO & THEN PERFORMS FFT
                Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                                      int samplingRate) {
                        double[] toTransform = new double[blockSize];
                        for (int i = 0; i < bytes.length; i++) {
                            toTransform[i] = (double) (bytes[i]) / 8192.0; // 32768.0
                        }

                        publishProgress(toTransform);
                    }

                    public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                                 int samplingRate) {
                    }
                };

                int status = visualizer.setDataCaptureListener(captureListener,
                        capRate, true/*wave*/, false/*no fft needed*/);
                visualizer.setScalingMode(Visualizer.SCALING_MODE_AS_PLAYED);
                visualizer.setEnabled(true);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... progress) {
            double freq = progress[0][1] * frequency / blockSize;
            double freq2 = progress[0][3] * frequency / blockSize;
            if (isRunning) {
                showinfo.setText(String.format("%.4f", freq) + ":" + String.format("%.4f", freq2) + "\n" + String.format("%.4f", progress[0][0]) + ":" + String.format("%.4f", progress[0][2]));


                LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(mDataPoint);
                graph.removeAllSeries();
                graph.addSeries(series);
            }

        }


    }

    class MyComparator implements Comparator<DataPoint> {
        @Override
        public int compare(DataPoint o1, DataPoint o2) {
//如果n1小于n2，我们就返回正值，如果n1大于n2我们就返回负值，//这样颠倒一下，就可以实现反向排序了
            if (o1.getY() < o2.getY()) {
                return 1;
            } else if (o1.getY() > o2.getY()) {
                return -1;
            } else {
                return 0;
            }
        }

    }
    class MyComparator2 implements Comparator<DataPoint> {
        @Override
        public int         compare(DataPoint o1, DataPoint o2) {
//如果n1小于n2，我们就返回正值，如果n1大于n2我们就返回负值，//这样颠倒一下，就可以实现反向排序了
            if(o1.getX() < o2.getX()) {
                return -1;
            }else if(o1.getX() > o2.getX()) {
                return 1;
            }else {
                return 0;
            }
        }
    }
}

