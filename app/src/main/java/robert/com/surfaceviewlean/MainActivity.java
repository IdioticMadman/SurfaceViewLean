package robert.com.surfaceviewlean;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private LuckyPanView mLucky;
    private ImageView mIvOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLucky = findViewById(R.id.lucky_pan);
        mIvOperator = findViewById(R.id.iv_operator);
        mIvOperator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLucky.isTurning()) {
                    if (mLucky.isEnding()) {
                        Toast.makeText(MainActivity.this, "请等待当前开奖结果", Toast.LENGTH_SHORT).show();
                    } else {
                        mIvOperator.setImageResource(R.drawable.start);
                        mLucky.luckyStop();
                    }
                } else {
                    mIvOperator.setImageResource(R.drawable.stop);
                    mLucky.luckyStart(3);
                }
            }
        });
    }


}
